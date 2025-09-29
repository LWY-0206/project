package com.jxdx.mine.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.jxdx.common.http.service.MineService
import com.jxdx.common.http.service.ServiceRegistry
import com.jxdx.mine.course.CourseActivity
import com.jxdx.mine.grade.GradeActivity
import com.jxdx.mine.homework.HomeworkActivity

class MineServiceImpl : MineService{
    override fun getFragment(fragmentName: String): Fragment? {
        return when(fragmentName){
            "my"-> My()
            else -> My()
        }
    }

    override fun navigationToHomeworkActivity(context: Context) {
        var intent=Intent(context, HomeworkActivity::class.java)
        context.startActivity(intent)
    }

    override fun navigationToGradeActivity(context: Context) {
        var intent=Intent(context, GradeActivity::class.java)
        context.startActivity(intent)
    }

    override fun navigationToCourseActivity(context: Context) {
        var intent=Intent(context, CourseActivity::class.java)
        context.startActivity(intent)
    }

}