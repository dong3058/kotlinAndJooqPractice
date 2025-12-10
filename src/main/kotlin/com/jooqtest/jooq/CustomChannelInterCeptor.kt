package com.jooqtest.jooq

import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import kotlin.math.log

@Component
class CustomChannelInterCeptor :ChannelInterceptor{
    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val accessor = StompHeaderAccessor.wrap(message)
        val command=accessor.command;
        if(command==StompCommand.CONNECT) {

            var value=accessor.getNativeHeader("heart-beat")
            println(value)
            accessor.setNativeHeader("heart-beat",20000.toString()+","+0.toString())
            value=accessor.getNativeHeader("heart-beat")
            println(value)
            println("헤더 설정 완료")
            return MessageBuilder.createMessage(message.payload,accessor.messageHeaders)
        }
        return message
    }
}