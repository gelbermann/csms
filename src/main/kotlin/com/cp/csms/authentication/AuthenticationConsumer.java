package com.cp.csms.authentication;

import com.cp.csms.authentication.validation.AuthenticationValidator;
import com.cp.csms.common.AuthenticationMessage;
import com.cp.csms.common.AuthenticationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationConsumer.class);

    @KafkaListener(
            topics = "${kafka.topics.auth-request}",
            groupId = "${kafka.consumer.authentication-service.group-id}",
            containerFactory = "authRequestKafkaListenerContainerFactory"
    )
    @SendTo("${kafka.topics.auth-response}")
    public AuthenticationResponse handleAuthRequest(AuthenticationMessage message) {
        log.info("Received authentication request: {}", message);
        // TODO: Implement authentication logic
    }
}
