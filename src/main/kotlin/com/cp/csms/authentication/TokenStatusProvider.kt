package com.cp.csms.authentication;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;

@Service
public class TokenStatusProvider {

    private final static Map<String, Boolean> tokenStatusMap = Map.ofEntries(
            entry("DISABLED_suspended-account-driver-token-abc", false),
            entry("DISABLED_token-blocked-user-456", false),
            entry("DISABLED_expired-trial-driver-token-xyz", false),

            entry("driverABC-1234567890", true),
            entry("driver-token-xyz789-authenticated-user-session", true),
            entry("ev-charging-driver-token-qwerty123456789", true),
            entry("mobile-app-driver-id-987654321-active", true),
            entry("verified-driver-session-token-abcdef123456", true),
            entry("fleet-manager-driver-id-456789-premium-account-enabled", true),
            entry("corporate-fleet-driver-token-long-format-id-12345678", true),
            entry("public-charging-driver-session-token-uuid-format-enabled", true)
    );

    public Optional<Boolean> isTokenEnabled(String token) {
        return Optional.ofNullable(tokenStatusMap.get(token));
    }

}
