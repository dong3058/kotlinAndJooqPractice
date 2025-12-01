package com.jooqtest.jooq

import com.jooqtest.jooq.mq.MasterOrder
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable


@Controller
class WebSocketAndRabbitMqTest(private val rabbitMqStreamService: RabbitMqStreamService
,private val rabbitTemplate: RabbitTemplate,private val simpMessageTemplate: SimpMessagingTemplate) {

    @MessageMapping("/test")
    fun testingRabbitMqWithSocket(@Payload testMsgClass: TestMsgClass){
        println(testMsgClass.msg+"\n")
        rabbitMqStreamService.sendMessage(testMsgClass);
    }

    @MessageMapping("/master")
    fun testingMasterOrder(@Payload masterOrder: MasterOrder){
        println(masterOrder.queueName+"\n")
        rabbitTemplate.convertAndSend("masterExchange","",masterOrder)
    }

    @MessageMapping("/testSending")
    fun testingSendMsg2(@Payload testMsgClass: TestMsgClass){
        rabbitTemplate.convertAndSend("nonExistExchange","",testMsgClass)
    }

    @MessageMapping("/sending")
    fun testingSendMsg(@Payload testMsgClass: TestMsgClass){

            //simpMessageTemplate.convertAndSend("/queue/testQueue",testMsgClass);
            rabbitTemplate.convertAndSend("testExchange", "", testMsgClass){
                throw RuntimeException("테스트용 강제 에러 발생!")
            }

    }

    fun testingRetryTemplate(){
        val testMsgClass=TestMsgClass("hello world")
        //simpMessageTemplate.convertAndSend("/queue/testQueue",testMsgClass);
        rabbitTemplate.convertAndSend("testExchange", "", testMsgClass){
            throw RuntimeException("테스트용 강제 에러 발생!")
        }
    }


}