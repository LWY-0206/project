package org.jxxy.debug.Scheme

import com.example.corekit.gson.TypeAdapterService
import com.google.auto.service.AutoService
import com.google.gson.TypeAdapterFactory


@AutoService(TypeAdapterService::class)
class SchemeServiceImpl : TypeAdapterService {
    override fun registerTypeAdapterFactory(): List<TypeAdapterFactory>? {
        val list = mutableListOf<TypeAdapterFactory>()
        list.add(SchemeTypeAdapterFactory())
        return list
    }

}