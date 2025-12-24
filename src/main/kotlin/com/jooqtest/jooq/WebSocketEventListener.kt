package com.jooqtest.jooq

import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.*

@Component
class WebSocketEventListener {

    @EventListener
    fun catchUnsubscribe(event:SessionUnsubscribeEvent){
        val acc=SimpMessageHeaderAccessor.wrap(event.message);
            println("구독 연결해제 이벤트")
            println("${event.message.headers}")
            println("${event.message}")
            println("${acc.user}")
        println("${event.user}")
    }
    @EventListener
    fun catchDisConnect(event:SessionDisconnectEvent){
        val acc=SimpMessageHeaderAccessor.wrap(event.message);
        println("세션 연결해제 이벤트")
        println("${event.message.headers}")
        println("${event.message}")
        println("${acc.user}")
        println("${event.user}")
    }

    @EventListener
    fun catchSubScribeEvent(event: SessionSubscribeEvent){

        val acc=SimpMessageHeaderAccessor.wrap(event.message);

        println("세션 구독 이벤트")
        println("${event.message.headers}")
        println("${acc.destination}")
        println("${acc.sessionId}")
        println("${event.message}")
        println("${acc.user}")
        println("${event.user}")
    }
    @EventListener
    fun catchConnectionEvent(event: SessionConnectEvent){
        val acc=SimpMessageHeaderAccessor.wrap(event.message);
        println("세션 연결 이벤트")
        println("${event.message.headers}")
        println("${event.message}")
        println("${acc.user}")
        println("${event.user}")
    }
}