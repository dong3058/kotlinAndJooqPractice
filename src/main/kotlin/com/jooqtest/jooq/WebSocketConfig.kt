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

        //val properties= mapOf("x-message-ttl" to 300000,"x-single-active-consumer" to true)
        //참고사항으로 rabbit mq를 외부 메시지 브로커로 사용해서 구독시에 만약 해당 큐가 이미 존재가고 저런 위의 properties
        //설정을 지닌다면 subscribe헤더에 저옵션들을 같이 넣어줘야된다.
    }
}