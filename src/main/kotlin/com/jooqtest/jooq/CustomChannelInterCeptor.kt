package com.jooqtest.jooq

import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageBuilder
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.stereotype.Component
import kotlin.math.log

@Component
class CustomChannelInterCeptor :ChannelInterceptor{
    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        //주의 사항 stompheaderaccosr로 불려온 해쉬 코드는 accesor.messagehdaer의 해쉬 코드와 다르고
        //accessor.messageHeaders.hashCode() 이거가 message.headers.hashCode() 이거다
        //        val accessor=StompHeaderAccessor.wrap(message);
        //        val command=accessor!!.command;
         //accessor.user=StompPrincipal("HI");
        //즉 바로위 코드처럼 accessor에 user를 해줘봤자 message builder에 사용하는 accessor.messaeheaders에는 아무값도 안들어간다,
        //참고로 stompmessageheader는 new로 새로운 객체를 messageheader를 둘러싸서 돌려주는거임.
        //        println("${message.headers.hashCode()}-${accessor.hashCode()}-${accessor2.hashCode()}-${accessor.messageHeaders.hashCode()}")
        //반면에 nativeheader의 경우에는 잘작동하는대
        //이는stompheaderacc의 get,setnativbehear가 stompheaderacc의 부모인  messageheaderacc의 메서드이기때문. 즉 원본에 세팅한다.
        //결론은principal설정시에는 원본을 가져와서 세팅해야된다.


        //messageheaderacc는 바로 원본 messageheader를 주기에 .user로 principal를 해도 principal이 eventlistener에서 null이되지 않음.
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
        val command=accessor!!.command
        if(command==StompCommand.CONNECT) {

            var value=accessor.getNativeHeader("heart-beat")
            println(value)
            accessor.setNativeHeader("heart-beat",20000.toString()+","+0.toString())
            value=accessor.getNativeHeader("heart-beat")
            println(value)
            println("헤더 설정 완료")
            accessor.user=StompPrincipal("HI");
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