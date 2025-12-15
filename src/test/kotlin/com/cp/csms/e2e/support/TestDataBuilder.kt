package com.cp.csms.e2e.support

import com.cp.csms.common.AuthenticationMessage
import com.cp.csms.common.AuthenticationResponse
import com.cp.csms.common.AuthenticationStatus
import com.cp.csms.transactions.AuthorizationRequest
import com.cp.csms.transactions.DriverIdentifier
import java.util.UUID

/**
 * Utility class for building test data objects.
 */
object TestDataBuilder {

    /**
     * Builds an AuthorizationRequest with the given parameters.
     *
     * @param stationUuid the station UUID
     * @param driverId    the driver identifier
     * @return a new AuthorizationRequest
     */
    fun buildAuthorizationRequest(stationUuid: String, driverId: String?): AuthorizationRequest {
        return AuthorizationRequest(stationUuid, DriverIdentifier(driverId))
    }

    /**
     * Builds an AuthorizationRequest with default test values.
     *
     * @return a new AuthorizationRequest with default values
     */
    fun buildDefaultAuthorizationRequest(): AuthorizationRequest {
        return buildAuthorizationRequest("test-station-${UUID.randomUUID()}", "test-driver-${UUID.randomUUID()}")
    }

    /**
     * Builds an AuthenticationMessage with the given parameters.
     *
     * @param requestId the request ID
     * @param token     the authentication token
     * @return a new AuthenticationMessage
     */
    fun buildAuthenticationMessage(requestId: String, token: String): AuthenticationMessage {
        return AuthenticationMessage(requestId, token)
    }

    /**
     * Builds an AuthenticationMessage with a random request ID.
     *
     * @param token the authentication token
     * @return a new AuthenticationMessage
     */
    fun buildAuthenticationMessage(token: String): AuthenticationMessage {
        return buildAuthenticationMessage(UUID.randomUUID().toString(), token)
    }

    /**
     * Builds an AuthenticationResponse with the given parameters.
     *
     * @param requestId the request ID
     * @param status    the authentication status
     * @return a new AuthenticationResponse
     */
    fun buildAuthenticationResponse(requestId: String, status: AuthenticationStatus): AuthenticationResponse {
        return AuthenticationResponse(requestId, status)
    }

    /**
     * Builds an AuthenticationResponse with ACCEPTED status.
     *
     * @param requestId the request ID
     * @return a new AuthenticationResponse with ACCEPTED status
     */
    fun buildAcceptedResponse(requestId: String): AuthenticationResponse {
        return buildAuthenticationResponse(requestId, AuthenticationStatus.ACCEPTED)
    }

    /**
     * Builds an AuthenticationResponse with REJECTED status.
     *
     * @param requestId the request ID
     * @return a new AuthenticationResponse with REJECTED status
     */
    fun buildRejectedResponse(requestId: String): AuthenticationResponse {
        return buildAuthenticationResponse(requestId, AuthenticationStatus.REJECTED)
    }

    /**
     * Builds an AuthenticationResponse with UNKNOWN status.
     *
     * @param requestId the request ID
     * @return a new AuthenticationResponse with UNKNOWN status
     */
    fun buildUnknownResponse(requestId: String): AuthenticationResponse {
        return buildAuthenticationResponse(requestId, AuthenticationStatus.UNKNOWN)
    }
}
