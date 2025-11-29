package com.jooqtest.jooq.mq


import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class RabbitMqConfig {


    @Value("\${spring.rabbitmq.host}")
    private val HOSTNAME: String=""

    @Value("\${spring.rabbitmq.port}")
    private val PORT: Int=0

    @Value("\${spring.rabbitmq.username}")
    private val USERNAME: String=""

    @Value("\${spring.rabbitmq.password}")
    private val PASSWORD: String=""
    @Bean
    fun connectionFactory(): ConnectionFactory {
        val connectionFactory = CachingConnectionFactory(HOSTNAME, PORT)
        connectionFactory.username = USERNAME
        connectionFactory.setPassword(PASSWORD)

        return connectionFactory
    }
    @Bean
    fun rabbitAdmin(): RabbitAdmin {
        return RabbitAdmin(connectionFactory())
    }
    @Bean
    //rabbit mq의경우 따로 producer객체를 만든다기보단 rabbittemplate로 convertandsend메서드 사용시 exchangename,routekey를 넣어서실행함.
    //즉 이자체가 producer이다.
    fun rabbitTemplate(): RabbitTemplate {
        val rabbitTemplate = RabbitTemplate(connectionFactory())
        rabbitTemplate.messageConverter = Jackson2JsonMessageConverter()
        return rabbitTemplate
    }
}