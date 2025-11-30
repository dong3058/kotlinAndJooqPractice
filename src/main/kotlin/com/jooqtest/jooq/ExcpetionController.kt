package com.jooqtest.jooq

import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice


@RestControllerAdvice
class ExcpetionController {


    @MessageExceptionHandler()
    fun controlError(e:Exception){
        println("에러 컨트롤러에서 에러 캐치:${e.message}")
    }
}