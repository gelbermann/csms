package com.cp.csms.authentication;

import com.cp.csms.authentication.validation.AuthenticationValidator;
import com.cp.csms.common.AuthenticationMessage;
import com.cp.csms.common.AuthenticationResponse;
import com.cp.csms.common.AuthenticationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthenticationConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationConsumer.class);

    private final List<AuthenticationValidator> validators;
    private final TokenStatusProvider tokenStatusProvider;

    public AuthenticationConsumer(List<AuthenticationValidator> validators,
                                  TokenStatusProvider tokenStatusProvider) {
        this.validators = validators;
        this.tokenStatusProvider = tokenStatusProvider;
    }

    @KafkaListener(
            topics = "${kafka.topics.auth-request}",
            groupId = "${kafka.consumer.authentication-service.group-id}",
            containerFactory = "authRequestKafkaListenerContainerFactory"
    )
    @SendTo("${kafka.topics.auth-response}")
    public AuthenticationResponse handleAuthRequest(AuthenticationMessage message) {
        log.info("Received authentication request: {}", message);

        final boolean isValid = validators.stream()
                .allMatch(validator -> validator.validate(message));

        if (!isValid) {
            log.warn("Validation failed for token: {}", message.getToken());
            return new AuthenticationResponse(
                    message.getRequestId(),
                    AuthenticationStatus.INVALID
            );
        }

        final AuthenticationStatus authenticationStatus = tokenStatusProvider.isTokenEnabled(message.getToken())
                .map(tokenEnabled -> tokenEnabled
                        ? AuthenticationStatus.ACCEPTED
                        : AuthenticationStatus.REJECTED)
                .orElse(AuthenticationStatus.UNKNOWN);

        return new AuthenticationResponse(
                message.getRequestId(),
                authenticationStatus
        );
    }

}
