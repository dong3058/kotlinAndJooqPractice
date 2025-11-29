package com.jooqtest.jooq

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.stream.Environment
import com.rabbitmq.stream.Producer
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@SpringBootApplication
class JooqApplication

fun main(args: Array<String>) {
	runApplication<JooqApplication>(*args)
}

@Service
class RabbitMqStreamService(private val environment: Environment, private val objectMapper: ObjectMapper) {

    private val producerHashMap= ConcurrentHashMap<String, Producer>();

    fun sendMessage(msg: TestMsgClass){
        val producer: Producer =createOrGetStreamProducer("test");
        val data=producer.messageBuilder()
            .addData(objectMapper.writeValueAsBytes(msg))
            .properties()
            .contentType("application/json")
            .messageBuilder()
            .build();
        producer.send(data){status->
            if(status.isConfirmed){
                println("메시지 전송 성공")
            }
            else{
                println("메시지 전송 실패")
            }
        }
    }
    private fun createOrGetStreamProducer(streamName: String): Producer {
        return producerHashMap.computeIfAbsent(streamName) { streamName ->
            try {
                environment.streamCreator()
                    .name(streamName)
                    .create()
                // 여기까지 오면 스트림 생성 성공!
            } catch (e: Exception) {
                // "이미 존재함" 에러는 무시해도 된다요. (우리가 원하는 상태니까)
            }
            // [2단계] 이제 스트림이 확실히 있으니까, 프로듀서를 만들어서 반환
            environment.producerBuilder()
                .stream(streamName)
                .build()
        }
    }
    fun delStreamProducer(streamName: String) {
        val producer = producerHashMap.remove(streamName)
        producer?.close()//이건 어플리케이션과 rabbit mq의 스트림간의 통신만 종료,mq에는 살아있음 스트림이.
        environment.deleteStream(streamName);//진짜 삭제시키는것.

    }
}