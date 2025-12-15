package com.cp.csms.authentication.validation

import com.cp.csms.common.AuthenticationMessage
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
class TokenLengthValidator : AuthenticationValidator {

    override fun validate(message: AuthenticationMessage?): Boolean {
        val tokenLength = message?.token?.length ?: 0
        return tokenLength in MIN_TOKEN_LENGTH..MAX_TOKEN_LENGTH
    }

    companion object {
        private const val MIN_TOKEN_LENGTH = 20
        private const val MAX_TOKEN_LENGTH = 80
    }
}
