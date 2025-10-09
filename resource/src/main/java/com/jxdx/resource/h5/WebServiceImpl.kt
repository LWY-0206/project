package org.jxxy.debug.h5

import android.content.Context
import android.os.Bundle
import com.example.corekit.util.startActivity
import com.google.auto.service.AutoService
import org.jxxy.debug.service.WebService

@AutoService(WebService::class)
class WebServiceImpl : WebService {
    override fun gotoWebH5(context: Context, url: String) {
        val bundle = Bundle()
        // 把数据保存到Bundle里
        bundle.putString("url", url)
        context.startActivity<WebActivity>(bundle) // 拿到bundle里的数据去跳网页H5
    }
}
