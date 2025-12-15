package com.cp.csms.authentication;

import com.cp.csms.common.AuthenticationMessage;
import com.cp.csms.common.AuthenticationResponse;
import com.cp.csms.common.AuthenticationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationConsumer.class);

    private static final int MIN_TOKEN_LENGTH = 20;
    private static final int MAX_TOKEN_LENGTH = 80;

    private final TokenStatusProvider tokenStatusProvider;

    public AuthenticationConsumer(TokenStatusProvider tokenStatusProvider) {
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

        final String token = message.getToken();
        if (!validateToken(token)) {
            log.warn("Invalid token format: {}", token);
            return new AuthenticationResponse(
                    message.getRequestId(),
                    AuthenticationStatus.INVALID
            );
        }

        final AuthenticationStatus authenticationStatus = tokenStatusProvider.isTokenEnabled(token)
                .map(tokenEnabled -> tokenEnabled
                        ? AuthenticationStatus.ACCEPTED
                        : AuthenticationStatus.REJECTED)
                .orElse(AuthenticationStatus.UNKNOWN);

        return new AuthenticationResponse(
                message.getRequestId(),
                authenticationStatus
        );
    }

    private boolean validateToken(String token) {
        return Optional.ofNullable(token)
                .filter(value -> value.length() >= MIN_TOKEN_LENGTH)
                .filter(value -> value.length() <= MAX_TOKEN_LENGTH)
                .isPresent();
    }


}
