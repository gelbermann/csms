package com.cp.csms.authentication.validation

import com.cp.csms.common.AuthenticationMessage
import org.springframework.stereotype.Service

@Service
class ValidationService(
    private val validators: List<AuthenticationValidator>
) {

    fun isValid(message: AuthenticationMessage): Boolean {
        return validators.all { it.validate(message) }
    }
}
