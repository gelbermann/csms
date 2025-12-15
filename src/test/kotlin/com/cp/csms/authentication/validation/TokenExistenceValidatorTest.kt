package com.cp.csms.authentication.validation

import com.cp.csms.common.AuthenticationMessage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class TokenExistenceValidatorTest {

    @InjectMocks
    private lateinit var validator: TokenExistenceValidator

    @Test
    fun `should return true when token exists`() {
        val message = AuthenticationMessage(REQUEST_ID, "valid-token")

        val result = validator.validate(message)

        assertThat(result).isTrue()
    }

    @Test
    fun `should return false when message is null`() {
        val result = validator.validate(null)

        assertThat(result).isFalse()
    }

    @Test
    fun `should return false when token is null`() {
        val message = AuthenticationMessage(REQUEST_ID, null)

        val result = validator.validate(message)

        assertThat(result).isFalse()
    }

    @Test
    fun `should return false when token is empty`() {
        val message = AuthenticationMessage(REQUEST_ID, "")

        val result = validator.validate(message)

        assertThat(result).isFalse()
    }

    @Test
    fun `should return false when token is blank`() {
        val message = AuthenticationMessage(REQUEST_ID, "   ")

        val result = validator.validate(message)

        assertThat(result).isFalse()
    }

    @Test
    fun `should return true when token has leading and trailing spaces but is not blank`() {
        val message = AuthenticationMessage(REQUEST_ID, "  valid-token  ")

        val result = validator.validate(message)

        assertThat(result).isTrue()
    }

    companion object {
        private const val REQUEST_ID = "req-123"
    }
}
