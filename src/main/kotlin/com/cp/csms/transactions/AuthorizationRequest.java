package com.cp.csms.transactions;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthorizationRequest {

    String stationUuid;
    DriverIdentifier driverIdentifier;

}