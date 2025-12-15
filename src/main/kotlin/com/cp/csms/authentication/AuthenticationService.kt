package com.cp.csms.authentication

import com.cp.csms.common.AuthenticationStatus
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val tokenStatusProvider: TokenStatusProvider
) {

    fun authenticate(token: String): AuthenticationStatus {
        return tokenStatusProvider.isTokenEnabled(token)
            .map { tokenEnabled ->
                if (tokenEnabled) AuthenticationStatus.ACCEPTED
                else AuthenticationStatus.REJECTED
            }
            .orElse(AuthenticationStatus.UNKNOWN)
    }
}
