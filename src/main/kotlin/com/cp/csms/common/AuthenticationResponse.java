package com.cp.csms.common;

public class AuthenticationResponse {

    private final String requestId;
    private final AuthenticationStatus status;

    public AuthenticationResponse(String requestId, AuthenticationStatus status) {
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
