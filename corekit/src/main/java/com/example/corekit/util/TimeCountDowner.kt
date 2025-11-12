package com.example.corekit.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.TimeUnit

object TimeCountDowner {
    @OptIn(ExperimentalCoroutinesApi::class)
    @JvmOverloads
    fun countDown(
        scope: CoroutineScope,
        onTick: ((Int) -> Unit)? = null,
        total: Int,
        interval: Int = 1,
        timeUnit: TimeUnit = TimeUnit.SECONDS,
        onFinish: (() -> Unit)?
    ): Job {
        return flow {
            for (i in total downTo 1 step interval) {
                emit(i)
                delay(timeUnit.toMillis(interval.toLong()))
            }
        }.flowOn(Dispatchers.Default)
            .onCompletion { onFinish?.invoke() }
            .onEach { onTick?.invoke(it) }
            .flowOn(Dispatchers.Main)
            .launchIn(scope)
    }

    fun countDown2(
        scope: CoroutineScope,
        interval: Int = 1,
        timeUnit: TimeUnit = TimeUnit.SECONDS,
        onTick: (() -> Unit)? = null,
    ): Job {
        return flow {
            while (true) {
                emit(null)
                delay(timeUnit.toMillis(interval.toLong()))
            }
        }.flowOn(Dispatchers.Default)
            .onEach { onTick?.invoke() }
            .flowOn(Dispatchers.Main)
            .launchIn(scope)
    }

    fun countDownWithDelay(
        scope: CoroutineScope,
        interval: Int = 1,
        timeUnit: TimeUnit = TimeUnit.SECONDS,
        onTick: (() -> Unit)? = null,
    ): Job {
        return flow {
            while (true) {
                delay(timeUnit.toMillis(interval.toLong()))
                emit(null)
            }
        }.flowOn(Dispatchers.Default)
            .onEach { onTick?.invoke() }
            .flowOn(Dispatchers.Main)
            .launchIn(scope)
    }
}
