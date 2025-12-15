package com.cp.csms.authentication.validation

import com.cp.csms.common.AuthenticationMessage
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(1)
class TokenExistenceValidator : AuthenticationValidator {

    override fun validate(message: AuthenticationMessage?): Boolean {
        return message?.token?.isNotBlank() ?: false
    }
}
