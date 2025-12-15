package com.cp.csms.e2e.support;

import com.cp.csms.common.AuthenticationMessage;
import com.cp.csms.common.AuthenticationResponse;
import com.cp.csms.common.AuthenticationStatus;
import com.cp.csms.transactions.AuthorizationRequest;
import com.cp.csms.transactions.DriverIdentifier;

import java.util.UUID;

/**
 * Utility class for building test data objects.
 */
public class TestDataBuilder {

    private TestDataBuilder() {
    }

    /**
     * Builds an AuthorizationRequest with the given parameters.
     *
     * @param stationUuid the station UUID
     * @param driverId    the driver identifier
     * @return a new AuthorizationRequest
     */
    public static AuthorizationRequest buildAuthorizationRequest(String stationUuid, String driverId) {
        return new AuthorizationRequest(stationUuid, new DriverIdentifier(driverId));
    }

    /**
     * Builds an AuthorizationRequest with default test values.
     *
     * @return a new AuthorizationRequest with default values
     */
    public static AuthorizationRequest buildDefaultAuthorizationRequest() {
        return buildAuthorizationRequest("test-station-" + UUID.randomUUID(), "test-driver-" + UUID.randomUUID());
    }

    /**
     * Builds an AuthenticationMessage with the given parameters.
     *
     * @param requestId the request ID
     * @param token     the authentication token
     * @return a new AuthenticationMessage
     */
    public static AuthenticationMessage buildAuthenticationMessage(String requestId, String token) {
        return new AuthenticationMessage(requestId, token);
    }

    /**
     * Builds an AuthenticationMessage with a random request ID.
     *
     * @param token the authentication token
     * @return a new AuthenticationMessage
     */
    public static AuthenticationMessage buildAuthenticationMessage(String token) {
        return buildAuthenticationMessage(UUID.randomUUID().toString(), token);
    }

    /**
     * Builds an AuthenticationResponse with the given parameters.
     *
     * @param requestId the request ID
     * @param status    the authentication status
     * @return a new AuthenticationResponse
     */
    public static AuthenticationResponse buildAuthenticationResponse(String requestId, AuthenticationStatus status) {
        return new AuthenticationResponse(requestId, status);
    }

    /**
     * Builds an AuthenticationResponse with ACCEPTED status.
     *
     * @param requestId the request ID
     * @return a new AuthenticationResponse with ACCEPTED status
     */
    public static AuthenticationResponse buildAcceptedResponse(String requestId) {
        return buildAuthenticationResponse(requestId, AuthenticationStatus.ACCEPTED);
    }

    /**
     * Builds an AuthenticationResponse with REJECTED status.
     *
     * @param requestId the request ID
     * @return a new AuthenticationResponse with REJECTED status
     */
    public static AuthenticationResponse buildRejectedResponse(String requestId) {
        return buildAuthenticationResponse(requestId, AuthenticationStatus.REJECTED);
    }

    /**
     * Builds an AuthenticationResponse with UNKNOWN status.
     *
     * @param requestId the request ID
     * @return a new AuthenticationResponse with UNKNOWN status
     */
    public static AuthenticationResponse buildUnknownResponse(String requestId) {
        return buildAuthenticationResponse(requestId, AuthenticationStatus.UNKNOWN);
    }

    /**
     * Builds an AuthenticationResponse with INVALID status.
     *
     * @param requestId the request ID
     * @return a new AuthenticationResponse with INVALID status
     */
    public static AuthenticationResponse buildInvalidResponse(String requestId) {
        return buildAuthenticationResponse(requestId, AuthenticationStatus.INVALID);
    }
}
