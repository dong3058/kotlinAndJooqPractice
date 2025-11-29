package com.jooqtest.jooq.stream

import com.rabbitmq.stream.Environment
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.rabbit.stream.config.StreamRabbitListenerContainerFactory


@Configuration
class RabbitMqStreamConfig {


    //rabbit stream은 envrioment안만들어도 yml파일 참고해서 알아서 만들어낸다.
    /*@Bean
    fun streamEnvironment(): Environment {
        return Environment.builder()
            .host("localhost")
            .port(5552)  // Stream 포트 (기본 AMQP 5672와 다름)
            .username("guest")
            .password("guest")
            .build()
    }*/

    @Bean
    fun streamRabbitListenerContainerFactory(
        environment: Environment
    ): StreamRabbitListenerContainerFactory {
        val factory = StreamRabbitListenerContainerFactory(environment)
        return factory
    }

}