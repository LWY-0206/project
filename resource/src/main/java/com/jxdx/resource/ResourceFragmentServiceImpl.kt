package com.jxdx.resource

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.jxdx.common.http.service.ResourceService
import com.jxdx.resource.FirstPage.FirstFragment
import com.jxdx.resource.Questions.QuizActivity
import com.jxdx.resource.resource.Resource
import okhttp3.Connection

class ResourceFragmentServiceImpl : ResourceService {
    override fun getFragment(fragmentName: String): Fragment? {
        return when(fragmentName){
            "Resource"-> Resource()
            "FirstFragment"-> FirstFragment()
            else -> Resource()
        }
    }

    override fun navigationTOQuizActivity(context: Context) {
        val intent = Intent(context, QuizActivity::class.java)
        context.startActivity(intent)
    }
}