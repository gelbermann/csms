package com.cp.csms.common

data class AuthenticationResponse(
    val requestId: String,
    val status: AuthenticationStatus
)
