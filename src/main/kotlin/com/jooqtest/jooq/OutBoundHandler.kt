package com.jooqtest.jooq

import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.GenericMessage
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component


@Component
class OutBoundHandler:ChannelInterceptor {

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        //RABBITMQ 환경하에 CONNECTED메시지 헤더.
        //simpMessageType=OTHER, stompCommand=CONNECTED, nativeHeaders={server=[RabbitMQ/4.2.1], session=[session-WdhJJ7_-Y5JIwgjtFRdGmQ], heart-beat=[10000,10000], version=[1.1]}, simpSessionId=5nltbddw}
        println("${message.headers}")

        return message
    }
}