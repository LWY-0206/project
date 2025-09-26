package com.jxdx.classroom.com.jxdx.classroom.fragment

import android.content.Intent
import com.example.corekit.common.BaseFragment
import com.jxdx.classroom.activity.ClassActivity
import com.jxdx.classroom.activity.MainActivity
import com.jxdx.classroom.databinding.TeacherClassRoomFragmentBinding

class TeacherClassRoomFragment:BaseFragment<TeacherClassRoomFragmentBinding>() {
    override fun bindLayout(): TeacherClassRoomFragmentBinding {
        return TeacherClassRoomFragmentBinding.inflate(layoutInflater)
    }

    override fun initView() {
        find.startLive.setOnClickListener {
            val intent = Intent(activity, ClassActivity::class.java)
            startActivity(intent)
        }

    }

    override fun subscribeUi() {
    }
}