package org.jxxy.debug.Scheme

import com.example.corekit.gson.CommonTypeAdapter
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken

class SchemeTypeAdapterFactory : TypeAdapterFactory {
    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        if(Scheme::class.java.isAssignableFrom(type.rawType)){
            //如果type是Scheme或者其子类，则创建一个CommonTypeAdapter
            val adapter = CommonTypeAdapter<Scheme>(gson, this, "type")
            //注册各种类型
            //子类
            adapter.addSubTypeAdapter<SchemeH5>(Scheme.H5)
            return adapter as (TypeAdapter<T>)
        }
        return null
    }
}