package com.jooqtest.jooq

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Controller


@Controller
class TestControllerWithOutRabbit(private val simpMessageTemplate: SimpMessagingTemplate) {



    @Retryable(maxAttempts = 2, recover = "recoverByDlx", backoff= Backoff(delay = 1000))
    @MessageMapping("/sending")
    fun testingSendMsg(@Payload testMsgClass: TestMsgClass){


        throw RuntimeException("hello world")
        /*
        * val header=map.of(~~)
        * 해서 해더값에다가 큐의 속성을 넣고 convberanjdsend에 넣으면 해당되눈 속성의 큐를 찾는대 솔직히 할필요가있나..?
        * */
        simpMessageTemplate.convertAndSend("/queue/testQueue",testMsgClass);
        /* rabbitTemplate.convertAndSend("testExchange", "", testMsgClass){
             throw RuntimeException("테스트용 강제 에러 발생!")
         }*/

    }
    @MessageMapping("/topic")
    fun topicSendingMsg(@Payload testMsgClass: TestMsgClass){
        //argumetns를 custom했다면 따로 header를 넣어줄 필요가ㅇ없다. 아래 예시같은 케이스를 말하는것.
        //단 큐타입 같은경우 아래 stream처럼 기존의 stomp 경로 topic,queue에서 벗어난 타입을 넣은경우  헤더에다가 값 넣어줘야됨.
        //name    durable auto_delete     exclusive       arguments       consumers
        //stomp-subscription-GrA2pmBV-Mih8ZAFWBfz1A       false   true    false   [{"x-message-ttl",10000},{"x-queue-type","classic"}]    1
        simpMessageTemplate.convertAndSend("/topic/topicQueue",testMsgClass);
    }
    @MessageMapping("/stream")
    fun streamQueueSendingMsg(@Payload testMsgClass: TestMsgClass){
        //구독 단계에서 인터셉터에서 stream으로 만든경우에는 이렇게 넣어줘야된다. 즉 stream속성에 대해서는 argumetns여도 넣어줘여됨.
        val header= mapOf("x-queue-type" to "stream")
        simpMessageTemplate.convertAndSend("/queue/streamQueue",testMsgClass,header);
    }


    @Recover
    fun recoverByDlx(e:Exception,testMsgClass: TestMsgClass){
        println("리커버 실행-에러 원인 ${e.message}")
        simpMessageTemplate.convertAndSend("/queue/errorQueue",testMsgClass)
    }
}