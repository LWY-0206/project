package org.jxxy.debug.Scheme

import android.content.Context
import android.util.Log
import org.jxxy.debug.Scheme.Scheme.Companion.DETAIL
import org.jxxy.debug.Scheme.Scheme.Companion.H5

fun Scheme.navigation(context: Context){
    when(this.type){
        H5 -> {
            this.url?.let {
                //后面用来处理H5的路径
            }
        }


    }
}