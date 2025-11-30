package com.jooqtest.jooq.mq

import com.fasterxml.jackson.databind.ObjectMapper
import com.jooqtest.jooq.TestMsgClass
import jakarta.annotation.PostConstruct
import org.springframework.amqp.AmqpRejectAndDontRequeueException
import org.springframework.amqp.core.MessageListener
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.rabbit.retry.MessageRecoverer
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer
import org.springframework.context.annotation.Bean
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.retry.RetryOperations
import org.springframework.retry.annotation.RecoverAnnotationRecoveryHandler
import org.springframework.retry.interceptor.MethodInvocationRecoverer
import org.springframework.retry.interceptor.RetryInterceptorBuilder
import org.springframework.retry.interceptor.RetryOperationsInterceptor
import org.springframework.retry.policy.SimpleRetryPolicy
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap


@Service
class RabbitMqConsumerManager(private val customRecover: CustomRecover,private val connectionFactory: ConnectionFactory,private val rabbitMqManager: RabbitMqManager,private val simpMessagingTemplate:SimpMessagingTemplate
,private val objectMapper:ObjectMapper) {

    private val connectionManagerMap: ConcurrentHashMap<String, SimpleMessageListenerContainer> = ConcurrentHashMap()

    @PostConstruct
    fun createMasterConsumer(){
        rabbitMqManager.createDLX()
        rabbitMqManager.createDLQ()
        rabbitMqManager.createDLXBinding()
        val dlxMessageListener= MessageListener { message->
            try{
                val bodyString = String(message.body, Charsets.UTF_8)
                System.out.printf("dlx 메시지 출력:%s\n",bodyString)
            }
            catch (e:Exception){
                println("retry에도실패 그냥 드랍.")
            }
        }
        val dlxContainer=SimpleMessageListenerContainer(connectionFactory).apply{
            setPrefetchCount(1)
            setConcurrentConsumers(3)
            setQueueNames("DLQ")
            setMessageListener(dlxMessageListener)
            start()
        }
        connectionManagerMap.set("DLX",dlxContainer)
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
            setConcurrentConsumers(3)
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
                    throw RuntimeException("에러 테스팅")
                    /* bodyString = String(message.body, Charsets.UTF_8)
                    val testMsgClass: TestMsgClass = objectMapper.readValue(bodyString,TestMsgClass::class.java)
                    simpMessagingTemplate.convertAndSend ("/queue/" + queueName, testMsgClass)
                    println("메시지 전송 성공")*/
                }
                catch (e:Exception){
                    System.out.printf("에러발생:%s\n",e.message)
                    throw e
                    //에러를 이렇게 던져버리면 nack 상태의 메시지가되고 기본적으로는 다시 큐에집어넣어서 처리한다.
                    //만약 catch해서 던지지않고 처리시 메시지가 완전하게
                    //처리된 ack상태가된다.
                }
            }
            val container=SimpleMessageListenerContainer(connectionFactory).apply {
                setPrefetchCount(1)
                setConcurrentConsumers(3)
                setQueueNames(queueName)
                setDefaultRequeueRejected(false)//애는 실패시 다시 큐에넣ㅇ르지 말지를 결정하는애
                // .기본 설정으로는 실패시 다시 큐에집어넣어서 재처리하는게 기본값임. false면 그렇게 안된다.
                //근대 작동을 안하는대... 왠지모르겟내
                setAdviceChain(retryOperationInterceptor())
                setMessageListener(messageListener)
                start()
            }
            connectionManagerMap.set(queueName,container);
        }
    }



    @Bean
    fun retryOperationInterceptor():RetryOperationsInterceptor{
        //stateless-> 메세지가 실패하면 리스너르 실행하는 스레드는 정지,계속해서 해당 메시지에 대해서 정해진 횟수만큼 retry를 시도
        //stateful-->메시지 실패시 다시 큐에 집어넣고 해당 메모리에대한 retry횟수를 기록해둠.
        //애는 인터셉터라서 소비자가 소비도중 발생한 애플리케이션 에러를 애가 인터셉터를 해감.-->그걸 정해진 횟수만큼 실행하고 만약 그리했는대도
        //에러가 발생시 recoverer에 넘긴다. 그럼 recoverer에서 처리함. 메시지는 ack처리됨.
        //본래 대로라면 에러 발생시 큐에 다시 넘겨서 해결될때까지 무한 반복 처리임.
        return RetryInterceptorBuilder.stateless()
            .retryPolicy(SimpleRetryPolicy(3))
            .recoverer(customRecover)
            .build()
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