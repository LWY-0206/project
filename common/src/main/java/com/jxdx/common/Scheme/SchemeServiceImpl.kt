package org.jxxy.debug.Scheme

import com.example.annotations.AutoService
import com.example.corekit.gson.TypeAdapterService
import com.google.gson.TypeAdapterFactory

@AutoService
class SchemeServiceImpl : TypeAdapterService {
    override fun registerTypeAdapterFactory(): List<TypeAdapterFactory>? {
        val list = mutableListOf<TypeAdapterFactory>()
        list.add(SchemeTypeAdapterFactory())
        return list
    }
}