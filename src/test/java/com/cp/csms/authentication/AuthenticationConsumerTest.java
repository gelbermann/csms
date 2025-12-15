package com.cp.csms.authentication;

import com.cp.csms.authentication.validation.ValidationService;
import com.cp.csms.common.AuthenticationMessage;
import com.cp.csms.common.AuthenticationResponse;
import com.cp.csms.common.AuthenticationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationConsumerTest {

    @Mock
    private ValidationService validationService;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationConsumer consumer;

    @Test
    void shouldReturnInvalidWhenValidationFails() {
        final AuthenticationMessage message = new AuthenticationMessage("req-123", "invalid-token");
        when(validationService.isValid(message)).thenReturn(false);

        final AuthenticationResponse response = consumer.handleAuthRequest(message);

        assertThat(response.getRequestId()).isEqualTo("req-123");
        assertThat(response.getAuthenticationStatus()).isEqualTo(AuthenticationStatus.INVALID);
        verify(validationService).isValid(message);
        verifyNoInteractions(authenticationService);
    }

    @Test
    void shouldReturnAcceptedWhenValidationPassesAndAuthenticationSucceeds() {
        final AuthenticationMessage message = new AuthenticationMessage("req-456", "valid-token");
        when(validationService.isValid(message)).thenReturn(true);
        when(authenticationService.authenticate("valid-token")).thenReturn(AuthenticationStatus.ACCEPTED);

        final AuthenticationResponse response = consumer.handleAuthRequest(message);

        assertThat(response.getRequestId()).isEqualTo("req-456");
        assertThat(response.getAuthenticationStatus()).isEqualTo(AuthenticationStatus.ACCEPTED);
        verify(validationService).isValid(message);
        verify(authenticationService).authenticate("valid-token");
    }

    @Test
    void shouldReturnRejectedWhenValidationPassesButTokenIsDisabled() {
        final AuthenticationMessage message = new AuthenticationMessage("req-789", "disabled-token");
        when(validationService.isValid(message)).thenReturn(true);
        when(authenticationService.authenticate("disabled-token")).thenReturn(AuthenticationStatus.REJECTED);

        final AuthenticationResponse response = consumer.handleAuthRequest(message);

        assertThat(response.getRequestId()).isEqualTo("req-789");
        assertThat(response.getAuthenticationStatus()).isEqualTo(AuthenticationStatus.REJECTED);
        verify(validationService).isValid(message);
        verify(authenticationService).authenticate("disabled-token");
    }

    @Test
    void shouldReturnUnknownWhenValidationPassesButTokenIsNotFound() {
        final AuthenticationMessage message = new AuthenticationMessage("req-999", "unknown-token");
        when(validationService.isValid(message)).thenReturn(true);
        when(authenticationService.authenticate("unknown-token")).thenReturn(AuthenticationStatus.UNKNOWN);

        final AuthenticationResponse response = consumer.handleAuthRequest(message);

        assertThat(response.getRequestId()).isEqualTo("req-999");
        assertThat(response.getAuthenticationStatus()).isEqualTo(AuthenticationStatus.UNKNOWN);
        verify(validationService).isValid(message);
        verify(authenticationService).authenticate("unknown-token");
    }

    @Test
    void shouldPreserveRequestIdInResponse() {
        final String requestId = "unique-request-id-12345";
        final AuthenticationMessage message = new AuthenticationMessage(requestId, "some-token");
        when(validationService.isValid(message)).thenReturn(false);

        final AuthenticationResponse response = consumer.handleAuthRequest(message);

        assertThat(response.getRequestId()).isEqualTo(requestId);
    }
}
