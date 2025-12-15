package com.cp.csms.transactions.controllers;

import com.cp.csms.common.AuthenticationMessage;
import com.cp.csms.common.AuthenticationResponse;
import com.cp.csms.common.AuthenticationStatus;
import com.cp.csms.config.KafkaTopicConfig;
import com.cp.csms.transactions.AuthorizationRequest;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class AuthorizationService {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationService.class);

    private static final int CACHE_MAX_SIZE = 10000;
    private static final int CACHE_EXPIRE_MINUTES = 10;

    private final int timeoutSeconds;

    private final Cache<String, CompletableFuture<AuthenticationResponse>> pendingRequests;

    private final KafkaTemplate<String, AuthenticationMessage> kafkaProducer;

    private final KafkaTopicConfig kafkaTopicConfig;

    public AuthorizationService(KafkaTemplate<String, AuthenticationMessage> kafkaProducer,
                                KafkaTopicConfig kafkaTopicConfig,
                                @Value("${transaction-service.authorization.timeout-seconds}") int timeoutSeconds) {
        this.kafkaProducer = kafkaProducer;
        this.kafkaTopicConfig = kafkaTopicConfig;
        this.timeoutSeconds = timeoutSeconds;
        this.pendingRequests = Caffeine.newBuilder()
                .maximumSize(CACHE_MAX_SIZE)
                .expireAfterWrite(CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES)
                .removalListener((key, value, cause) -> {
                    if (value instanceof CompletableFuture<?> future && !future.isDone()) {
                        log.warn("Pending request evicted from cache: requestId={}, cause={}", key, cause);
                    }
                })
                .build();
    }

    public AuthenticationStatus authorize(AuthorizationRequest request) throws TimeoutException {
        final String requestId = UUID.randomUUID().toString();
        final CompletableFuture<AuthenticationResponse> future = new CompletableFuture<>();

        pendingRequests.put(requestId, future);

        final AuthenticationMessage message = new AuthenticationMessage(
                requestId,
                request.getDriverIdentifier().getId()
        );

        kafkaProducer.send(kafkaTopicConfig.getAuthRequestTopic(), requestId, message);

        try {
            final AuthenticationResponse response = future.get(timeoutSeconds, TimeUnit.SECONDS);
            return response.getStatus();
        } catch (TimeoutException e) {
            log.warn("Authorization request timed out for requestId: {}", requestId);
            throw e;
        } catch (Exception e) {
            log.error("Error while authorizing requestId: {}", requestId, e);
            throw new RuntimeException(e);
        } finally {
            pendingRequests.invalidate(requestId);
        }
    }

    @KafkaListener(
            topics = "${kafka.topics.auth-response}",
            groupId = "${kafka.consumer.transaction-service.group-id}",
            containerFactory = "authResponseKafkaListenerContainerFactory"
    )
    public void handleAuthResponse(AuthenticationResponse response) {
        final CompletableFuture<AuthenticationResponse> future = pendingRequests.getIfPresent(response.getRequestId());

        if (future != null && !future.isDone()) {
            future.complete(response);
            pendingRequests.invalidate(response.getRequestId());
        } else if (future == null) {
            log.warn("Received response for unknown or expired requestId: {}", response.getRequestId());
        }
    }

}
