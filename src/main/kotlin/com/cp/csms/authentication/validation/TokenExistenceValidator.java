package com.cp.csms.authentication.validation;

import com.cp.csms.common.AuthenticationMessage;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class TokenExistenceValidator implements AuthenticationValidator {

    @Override
    public boolean validate(AuthenticationMessage message) {
        return message != null 
            && message.getToken() != null 
            && !message.getToken().isBlank();
    }
}
