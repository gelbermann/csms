package com.cp.csms.authentication.validation;

import com.cp.csms.common.AuthenticationMessage;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
public class TokenLengthValidator implements AuthenticationValidator {

    private static final int MIN_TOKEN_LENGTH = 20;
    private static final int MAX_TOKEN_LENGTH = 80;

    @Override
    public boolean validate(AuthenticationMessage message) {
        final String token = message.getToken();
        return token.length() >= MIN_TOKEN_LENGTH 
            && token.length() <= MAX_TOKEN_LENGTH;
    }
}
