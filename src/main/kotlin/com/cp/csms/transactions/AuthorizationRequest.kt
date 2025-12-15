package com.cp.csms.transactions

data class AuthorizationRequest(
    val stationUuid: String,
    val driverIdentifier: DriverIdentifier
)
