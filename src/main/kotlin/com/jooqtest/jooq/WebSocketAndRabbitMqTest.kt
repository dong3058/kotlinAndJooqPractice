package com.jooqtest.jooq

import com.jooqtest.jooq.mq.MasterOrder
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable


@Controller
class WebSocketAndRabbitMqTest(private val rabbitMqStreamService: RabbitMqStreamService,private val rabbitTemplate: RabbitTemplate) {

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

    @MessageMapping("/sending")
    fun testingSendMsg(@Payload testMsgClass: TestMsgClass){
        rabbitTemplate.convertAndSend("testExchange","",testMsgClass)
    }


}