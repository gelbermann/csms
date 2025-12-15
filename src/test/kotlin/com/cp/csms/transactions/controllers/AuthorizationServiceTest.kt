package com.cp.csms.transactions.controllers

import com.cp.csms.common.AuthenticationMessage
import com.cp.csms.common.AuthenticationResponse
import com.cp.csms.common.AuthenticationStatus
import com.cp.csms.config.KafkaTopicConfig
import com.cp.csms.transactions.AuthorizationRequest
import com.cp.csms.transactions.AuthorizationService
import com.cp.csms.transactions.DriverIdentifier
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeoutException

@ExtendWith(MockitoExtension::class)
class AuthorizationServiceTest {

    @Mock
    private lateinit var kafkaProducer: KafkaTemplate<String, AuthenticationMessage>

    @Mock
    private lateinit var kafkaTopicConfig: KafkaTopicConfig

    private lateinit var underTest: AuthorizationService

    @Captor
    private lateinit var requestIdCaptor: ArgumentCaptor<String>

    @Captor
    private lateinit var messageCaptor: ArgumentCaptor<AuthenticationMessage>

    @BeforeEach
    fun setUp() {
        underTest = AuthorizationService(kafkaProducer, kafkaTopicConfig, TIMEOUT_SECONDS)
    }

    @Test
    fun `authorize should send kafka message and return accepted status when response received in time`() {
        stubKafkaConfiguration()
        val request = createAuthorizationRequest()
        stubKafkaProducer()

        val authorizationFuture = executeAuthorizationAsync(request)
        waitForAsyncSetup()

        val sentMessage = captureKafkaMessage()
        val sentRequestId = sentMessage.requestId!!

        val incomingResponse = createAuthResponse(sentRequestId, AuthenticationStatus.ACCEPTED)
        underTest.handleAuthResponse(incomingResponse)

        val result = authorizationFuture.get()

        assertThat(result).isEqualTo(AuthenticationStatus.ACCEPTED)
        assertThat(sentMessage.requestId).isEqualTo(sentRequestId)
        assertThat(sentMessage.token).isEqualTo(DRIVER_ID)
    }

    @Test
    fun `authorize should throw timeout exception when timeout occurs`() {
        stubKafkaConfiguration()
        val request = createAuthorizationRequest()
        stubKafkaProducer()

        assertThatThrownBy { underTest.authorize(request) }
            .isInstanceOf(TimeoutException::class.java)

        verify(kafkaProducer).send(eq(AUTH_REQUEST_TOPIC), any(), any())
    }

    @Test
    fun `authorize should send correct message to kafka`() {
        stubKafkaConfiguration()
        val request = createAuthorizationRequest()
        stubKafkaProducer()

        assertThatThrownBy { underTest.authorize(request) }
            .isInstanceOf(TimeoutException::class.java)

        val sentMessage = captureKafkaMessage()
        val sentRequestId = requestIdCaptor.value

        assertThat(sentRequestId).isNotNull()
        assertThat(sentMessage.requestId).isNotNull().isEqualTo(sentRequestId)
        assertThat(sentMessage.token).isEqualTo(DRIVER_ID)
    }

    @Test
    fun `handleAuthResponse should complete future when pending request exists`() {
        stubKafkaConfiguration()
        val request = createAuthorizationRequest()
        stubKafkaProducer()

        val authorizationFuture = executeAuthorizationAsync(request)
        waitForAsyncSetup()

        verify(kafkaProducer).send(eq(AUTH_REQUEST_TOPIC), requestIdCaptor.capture(), any())
        val sentRequestId = requestIdCaptor.value

        val incomingResponse = createAuthResponse(sentRequestId, AuthenticationStatus.INVALID)
        underTest.handleAuthResponse(incomingResponse)

        val result = authorizationFuture.get()
        assertThat(result).isEqualTo(AuthenticationStatus.INVALID)
    }

    @Test
    fun `handleAuthResponse should do nothing when no pending request exists`() {
        val nonExistentRequestId = "non-existent-request-id"
        val response = createAuthResponse(nonExistentRequestId, AuthenticationStatus.ACCEPTED)

        underTest.handleAuthResponse(response)
    }

    private fun createAuthorizationRequest(): AuthorizationRequest {
        val driverIdentifier = DriverIdentifier(DRIVER_ID)
        return AuthorizationRequest(STATION_UUID, driverIdentifier)
    }

    private fun stubKafkaConfiguration() {
        whenever(kafkaTopicConfig.authRequestTopic).thenReturn(AUTH_REQUEST_TOPIC)
    }

    private fun stubKafkaProducer() {
        val future = CompletableFuture<SendResult<String, AuthenticationMessage>>()
        whenever(kafkaProducer.send(any(), any(), any()))
            .thenReturn(future)
    }

    private fun executeAuthorizationAsync(request: AuthorizationRequest): CompletableFuture<AuthenticationStatus> {
        return CompletableFuture.supplyAsync {
            try {
                underTest.authorize(request)
            } catch (e: TimeoutException) {
                throw RuntimeException(e)
            }
        }
    }

    private fun waitForAsyncSetup() {
        try {
            Thread.sleep(ASYNC_SETUP_DELAY_MS.toLong())
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    private fun captureKafkaMessage(): AuthenticationMessage {
        verify(kafkaProducer).send(eq(AUTH_REQUEST_TOPIC), requestIdCaptor.capture(), messageCaptor.capture())
        return messageCaptor.value
    }

    private fun createAuthResponse(requestId: String, status: AuthenticationStatus): AuthenticationResponse {
        return AuthenticationResponse(requestId, status)
    }

    companion object {
        private const val AUTH_REQUEST_TOPIC = "auth-request-topic"
        private const val DRIVER_ID = "driver-123"
        private const val STATION_UUID = "station-uuid"
        private const val ASYNC_SETUP_DELAY_MS = 100
        private const val TIMEOUT_SECONDS = 5
    }
}
