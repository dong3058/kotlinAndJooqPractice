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
            //rabbitmq하고 heartbeat 협상과정인대 내가쓴 websocket 툴은 협상과정을 조절할수없어서 여기서 들어온 메시지의 heartbeat를조절.
            return MessageBuilder.createMessage(message.payload,accessor.messageHeaders);

        }

        if(command==StompCommand.SUBSCRIBE&&accessor.destination!!.startsWith("/topic")){
            println(accessor.destination)
            accessor.setNativeHeader("x-message-ttl","10000")//이거는 argumetns라는 속성 지정시 이렇게 넣어주자.
            //accessor.setNativeHeader("x-queue-type","stream")
            /*accessor.setNativeHeader("durable", "false")
            accessor.setNativeHeader("exclusive", "false")
            accessor.setNativeHeader("auto-delete", "true");
            return MessageBuilder.createMessage(message.payload,accessor.messageHeaders)*/
            return MessageBuilder.createMessage(message.payload,accessor.messageHeaders);
            //이렇게 구독 하는 단계에서 생성되는 큐의 속성값을 조절할수있는대 이거 쓸일이 잇긴할까.,.?
        }
        if(command==StompCommand.SUBSCRIBE&&accessor.destination!!.startsWith("/queue/streamQueue")){
            println(accessor.destination)
            accessor.setNativeHeader("x-queue-type","stream")
            accessor.setNativeHeader("prefetch-count","10")
            accessor.setNativeHeader("ack","client")
            /*accessor.setNativeHeader("durable", "false")
            accessor.setNativeHeader("exclusive", "false")
            accessor.setNativeHeader("auto-delete", "true");
            return MessageBuilder.createMessage(message.payload,accessor.messageHeaders)*/
            return MessageBuilder.createMessage(message.payload,accessor.messageHeaders);
            //이렇게 구독 하는 단계에서 생성되는 큐의 속성값을 조절할수있는대 이거 쓸일이 잇긴할까.,.?
        }
        return message
    }
}