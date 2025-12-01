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
        //relayport--> rabbitmq stomp의 포트인 61613에 등록하는것.--> client 의 stomp subscribe를 큐와 연결해준다.
        // 이렇게 연결을 해두면 rabbit template로 메시지 전송시 해당 메시지가 가야될 큐를 보고, 해당 큐가 subscribe가 된상태라면
        //알아서 자동으로 websocket의 메시지 역할 전송을 대신처리 해준다.
        //이떄 rabit template은 61613이 아니라 보통의 rabbit port로 등록된 포트여도 알아서 처리해준다.
        //또한 rabbit template가 아니라 simpmessageTemplate로 구독 경로로 메시지를 보내도 rabbit template가 알아서 처리한다.
        //단 simpmessagetemplate를 쓰면 rabbit template에 대한 confirm,returns callback을 쓸수없다.
    }
}