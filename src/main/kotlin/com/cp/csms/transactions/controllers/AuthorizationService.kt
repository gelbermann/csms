package com.cp.csms.transactions.controllers

import com.cp.csms.common.AuthenticationMessage
import com.cp.csms.common.AuthenticationResponse
import com.cp.csms.common.AuthenticationStatus
import com.cp.csms.config.KafkaTopicConfig
import com.cp.csms.transactions.AuthorizationRequest
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@Service
class AuthorizationService(
    private val kafkaProducer: KafkaTemplate<String, AuthenticationMessage>,
    private val kafkaTopicConfig: KafkaTopicConfig,
    @Value("\${transaction-service.authorization.timeout-seconds}") private val timeoutSeconds: Int
) {

    private val pendingRequests: Cache<String, CompletableFuture<AuthenticationResponse>> = Caffeine.newBuilder()
        .maximumSize(CACHE_MAX_SIZE.toLong())
        .expireAfterWrite(CACHE_EXPIRE_MINUTES.toLong(), TimeUnit.MINUTES)
        .removalListener<String, CompletableFuture<AuthenticationResponse>> { key, value, cause ->
            if (value != null && !value.isDone) {
                log.warn("Pending request evicted from cache: requestId={}, cause={}", key, cause)
            }
        }
        .build()

    @Throws(TimeoutException::class)
    fun authorize(request: AuthorizationRequest): AuthenticationStatus {
        val requestId = UUID.randomUUID().toString()
        val future = CompletableFuture<AuthenticationResponse>()

        pendingRequests.put(requestId, future)

        val message = AuthenticationMessage(
            requestId,
            request.driverIdentifier.id
        )

        kafkaProducer.send(kafkaTopicConfig.authRequestTopic, requestId, message)

        return try {
            val response = future.get(timeoutSeconds.toLong(), TimeUnit.SECONDS)
            response.authenticationStatus
        } catch (e: TimeoutException) {
            log.warn("Authorization request timed out for requestId: {}", requestId)
            throw e
        } catch (e: Exception) {
            log.error("Error while authorizing requestId: {}", requestId, e)
            throw RuntimeException(e)
        } finally {
            pendingRequests.invalidate(requestId)
        }
    }

    @KafkaListener(
        topics = ["\${kafka.topics.auth-response}"],
        groupId = "\${kafka.consumer.transaction-service.group-id}",
        containerFactory = "authResponseKafkaListenerContainerFactory"
    )
    fun handleAuthResponse(response: AuthenticationResponse) {
        val future = pendingRequests.getIfPresent(response.requestId)

        if (future != null && !future.isDone) {
            future.complete(response)
            pendingRequests.invalidate(response.requestId)
        } else if (future == null) {
            log.warn("Received response for unknown or expired requestId: {}", response.requestId)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(AuthorizationService::class.java)
        private const val CACHE_MAX_SIZE = 10000
        private const val CACHE_EXPIRE_MINUTES = 10
    }
}
