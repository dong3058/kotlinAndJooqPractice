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
        val genericMessage = message.headers["simpConnectMessage"] as GenericMessage<*>?
        val accessor = StompHeaderAccessor.wrap(message)
        if (genericMessage != null) {
            val messageHeaders = genericMessage.headers
            val maps: Map<String, Any>? = messageHeaders["nativeHeaders"] as Map<String,Any>?
            val newAccessor = StompHeaderAccessor.create(StompCommand.CONNECTED)
            newAccessor.copyHeaders(accessor.toMap())
            newAccessor.setNativeHeader("x-custom", "value")
            maps!!.entries.stream().forEach { x: Map.Entry<String, Any> ->
                val list = x.value as List<String>
                //클라이언트에게 heartbeat를 이렇게 하자고 협상하는과정. 실질적으로 hearbeat를 저기에 설정한값으로 나가는건아니고
                //걍 알려줘서 협상한다 생각하면됨. 물론 백엔드는 websocketconfig에 설정해둔값에따라서 알아서 나간다.
                //즉 알려주는 데이터랑 백엔드 설정이 달라도 되긴하는대 그건 테스트환경이고 실제론 일치시켜주자.
                //앞의 값이 server가 쏘는 주기(ms단위) 뒤값이 클라이언트가 쏘는값.
                if (x.key == "heart-beat") {
                    newAccessor.setNativeHeader(
                        x.key,
                        java.lang.String.join(",", listOf("20000","0"))
                    )
                } else {
                    newAccessor.setNativeHeader(x.key, java.lang.String.join(",", list))
                }
            }
            return MessageBuilder.createMessage(message.payload, newAccessor.messageHeaders)
        }

        return message
    }
}