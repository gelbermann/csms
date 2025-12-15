package com.cp.csms.authentication

import com.cp.csms.authentication.validation.ValidationService
import com.cp.csms.common.AuthenticationMessage
import com.cp.csms.common.AuthenticationStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class AuthenticationConsumerTest {

    @Mock
    private lateinit var validationService: ValidationService

    @Mock
    private lateinit var authenticationService: AuthenticationService

    @InjectMocks
    private lateinit var consumer: AuthenticationConsumer

    @Test
    fun `should return invalid when validation fails`() {
        val message = AuthenticationMessage("req-123", "invalid-token")
        whenever(validationService.isValid(message)).thenReturn(false)

        val response = consumer.handleAuthRequest(message)

        assertThat(response.requestId).isEqualTo("req-123")
        assertThat(response.authenticationStatus).isEqualTo(AuthenticationStatus.INVALID)
        verify(validationService).isValid(message)
        verifyNoInteractions(authenticationService)
    }

    @Test
    fun `should return accepted when validation passes and authentication succeeds`() {
        val message = AuthenticationMessage("req-456", "valid-token")
        whenever(validationService.isValid(message)).thenReturn(true)
        whenever(authenticationService.authenticate("valid-token")).thenReturn(AuthenticationStatus.ACCEPTED)

        val response = consumer.handleAuthRequest(message)

        assertThat(response.requestId).isEqualTo("req-456")
        assertThat(response.authenticationStatus).isEqualTo(AuthenticationStatus.ACCEPTED)
        verify(validationService).isValid(message)
        verify(authenticationService).authenticate("valid-token")
    }

    @Test
    fun `should return rejected when validation passes but token is disabled`() {
        val message = AuthenticationMessage("req-789", "disabled-token")
        whenever(validationService.isValid(message)).thenReturn(true)
        whenever(authenticationService.authenticate("disabled-token")).thenReturn(AuthenticationStatus.REJECTED)

        val response = consumer.handleAuthRequest(message)

        assertThat(response.requestId).isEqualTo("req-789")
        assertThat(response.authenticationStatus).isEqualTo(AuthenticationStatus.REJECTED)
        verify(validationService).isValid(message)
        verify(authenticationService).authenticate("disabled-token")
    }

    @Test
    fun `should return unknown when validation passes but token is not found`() {
        val message = AuthenticationMessage("req-999", "unknown-token")
        whenever(validationService.isValid(message)).thenReturn(true)
        whenever(authenticationService.authenticate("unknown-token")).thenReturn(AuthenticationStatus.UNKNOWN)

        val response = consumer.handleAuthRequest(message)

        assertThat(response.requestId).isEqualTo("req-999")
        assertThat(response.authenticationStatus).isEqualTo(AuthenticationStatus.UNKNOWN)
        verify(validationService).isValid(message)
        verify(authenticationService).authenticate("unknown-token")
    }

    @Test
    fun `should preserve request id in response`() {
        val requestId = "unique-request-id-12345"
        val message = AuthenticationMessage(requestId, "some-token")
        whenever(validationService.isValid(message)).thenReturn(false)

        val response = consumer.handleAuthRequest(message)

        assertThat(response.requestId).isEqualTo(requestId)
    }
}
