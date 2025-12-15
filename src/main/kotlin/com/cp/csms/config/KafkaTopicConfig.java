package com.cp.csms.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topics.auth-request}")
    private String authRequestTopic;

    @Value("${kafka.topics.auth-response}")
    private String authResponseTopic;

    @Value("${kafka.consumer.transaction-service.group-id}")
    private String transactionServiceGroupId;
}
