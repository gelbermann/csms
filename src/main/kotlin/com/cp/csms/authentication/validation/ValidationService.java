package com.cp.csms.authentication.validation;

import com.cp.csms.common.AuthenticationMessage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ValidationService {

    private final List<AuthenticationValidator> validators;

    public ValidationService(List<AuthenticationValidator> validators) {
        this.validators = validators;
    }

    public boolean isValid(AuthenticationMessage message) {
        return validators.stream()
                .allMatch(validator -> validator.validate(message));
    }
}
