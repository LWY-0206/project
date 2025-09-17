package com.jxdx.resource

import androidx.fragment.app.Fragment
import com.jxdx.common.http.service.ResourceService
import com.jxdx.resource.resource.Resource

class ResourceFragmentServiceImpl : ResourceService {
    override fun getFragment(fragmentName: String): Fragment? {
        return when(fragmentName){
            "Resource"-> Resource()
            else -> Resource()
        }
    }
}