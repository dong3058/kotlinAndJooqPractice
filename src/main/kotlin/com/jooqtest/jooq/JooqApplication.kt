package com.jooqtest.jooq

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.stream.Environment
import com.rabbitmq.stream.Producer
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@SpringBootApplication
@EnableRetry
class JooqApplication

fun main(args: Array<String>) {
	runApplication<JooqApplication>(*args)
}

