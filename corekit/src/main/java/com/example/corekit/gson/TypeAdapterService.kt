package com.example.corekit.gson

import com.google.gson.TypeAdapterFactory

interface TypeAdapterService {
    fun registerTypeAdapterFactory(): List<TypeAdapterFactory>?
}
