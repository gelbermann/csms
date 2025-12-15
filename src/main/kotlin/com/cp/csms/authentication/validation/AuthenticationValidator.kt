package com.cp.csms.authentication.validation

import com.cp.csms.common.AuthenticationMessage

interface AuthenticationValidator {
    fun validate(message: AuthenticationMessage?): Boolean
}
