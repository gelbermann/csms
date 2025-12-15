package com.cp.csms.authentication.validation;

import com.cp.csms.common.AuthenticationMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidationServiceTest {

    private static final String REQUEST_ID = "req-123";

    @Mock
    private AuthenticationValidator validator1;

    @Mock
    private AuthenticationValidator validator2;

    @Test
    void shouldReturnTrueWhenAllValidatorsPass() {
        final AuthenticationMessage message = new AuthenticationMessage(REQUEST_ID, "valid-token");
        when(validator1.validate(message)).thenReturn(true);
        when(validator2.validate(message)).thenReturn(true);

        final ValidationService validationService = new ValidationService(List.of(validator1, validator2));

        final boolean result = validationService.isValid(message);

        assertThat(result).isTrue();
    }

    @Test
    void shouldShortCircuitOnFirstFailure() {
        final AuthenticationMessage message = new AuthenticationMessage(REQUEST_ID, "invalid-token");

        final AuthenticationValidator failingValidator = msg -> false;
        final AuthenticationValidator throwingValidator = msg -> {
            throw new RuntimeException("Should not be called due to short-circuit");
        };

        final ValidationService shortCircuitService =
                new ValidationService(List.of(failingValidator, throwingValidator));

        final boolean result = shortCircuitService.isValid(message);

        assertThat(result).isFalse();
    }
}
