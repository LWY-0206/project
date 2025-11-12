package org.jxxy.debug.http

import android.util.Log
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import org.jxxy.debug.common.http.BaseLiveDataCallback2


abstract class BaseLiveDataCallback3<respone> : BaseLiveDataCallback2<respone>{

    override fun otherCode(emit: ResLiveData<respone>, code: Int?, msg: String?, data: respone?) {
        if (code == 101) {
            Log.d("getUserInfo1","我进到101了")
            onCode101(emit,code, msg,data)
        } else {
            emit.error(ErrorResponse.otherCode(code, msg))
        }
    }


    abstract fun onCode101(emit: ResLiveData<respone>, code: Int?, msg: String?, data: respone?)

}