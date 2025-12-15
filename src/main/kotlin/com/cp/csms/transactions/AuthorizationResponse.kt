package com.cp.csms.transactions

import com.cp.csms.common.AuthenticationStatus

data class AuthorizationResponse(
    val authenticationStatus: AuthenticationStatus
)
