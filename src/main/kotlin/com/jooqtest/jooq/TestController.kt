package com.jooqtest.jooq

import com.jooqtest.jooq.mq.RabbitMqConfig
import com.jooqtest.jooq.mq.RabbitMqConsumerManager
import com.jooqtest.jooq.mq.RabbitMqManager
import com.jooqtest.jooq.stream.RabbitMqStreamService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController


//@RestController
class TestController(private val rabbitMqStreamService: RabbitMqStreamService,private val rabbitMqManager: RabbitMqManager,
    private val rabbitMqConsumerManager: RabbitMqConsumerManager) {
    @GetMapping("/delete/{name}")
    fun deleteProducer(@PathVariable(value = "name")name: String){
        rabbitMqStreamService.delStreamProducer(name);
    }


    @GetMapping("/create/queue")
    fun createQueue(){
        rabbitMqManager.createQueue("testQueue")
        rabbitMqManager.createExchange("testExchange")
        rabbitMqManager.createBinding("testQueue","testExchange")
        //rabbitMqConsumerManager.createConsumer("testQueue")
    }

}