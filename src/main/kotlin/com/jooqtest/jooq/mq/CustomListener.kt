package com.jooqtest.jooq.mq

import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.RetryListener
import org.springframework.stereotype.Component

@Component
class CustomListener:RetryListener {

    //producer 즉 rabbittemplate에 등록하는 retry시에 작동하는 lisntener.

    override fun <T : Any?, E : Throwable?> onSuccess(
        context: RetryContext?,
        callback: RetryCallback<T, E>?,
        result: T
    ) {

        println("재실행 성공:${context}\n")
        super.onSuccess(context, callback, result)
    }

    override fun <T : Any?, E : Throwable?> onError(
        context: RetryContext?,
        callback: RetryCallback<T, E>?,
        throwable: Throwable?
    ) {
        print("재실행 실패:${throwable}\n")
        super.onError(context, callback, throwable)
    }
}