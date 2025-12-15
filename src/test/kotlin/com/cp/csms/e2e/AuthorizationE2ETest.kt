package com.cp.csms.e2e

import com.cp.csms.common.AuthenticationStatus
import com.cp.csms.e2e.support.TestDataBuilder
import com.cp.csms.transactions.AuthorizationRequest
import com.cp.csms.transactions.AuthorizationResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

/**
 * E2E tests for the authorization flow.
 * Tests the complete flow from API request through Kafka messaging to API response.
 */
class AuthorizationE2ETest : BaseE2ETest() {

    @Test
    fun `should complete full authorization flow when token accepted`() {
        val request = TestDataBuilder.buildAuthorizationRequest(
            "station-123",
            "driverABC-1234567890"
        )

        val response: ResponseEntity<AuthorizationResponse> = restTemplate.postForEntity(
            getAuthorizationUrl(),
            request,
            AuthorizationResponse::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
        assertThat(response.body?.authenticationStatus).isEqualTo(AuthenticationStatus.ACCEPTED)
    }

    @Test
    fun `should return rejected status when token disabled`() {
        val request = TestDataBuilder.buildAuthorizationRequest(
            "station-123",
            "DISABLED_suspended-account-driver-token-abc"
        )

        val response: ResponseEntity<AuthorizationResponse> = restTemplate.postForEntity(
            getAuthorizationUrl(),
            request,
            AuthorizationResponse::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
        assertThat(response.body?.authenticationStatus).isEqualTo(AuthenticationStatus.REJECTED)
    }

    @Test
    fun `should return unknown status when token not found`() {
        val request = TestDataBuilder.buildAuthorizationRequest(
            "station-123",
            "non-existent-token-xyz"
        )

        val response: ResponseEntity<AuthorizationResponse> = restTemplate.postForEntity(
            getAuthorizationUrl(),
            request,
            AuthorizationResponse::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
        assertThat(response.body?.authenticationStatus).isEqualTo(AuthenticationStatus.UNKNOWN)
    }

    @Test
    fun `should return invalid status when validation fails`() {
        val request = TestDataBuilder.buildAuthorizationRequest(
            "station-123",
            null
        )

        val response: ResponseEntity<AuthorizationResponse> = restTemplate.postForEntity(
            getAuthorizationUrl(),
            request,
            AuthorizationResponse::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
        assertThat(response.body?.authenticationStatus).isEqualTo(AuthenticationStatus.INVALID)
    }
}
