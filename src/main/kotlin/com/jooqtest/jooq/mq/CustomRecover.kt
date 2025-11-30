package com.jooqtest.jooq.mq

import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.retry.interceptor.MethodInvocationRecoverer
import org.springframework.stereotype.Component

@Component
class CustomRecover(private val rabbitTemplate: RabbitTemplate)  :MethodInvocationRecoverer<Unit>{
    override fun recover(p0: Array<out Any>?, p1: Throwable?) {
        val message = p0?.get(1) as Message
        val bodyString = String(message.body, Charsets.UTF_8)
        System.out.printf("재시도 실패:%s\n",bodyString)
        rabbitTemplate.convertAndSend("DLX","",message)
    }
}