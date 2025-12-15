package com.cp.csms.authentication;

import com.cp.csms.common.AuthenticationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private TokenStatusProvider tokenStatusProvider;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void shouldReturnAcceptedWhenTokenIsEnabled() {
        final String token = "enabled-token";
        when(tokenStatusProvider.isTokenEnabled(token)).thenReturn(Optional.of(true));

        final AuthenticationStatus result = authenticationService.authenticate(token);

        assertThat(result).isEqualTo(AuthenticationStatus.ACCEPTED);
    }

    @Test
    void shouldReturnRejectedWhenTokenIsDisabled() {
        final String token = "disabled-token";
        when(tokenStatusProvider.isTokenEnabled(token)).thenReturn(Optional.of(false));

        final AuthenticationStatus result = authenticationService.authenticate(token);

        assertThat(result).isEqualTo(AuthenticationStatus.REJECTED);
    }

    @Test
    void shouldReturnUnknownWhenTokenIsNotFound() {
        final String token = "unknown-token";
        when(tokenStatusProvider.isTokenEnabled(token)).thenReturn(Optional.empty());

        final AuthenticationStatus result = authenticationService.authenticate(token);

        assertThat(result).isEqualTo(AuthenticationStatus.UNKNOWN);
    }

}
