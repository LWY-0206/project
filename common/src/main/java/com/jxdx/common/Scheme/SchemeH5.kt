package org.jxxy.debug.Scheme

import okhttp3.HttpUrl

class SchemeH5(
    type : Int,
    url: String ?= null,
    route : String ?= null
): Scheme(type){
    override fun viewType(): Int {
        return H5
    }
}