package com.cp.csms.authentication

import org.springframework.stereotype.Service
import java.util.Optional

@Service
class TokenStatusProvider {

    fun isTokenEnabled(token: String): Optional<Boolean> {
        return Optional.ofNullable(tokenStatusMap[token])
    }

    companion object {
        private val tokenStatusMap: Map<String, Boolean> = mapOf(
            "DISABLED_suspended-account-driver-token-abc" to false,
            "DISABLED_token-blocked-user-456" to false,
            "DISABLED_expired-trial-driver-token-xyz" to false,
            
            "driverABC-1234567890" to true,
            "driver-token-xyz789-authenticated-user-session" to true,
            "ev-charging-driver-token-qwerty123456789" to true,
            "mobile-app-driver-id-987654321-active" to true,
            "verified-driver-session-token-abcdef123456" to true,
            "fleet-manager-driver-id-456789-premium-account-enabled" to true,
            "corporate-fleet-driver-token-long-format-id-12345678" to true,
            "public-charging-driver-session-token-uuid-format-enabled" to true
        )
    }
}
