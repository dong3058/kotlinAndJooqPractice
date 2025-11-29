package com.jooqtest.jooq


import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {


    @Value("\${spring.rabbitmq.host}")
    private val HOSTNAME: String=""

    @Value("\${spring.rabbitmq.port}")
    private val PORT: Int=0

    @Value("\${spring.rabbitmq.username}")
    private val USERNAME: String=""

    @Value("\${spring.rabbitmq.password}")
    private val PASSWORD: String=""


    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS();
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableStompBrokerRelay("/queue")
            .setRelayHost(HOSTNAME)
            .setRelayPort(61613)
            .setClientLogin(USERNAME)
            .setClientPasscode(PASSWORD);
        registry.setApplicationDestinationPrefixes("/app")
    }
}