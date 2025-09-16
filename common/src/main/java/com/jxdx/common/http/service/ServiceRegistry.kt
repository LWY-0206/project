package com.jxdx.common.http.service

/**
 * 一个简单的 Service 注册中心
 */
object ServiceRegistry {
    private val services = mutableMapOf<Class<*>, Any>()

    fun <T : Any> register(serviceClass: Class<T>, impl: T) {
        services[serviceClass] = impl
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(serviceClass: Class<T>): T? {
        return services[serviceClass] as? T
    }
}
