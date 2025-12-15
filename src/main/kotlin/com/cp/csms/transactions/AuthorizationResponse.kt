package com.cp.csms.transactions;

import com.cp.csms.common.AuthenticationStatus;

public class AuthorizationResponse {

    private final AuthenticationStatus authenticationStatus;

    public AuthorizationResponse(AuthenticationStatus authenticationStatus) {
        this.authenticationStatus = authenticationStatus;
    }

    public AuthenticationStatus getAuthenticationStatus() {
        return authenticationStatus;
    }

    @Override
    public String toString() {
        return "AuthorizationResponse{" +
                "authenticationStatus=" + authenticationStatus +
                '}';
    }
}
