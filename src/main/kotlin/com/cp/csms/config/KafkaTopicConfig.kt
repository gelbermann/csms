package com.cp.csms.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class KafkaTopicConfig(
    @Value("\${kafka.topics.auth-request}")
    val authRequestTopic: String,
    
    @Value("\${kafka.topics.auth-response}")
    val authResponseTopic: String,
    
    @Value("\${kafka.consumer.transaction-service.group-id}")
    val transactionServiceGroupId: String
)
