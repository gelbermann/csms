package com.cp.csms.authentication;

import com.cp.csms.authentication.validation.ValidationService;
import com.cp.csms.common.AuthenticationMessage;
import com.cp.csms.common.AuthenticationResponse;
import com.cp.csms.common.AuthenticationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationConsumer.class);

    private final ValidationService validationService;
    private final AuthenticationService authenticationService;

    public AuthenticationConsumer(
            ValidationService validationService,
            AuthenticationService authenticationService) {
        this.validationService = validationService;
        this.authenticationService = authenticationService;
    }

    @KafkaListener(
            topics = "${kafka.topics.auth-request}",
            groupId = "${kafka.consumer.authentication-service.group-id}",
            containerFactory = "authRequestKafkaListenerContainerFactory"
    )
    @SendTo("${kafka.topics.auth-response}")
    public AuthenticationResponse handleAuthRequest(AuthenticationMessage message) {
        log.info("Received authentication request: {}", message);

        if (!validationService.isValid(message)) {
            log.warn("Validation failed for token: {}", message.getToken());
            return new AuthenticationResponse(
                    message.getRequestId(),
                    AuthenticationStatus.INVALID
            );
        }

        final AuthenticationStatus authenticationStatus = 
                authenticationService.authenticate(message.getToken());
        return new AuthenticationResponse(
                message.getRequestId(),
                authenticationStatus
        );
    }

}
