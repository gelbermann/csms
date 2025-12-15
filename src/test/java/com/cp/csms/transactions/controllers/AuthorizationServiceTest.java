package com.cp.csms.transactions.controllers;

import com.cp.csms.common.AuthenticationMessage;
import com.cp.csms.common.AuthenticationResponse;
import com.cp.csms.common.AuthenticationStatus;
import com.cp.csms.config.KafkaTopicConfig;
import com.cp.csms.transactions.AuthorizationRequest;
import com.cp.csms.transactions.DriverIdentifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthorizationServiceTest {

    @Mock
    private KafkaTemplate<String, AuthenticationMessage> kafkaProducer;

    @Mock
    private KafkaTopicConfig kafkaTopicConfig;

    @InjectMocks
    private AuthorizationService underTest;

    @Captor
    private ArgumentCaptor<String> requestIdCaptor;

    @Captor
    private ArgumentCaptor<AuthenticationMessage> messageCaptor;

    private static final String AUTH_REQUEST_TOPIC = "auth-request-topic";
    private static final String DRIVER_ID = "driver-123";
    private static final String STATION_UUID = "station-uuid";
    private static final int ASYNC_SETUP_DELAY_MS = 100;

    @Test
    public void authorize_shouldSendKafkaMessageAndReturnAcceptedStatus_whenResponseReceivedInTime() {
        stubKafkaConfiguration();
        final AuthorizationRequest request = createAuthorizationRequest();
        stubKafkaProducer();

        final CompletableFuture<Optional<AuthenticationStatus>> authorizationFuture = 
                executeAuthorizationAsync(request);
        waitForAsyncSetup();

        final String sentRequestId = captureKafkaMessage().getRequestId();
        final AuthenticationMessage sentMessage = messageCaptor.getValue();

        final AuthenticationResponse incomingResponse = createAuthResponse(sentRequestId, AuthenticationStatus.ACCEPTED);
        underTest.handleAuthResponse(incomingResponse);

        final Optional<AuthenticationStatus> result = authorizationFuture.join();

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(AuthenticationStatus.ACCEPTED);
        assertThat(sentMessage.getRequestId()).isEqualTo(sentRequestId);
        assertThat(sentMessage.getToken()).isEqualTo(DRIVER_ID);
    }

    @Test
    public void authorize_shouldReturnEmpty_whenTimeoutOccurs() {
        stubKafkaConfiguration();
        final AuthorizationRequest request = createAuthorizationRequest();
        stubKafkaProducer();

        final Optional<AuthenticationStatus> result = underTest.authorize(request);

        assertThat(result).isEmpty();
        verify(kafkaProducer).send(eq(AUTH_REQUEST_TOPIC), anyString(), any(AuthenticationMessage.class));
    }

    @Test
    public void authorize_shouldSendCorrectMessageToKafka() {
        stubKafkaConfiguration();
        final AuthorizationRequest request = createAuthorizationRequest();
        stubKafkaProducer();

        underTest.authorize(request);

        final AuthenticationMessage sentMessage = captureKafkaMessage();
        final String sentRequestId = requestIdCaptor.getValue();

        assertThat(sentRequestId).isNotNull();
        assertThat(sentMessage.getRequestId()).isEqualTo(sentRequestId);
        assertThat(sentMessage.getToken()).isEqualTo(DRIVER_ID);
    }

    @Test
    public void handleAuthResponse_shouldCompleteFuture_whenPendingRequestExists() {
        stubKafkaConfiguration();
        final AuthorizationRequest request = createAuthorizationRequest();
        stubKafkaProducer();

        final CompletableFuture<Optional<AuthenticationStatus>> authorizationFuture = 
                executeAuthorizationAsync(request);
        waitForAsyncSetup();

        verify(kafkaProducer).send(eq(AUTH_REQUEST_TOPIC), requestIdCaptor.capture(), any(AuthenticationMessage.class));
        final String sentRequestId = requestIdCaptor.getValue();

        final AuthenticationResponse incomingResponse = createAuthResponse(sentRequestId, AuthenticationStatus.INVALID);
        underTest.handleAuthResponse(incomingResponse);

        final Optional<AuthenticationStatus> result = authorizationFuture.join();
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(AuthenticationStatus.INVALID);
    }

    @Test
    public void handleAuthResponse_shouldDoNothing_whenNoPendingRequestExists() {
        final String nonExistentRequestId = "non-existent-request-id";
        final AuthenticationResponse response = createAuthResponse(nonExistentRequestId, AuthenticationStatus.ACCEPTED);

        underTest.handleAuthResponse(response);
    }

    private AuthorizationRequest createAuthorizationRequest() {
        final DriverIdentifier driverIdentifier = DriverIdentifier.builder()
                .id(DRIVER_ID)
                .build();

        return AuthorizationRequest.builder()
                .stationUuid(STATION_UUID)
                .driverIdentifier(driverIdentifier)
                .build();
    }

    private void stubKafkaConfiguration() {
        when(kafkaTopicConfig.getAuthRequestTopic()).thenReturn(AUTH_REQUEST_TOPIC);
    }

    private void stubKafkaProducer() {
        final CompletableFuture<SendResult<String, AuthenticationMessage>> future = new CompletableFuture<>();
        when(kafkaProducer.send(anyString(), anyString(), any(AuthenticationMessage.class)))
                .thenReturn(future);
    }

    private CompletableFuture<Optional<AuthenticationStatus>> executeAuthorizationAsync(AuthorizationRequest request) {
        return CompletableFuture.supplyAsync(() -> underTest.authorize(request));
    }

    private void waitForAsyncSetup() {
        try {
            Thread.sleep(ASYNC_SETUP_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private AuthenticationMessage captureKafkaMessage() {
        verify(kafkaProducer).send(eq(AUTH_REQUEST_TOPIC), requestIdCaptor.capture(), messageCaptor.capture());
        return messageCaptor.getValue();
    }

    private AuthenticationResponse createAuthResponse(String requestId, AuthenticationStatus status) {
        return AuthenticationResponse.builder()
                .requestId(requestId)
                .status(status)
                .build();
    }
}
