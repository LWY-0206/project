package com.jxdx.classroom.fragment

import android.content.Intent
import com.example.corekit.common.BaseFragment
import com.jxdx.classroom.activity.ClassActivity
import com.jxdx.classroom.activity.MainActivity


import com.jxdx.classroom.databinding.ClassroomFragmentBinding



class ClassRoomFragment : BaseFragment<ClassroomFragmentBinding>() {
    override fun bindLayout(): ClassroomFragmentBinding = ClassroomFragmentBinding.inflate(layoutInflater)
    override fun initView() {
        find.btnEnterClass.setOnClickListener {
            val intent = Intent(requireActivity(), ClassActivity::class.java)
            startActivity(intent)
        }
    }
    override fun subscribeUi() {

    }

}