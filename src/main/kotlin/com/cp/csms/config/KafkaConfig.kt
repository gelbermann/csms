package com.cp.csms.config

import com.cp.csms.common.AuthenticationMessage
import com.cp.csms.common.AuthenticationResponse
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer

@EnableKafka
@Configuration
class KafkaConfig(
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String,
    
    @Value("\${spring.kafka.consumer.auto-offset-reset}")
    private val autoOffsetReset: String,
    
    @Value("\${spring.kafka.consumer.enable-auto-commit}")
    private val enableAutoCommit: Boolean
) {

    @Bean
    @Suppress("DEPRECATION") // JsonDeserializer deprecated in Spring Kafka 4.0, but still functional
    fun authenticationResponseConsumerFactory(): ConsumerFactory<String, AuthenticationResponse> {
        val configProps = mapOf<String, Any>(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer::class.java,
            ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS to JsonDeserializer::class.java.name,
            JsonDeserializer.VALUE_DEFAULT_TYPE to AuthenticationResponse::class.java.name,
            JsonDeserializer.TRUSTED_PACKAGES to "com.cp.csms.common",
            JsonDeserializer.USE_TYPE_INFO_HEADERS to false,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to autoOffsetReset,
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to enableAutoCommit
        )
        
        return DefaultKafkaConsumerFactory(configProps)
    }

    @Bean
    fun authResponseKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, AuthenticationResponse> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, AuthenticationResponse>()
        factory.setConsumerFactory(authenticationResponseConsumerFactory())
        return factory
    }

    @Bean
    @Suppress("DEPRECATION") // JsonDeserializer deprecated in Spring Kafka 4.0, but still functional
    fun authenticationMessageConsumerFactory(): ConsumerFactory<String, AuthenticationMessage> {
        val configProps = mapOf<String, Any>(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer::class.java,
            ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS to JsonDeserializer::class.java.name,
            JsonDeserializer.VALUE_DEFAULT_TYPE to AuthenticationMessage::class.java.name,
            JsonDeserializer.TRUSTED_PACKAGES to "com.cp.csms.common",
            JsonDeserializer.USE_TYPE_INFO_HEADERS to false,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to autoOffsetReset,
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to enableAutoCommit
        )
        
        return DefaultKafkaConsumerFactory(configProps)
    }

    @Bean
    fun authRequestKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, AuthenticationMessage> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, AuthenticationMessage>()
        factory.setConsumerFactory(authenticationMessageConsumerFactory())
        factory.setReplyTemplate(authenticationResponseKafkaTemplate())
        return factory
    }

    @Bean
    fun authenticationResponseProducerFactory(): ProducerFactory<String, AuthenticationResponse> {
        val configProps = mapOf<String, Any>(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )
        return DefaultKafkaProducerFactory(configProps)
    }

    @Bean
    fun authenticationResponseKafkaTemplate(): KafkaTemplate<String, AuthenticationResponse> {
        return KafkaTemplate(authenticationResponseProducerFactory())
    }

    @Bean
    fun authenticationMessageProducerFactory(): ProducerFactory<String, AuthenticationMessage> {
        val configProps = mapOf<String, Any>(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )
        return DefaultKafkaProducerFactory(configProps)
    }

    @Bean
    fun authenticationMessageKafkaTemplate(): KafkaTemplate<String, AuthenticationMessage> {
        return KafkaTemplate(authenticationMessageProducerFactory())
    }
}
