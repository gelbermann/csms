package com.cp.csms.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthenticationResponse {

    private final String requestId;
    private final AuthenticationStatus status;

    @JsonCreator
    public AuthenticationResponse(
            @JsonProperty("requestId") String requestId,
            @JsonProperty("status") AuthenticationStatus status) {
        this.requestId = requestId;
        this.status = status;
    }

    public String getRequestId() {
        return requestId;
    }

    public AuthenticationStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "AuthenticationResponse{" +
                "requestId='" + requestId + '\'' +
                ", status=" + status +
                '}';
    }
}
