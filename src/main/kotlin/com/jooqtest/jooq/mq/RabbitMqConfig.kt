package com.jooqtest.jooq.mq


import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class RabbitMqConfig {


    @Value("\${spring.rabbitmq.host}")
    private val HOSTNAME: String=""

    @Value("\${spring.rabbitmq.port}")
    private val PORT: Int=0

    @Value("\${spring.rabbitmq.username}")
    private val USERNAME: String=""

    @Value("\${spring.rabbitmq.password}")
    private val PASSWORD: String=""
    @Bean
    fun connectionFactory(): ConnectionFactory {
        val connectionFactory = CachingConnectionFactory(HOSTNAME, PORT)
        connectionFactory.username = USERNAME
        connectionFactory.setRequestedHeartBeat(30)
        connectionFactory.setConnectionLimit(5000)

        //아래 2개의 설정은 각각 confirm 하고 returns 콜백의 활성화 여부를 결정하는 옵션.
        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED)
        connectionFactory.setPublisherReturns(true)
        return connectionFactory
    }
    @Bean
    fun rabbitAdmin(): RabbitAdmin {
        return RabbitAdmin(connectionFactory())
    }
    @Bean
    //rabbit mq의경우 따로 producer객체를 만든다기보단 rabbittemplate로 convertandsend메서드 사용시 exchangename,routekey를 넣어서실행함.
    //즉 이자체가 producer이다.
            /*
        * [Application]
        ↓
    convertAndSend("exchange", "routing-key", "message")
        ↓
    [RabbitMQ Broker] ← Confirm: 여기 도착 ✅
        ↓
    [Exchange]
        ↓
    라우팅 시도
        ↓
    [Queue] ← 여기 도달 실패!
        ↓
    setReturnsCallback 호출 ← 반환 사유 알려줌 */
    fun rabbitTemplate(): RabbitTemplate {
        val rabbitTemplate = RabbitTemplate(connectionFactory())
        rabbitTemplate.messageConverter = Jackson2JsonMessageConverter()
        rabbitTemplate.setMandatory(true)// 이거는 문제 발생시(큐에 도달 못하거나, 브로커로 못넘어갓을때) producer에게에러 반환한다이말.
        //이값으 true여야만 returns가 제대로 작동
        //returns callback은 큐도달 실패라거나 혹은 큐의 용량이 가득찬 케이스에 발생한 에러를 캐치한다.
        //참고로 큐생성시 overflow설정을 rejectpublish로 해줘야된다-->그래야 용량이 가득찬걸 캐치함.
        // 이런 에러들을 처리:
        // 1. 큐가 존재하지 않음
        // 2. 잘못된 라우팅 키
        // 3. 큐가 가득 참 (rejectPublish)
        // 4. 바인딩이 없음

        //반대로 try catch로 rabbittemplate를 덮어씌워야 캐치하는 에러는
        /*
        *     // 1. Exchange가 존재하지 않음
    // 2. 메시지 크기 초과
    // 3. 권한 문제
    * -->이 3개의 에러는 어디서 캐치하는지 잘모르겠다. 우선 코드상으로는 exchange 문제는 confirm에서 캐치중인거같은대
    * */
        rabbitTemplate.setConfirmCallback{data,ack,cause->
            if(ack){
                println("메시지 전송 성공")
            }
            else{
                println("confirm callback 에러 원인:${cause}")
            }
        }
        rabbitTemplate.setReturnsCallback { returned->
            println("returns callback 원인:${returned.message.body}")
            println("returns call back 사유:${returned.replyText}")
        }
        return rabbitTemplate
    }
}