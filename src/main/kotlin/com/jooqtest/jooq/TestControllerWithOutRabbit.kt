package com.jooqtest.jooq

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller


@Controller
class TestControllerWithOutRabbit(private val simpMessageTemplate: SimpMessagingTemplate) {

    @MessageMapping("/sending")
    fun testingSendMsg(@Payload testMsgClass: TestMsgClass){

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
        //name    durable auto_delete     exclusive       arguments       consumers
        //stomp-subscription-GrA2pmBV-Mih8ZAFWBfz1A       false   true    false   [{"x-message-ttl",10000},{"x-queue-type","classic"}]    1
        simpMessageTemplate.convertAndSend("/topic/topicQueue",testMsgClass);
    }
}