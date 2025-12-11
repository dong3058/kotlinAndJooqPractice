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

