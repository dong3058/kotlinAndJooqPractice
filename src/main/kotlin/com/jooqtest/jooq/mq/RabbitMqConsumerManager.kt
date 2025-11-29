package com.jooqtest.jooq.mq

import com.fasterxml.jackson.databind.ObjectMapper
import com.jooqtest.jooq.TestMsgClass
import jakarta.annotation.PostConstruct
import org.springframework.amqp.core.MessageListener
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap


@Service
class RabbitMqConsumerManager(private val connectionFactory: ConnectionFactory,private val rabbitMqManager: RabbitMqManager,private val simpMessagingTemplate:SimpMessagingTemplate
,private val objectMapper:ObjectMapper) {

    private val connectionManagerMap: ConcurrentHashMap<String, SimpleMessageListenerContainer> = ConcurrentHashMap()

    @PostConstruct
    fun createMasterConsumer(){

        rabbitMqManager.createMasterParts()
        val messageListener=MessageListener{message->
                try {
                    val bodyString = String(message.body, Charsets.UTF_8)
                    val order: MasterOrder = objectMapper.readValue(bodyString,MasterOrder::class.java)
                    rabbitMqManager.delQueue(order.queueName)
                    removeConsumer(order.queueName);
                    System.out.printf("마스터 명령실행 성공:%s\n",order.queueName)
                }
                catch (e:Exception){
                    System.out.printf("에러발생:%s\n",e.message)
                }
            }
        val container=SimpleMessageListenerContainer(connectionFactory).apply {
                setPrefetchCount(1)
                setConcurrentConsumers(1)
                setQueueNames("masterQueue")
                setMessageListener(messageListener)
                start()
            }
        connectionManagerMap.set("masterQueue",container)

    }

    fun createConsumer(queueName: String){
        if(!connectionManagerMap.contains(queueName)){
            val messageListener=MessageListener{message->
                try {
                    val bodyString = String(message.body, Charsets.UTF_8)
                    val testMsgClass: TestMsgClass = objectMapper.readValue(bodyString,TestMsgClass::class.java)
                    simpMessagingTemplate.convertAndSend ("/queue/" + queueName, testMsgClass)
                    println("메시지 전송 성공")
                }
                catch (e:Exception){
                    System.out.printf("에러발생:%s\n",e.message)
                }
            }
            val container=SimpleMessageListenerContainer(connectionFactory).apply {
                setPrefetchCount(1)
                setConcurrentConsumers(3)
                setQueueNames(queueName)
                setMessageListener(messageListener)
                start()
            }
            connectionManagerMap.set(queueName,container);
        }
    }

    fun removeConsumer(queueName: String){
        connectionManagerMap.remove(queueName)?.let { container->
            container.stop()
            true
        }?:false
    }

    fun removeAllConsumer(){
        connectionManagerMap.values.forEach{value->value.stop()}
        connectionManagerMap.clear()
    }
}