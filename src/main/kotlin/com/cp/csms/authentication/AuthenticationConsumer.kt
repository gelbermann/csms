package com.cp.csms.authentication

import com.cp.csms.authentication.validation.ValidationService
import com.cp.csms.common.AuthenticationMessage
import com.cp.csms.common.AuthenticationResponse
import com.cp.csms.common.AuthenticationStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Service

@Service
class AuthenticationConsumer(
    private val validationService: ValidationService,
    private val authenticationService: AuthenticationService
) {

    @KafkaListener(
        topics = ["\${kafka.topics.auth-request}"],
        groupId = "\${kafka.consumer.authentication-service.group-id}",
        containerFactory = "authRequestKafkaListenerContainerFactory"
    )
    @SendTo("\${kafka.topics.auth-response}")
    fun handleAuthRequest(message: AuthenticationMessage): AuthenticationResponse {
        log.info("Received authentication request: {}", message)

        if (!validationService.isValid(message)) {
            log.warn("Validation failed for token: {}", message.token)
            return AuthenticationResponse(
                message.requestId ?: "unknown",
                AuthenticationStatus.INVALID
            )
        }

        val authenticationStatus = authenticationService.authenticate(message.token ?: "")
        return AuthenticationResponse(
            message.requestId ?: "unknown",
            authenticationStatus
        )
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(AuthenticationConsumer::class.java)
    }
}
