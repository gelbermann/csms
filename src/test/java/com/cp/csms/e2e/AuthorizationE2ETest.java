package com.cp.csms.e2e;

import com.cp.csms.common.AuthenticationStatus;
import com.cp.csms.e2e.support.TestDataBuilder;
import com.cp.csms.transactions.AuthorizationRequest;
import com.cp.csms.transactions.AuthorizationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E tests for the authorization flow.
 * Tests the complete flow from API request through Kafka messaging to API response.
 */
public class AuthorizationE2ETest extends BaseE2ETest {

    @Test
    public void shouldCompleteFullAuthorizationFlow_whenTokenAccepted() {
        final AuthorizationRequest request = TestDataBuilder.buildAuthorizationRequest(
                "station-123",
                "driverABC-1234567890"
        );

        final ResponseEntity<AuthorizationResponse> response = restTemplate.postForEntity(
                getAuthorizationUrl(),
                request,
                AuthorizationResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAuthenticationStatus()).isEqualTo(AuthenticationStatus.ACCEPTED);
    }

    @Test
    public void shouldReturnRejectedStatus_whenTokenDisabled() {
        final AuthorizationRequest request = TestDataBuilder.buildAuthorizationRequest(
                "station-123",
                "DISABLED_suspended-account-driver-token-abc"
        );

        ResponseEntity<AuthorizationResponse> response = restTemplate.postForEntity(
                getAuthorizationUrl(),
                request,
                AuthorizationResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAuthenticationStatus()).isEqualTo(AuthenticationStatus.REJECTED);
    }

    @Test
    public void shouldReturnUnknownStatus_whenTokenNotFound() {
        final AuthorizationRequest request = TestDataBuilder.buildAuthorizationRequest(
                "station-123",
                "non-existent-token-xyz"
        );

        final ResponseEntity<AuthorizationResponse> response = restTemplate.postForEntity(
                getAuthorizationUrl(),
                request,
                AuthorizationResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAuthenticationStatus()).isEqualTo(AuthenticationStatus.UNKNOWN);
    }

    @Test
    public void shouldReturnInvalidStatus_whenValidationFails() {
        final AuthorizationRequest request = TestDataBuilder.buildAuthorizationRequest(
                "station-123",
                null
        );

        final ResponseEntity<AuthorizationResponse> response = restTemplate.postForEntity(
                getAuthorizationUrl(),
                request,
                AuthorizationResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAuthenticationStatus()).isEqualTo(AuthenticationStatus.INVALID);
    }

}
