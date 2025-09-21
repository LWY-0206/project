package com.jxdx.classroom

import androidx.fragment.app.Fragment
import com.jxdx.classroom.fragment.ClassRoomFragment
import com.jxdx.common.http.service.ClassService




class ClassServiceImpl: ClassService{

    override fun getClassFragment(fragmentName: String): Fragment {
        return when(fragmentName){
            "Class" -> ClassRoomFragment()
            else -> ClassRoomFragment()
        }
    }
}