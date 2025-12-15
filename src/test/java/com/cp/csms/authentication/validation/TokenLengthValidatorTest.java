package com.cp.csms.authentication.validation;

import com.cp.csms.common.AuthenticationMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TokenLengthValidatorTest {

    private static final String REQUEST_ID = "req-123";

    @InjectMocks
    private TokenLengthValidator validator;

    @Test
    void shouldReturnTrueWhenTokenLengthIsMinimum() {
        final String token = "a".repeat(20);
        final AuthenticationMessage message = new AuthenticationMessage(REQUEST_ID, token);

        final boolean result = validator.validate(message);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnTrueWhenTokenLengthIsMaximum() {
        final String token = "a".repeat(80);
        final AuthenticationMessage message = new AuthenticationMessage(REQUEST_ID, token);

        final boolean result = validator.validate(message);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnTrueWhenTokenLengthIsWithinRange() {
        final String token = "a".repeat(50);
        final AuthenticationMessage message = new AuthenticationMessage(REQUEST_ID, token);

        final boolean result = validator.validate(message);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenTokenIsTooShort() {
        final String token = "a".repeat(19);
        final AuthenticationMessage message = new AuthenticationMessage(REQUEST_ID, token);

        final boolean result = validator.validate(message);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenTokenIsTooLong() {
        final String token = "a".repeat(81);
        final AuthenticationMessage message = new AuthenticationMessage(REQUEST_ID, token);

        final boolean result = validator.validate(message);

        assertThat(result).isFalse();
    }

}
