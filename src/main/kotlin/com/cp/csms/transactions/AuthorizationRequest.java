package com.cp.csms.transactions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthorizationRequest {

    private final String stationUuid;
    private final DriverIdentifier driverIdentifier;

    @JsonCreator
    public AuthorizationRequest(
            @JsonProperty("stationUuid") String stationUuid,
            @JsonProperty("driverIdentifier") DriverIdentifier driverIdentifier) {
        this.stationUuid = stationUuid;
        this.driverIdentifier = driverIdentifier;
    }

    public String getStationUuid() {
        return stationUuid;
    }

    public DriverIdentifier getDriverIdentifier() {
        return driverIdentifier;
    }

    @Override
    public String toString() {
        return "AuthorizationRequest{" +
                "stationUuid='" + stationUuid + '\'' +
                ", driverIdentifier=" + driverIdentifier +
                '}';
    }
}