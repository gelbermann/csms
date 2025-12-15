package com.cp.csms.common;

public class AuthenticationMessage {

    private final String requestId;
    private final String token;

    public AuthenticationMessage(String requestId, String token) {
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
