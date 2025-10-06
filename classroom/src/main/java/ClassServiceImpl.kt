package com.jxdx.classroom

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.jxdx.classroom.entrance.EntranceFragment
import com.jxdx.classroom.fragment.ClassRoomFragment
import com.jxdx.classroom.group.GroupSeatActivity
import com.jxdx.classroom.group.TeacherViewActivity
import com.jxdx.common.http.service.ClassService




class ClassServiceImpl: ClassService{

    override fun getClassFragment(fragmentName: String): Fragment {
        return when(fragmentName){
            "entrance" -> EntranceFragment()
            else -> ClassRoomFragment()
        }
    }

    override fun navigateToGroupSeatActivity(context: Context) {
        var intent= Intent(context, GroupSeatActivity::class.java)
        context.startActivity(intent)
    }
    override fun navigateToTeacherViewActivity(context: Context) {
        var intent= Intent(context, TeacherViewActivity::class.java)
        context.startActivity(intent)
    }
}