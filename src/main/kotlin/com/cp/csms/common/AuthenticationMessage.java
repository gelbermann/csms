package com.cp.csms.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthenticationMessage {

    private final String requestId;
    private final String token;

    @JsonCreator
    public AuthenticationMessage(
            @JsonProperty("requestId") String requestId,
            @JsonProperty("token") String token) {
        this.requestId = requestId;
        this.token = token;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getToken() {
        return token;
    }

    @Override
    public String toString() {
        return "AuthenticationMessage{" +
                "requestId='" + requestId + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
