package org.jxxy.debug.Scheme


import com.example.corekit.recyclerview.MultipleType
import java.io.Serializable

open class Scheme(val type: Int = -1) : MultipleType, Serializable {

    constructor() : this(-1)

    var resourceId : Int ?= null
    var url : String ?= null
    val route : String ?= null

    //子类的类型
    companion object {
        const val H5 = 1
        //详情资源为2
        const val DETAIL = 2
    }


    override fun viewType(): Int = type
}