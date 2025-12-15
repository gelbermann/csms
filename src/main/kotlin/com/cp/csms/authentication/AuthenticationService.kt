package com.cp.csms.authentication;

import com.cp.csms.common.AuthenticationStatus;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final TokenStatusProvider tokenStatusProvider;

    public AuthenticationService(TokenStatusProvider tokenStatusProvider) {
        this.tokenStatusProvider = tokenStatusProvider;
    }

    public AuthenticationStatus authenticate(String token) {
        return tokenStatusProvider.isTokenEnabled(token)
                .map(tokenEnabled -> tokenEnabled
                        ? AuthenticationStatus.ACCEPTED
                        : AuthenticationStatus.REJECTED)
                .orElse(AuthenticationStatus.UNKNOWN);
    }
}
