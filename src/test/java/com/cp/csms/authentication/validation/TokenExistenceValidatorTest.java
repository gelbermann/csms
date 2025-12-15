package com.cp.csms.authentication.validation;

import com.cp.csms.common.AuthenticationMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class TokenExistenceValidatorTest {

    private static final String REQUEST_ID = "req-123";

    @InjectMocks
    private TokenExistenceValidator validator;

    @Test
    void shouldReturnTrueWhenTokenExists() {
        final AuthenticationMessage message = new AuthenticationMessage(REQUEST_ID, "valid-token");

        final boolean result = validator.validate(message);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenMessageIsNull() {
        final boolean result = validator.validate(null);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenTokenIsNull() {
        final AuthenticationMessage message = new AuthenticationMessage(REQUEST_ID, null);

        final boolean result = validator.validate(message);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenTokenIsEmpty() {
        final AuthenticationMessage message = new AuthenticationMessage(REQUEST_ID, "");

        final boolean result = validator.validate(message);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenTokenIsBlank() {
        final AuthenticationMessage message = new AuthenticationMessage(REQUEST_ID, "   ");

        final boolean result = validator.validate(message);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenTokenIsOnlyWhitespace() {
        final AuthenticationMessage message = new AuthenticationMessage(REQUEST_ID, "\t\n\r");

        final boolean result = validator.validate(message);

        assertThat(result).isFalse();
    }
}
