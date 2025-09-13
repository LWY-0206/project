package org.jxxy.debug.common.http.repository

import com.example.corekit.http.HttpManager
import com.example.corekit.http.TokenManager


open class BaseRepository<T>(val serviceClass: Class<T>) {
    val value: String
        get() {
            return TokenManager.getToken().toString()
        }
    val APiService: T by lazy {
        HttpManager.instance.service(serviceClass)
    }
}
