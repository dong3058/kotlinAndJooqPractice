package com.jooqtest.jooq


import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(private val channelInterceptor:CustomChannelInterCeptor
,private val outBoundHandler: OutBoundHandler) : WebSocketMessageBrokerConfigurer {


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
        //client과 stomp간의 즉 기존의 인메모리 브로커시 heartbeat와는 다르게
        //rabbit mq 를 외부브로커로 사용해서 heartbeat를 설정할려면 outboundhander,customchannelinterceptor
        //처럼 들어가고,나오는 connect 메시지의 heartbeat값을 조정해줘야된다.--> 반드시 둘다 해줘야되며 해당 코드 참조바람.
        //이렇게하면 heartbeat를 처리하는(client->spring->rabbitmq) 스레드를 정의할필요없다. 왜냐면 spring은
        //그냥 중계기이기떄문.
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableStompBrokerRelay("/queue")
            .setRelayHost(HOSTNAME)
            .setRelayPort(61613)
            //.setSystemHeartbeatSendInterval()
            //.setSystemHeartbeatReceiveInterval(\)
            //.setTaskScheduler()
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

        //엄밀하게 말하면 rabbitmq template같이 직접 rabbitmq에대한 접근이 필요없을 경우
        //위의 메시지 브로커 설정만 해주면 rabbitmq 의 stomp 포트인 61613으로 연결된다.-->spring과 rabbitmq stomp간의 tcp 커낵션(1개)를 연결한다 이말.
        //즉 rabbit mq에대한 저수준 연결을 설정해줄필요가없음.

        //.setSystemHeartbeatSendInterval()
        //.setSystemHeartbeatReceiveInterval()
        //이 2개 옵션은 spring 과 rabbit mq의61613이라는 rabbit stomp 랑 heartbeat를 주고받는것이다.
        //.setTaskScheduler()-->이거는 spring-rabbit mq stomp간의 heartbet를 위한것. tcp연결이 1개니까 스레드는 1~2개정도만?

    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(channelInterceptor)
    }

    override fun configureClientOutboundChannel(registration: ChannelRegistration) {
        registration.interceptors(outBoundHandler)
    }
}