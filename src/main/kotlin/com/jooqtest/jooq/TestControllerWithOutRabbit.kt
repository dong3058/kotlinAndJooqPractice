package com.jooqtest.jooq

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller


@Controller
class TestControllerWithOutRabbit(private val simpMessageTemplate: SimpMessagingTemplate) {

    @MessageMapping("/sending")
    fun testingSendMsg(@Payload testMsgClass: TestMsgClass){
        simpMessageTemplate.convertAndSend("/queue/testQueue",testMsgClass);
        /* rabbitTemplate.convertAndSend("testExchange", "", testMsgClass){
             throw RuntimeException("테스트용 강제 에러 발생!")
         }*/

    }
}