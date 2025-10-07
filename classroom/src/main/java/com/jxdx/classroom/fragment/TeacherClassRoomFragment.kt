package com.jxdx.classroom.fragment

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.example.corekit.common.BaseFragment
import com.jxdx.classroom.activity.SelectClassAdapter
import com.jxdx.classroom.activity.SelectClassViewModel
import com.jxdx.classroom.databinding.TeacherClassRoomFragmentBinding


class TeacherClassRoomFragment:BaseFragment<TeacherClassRoomFragmentBinding>() {
    private var teacherId: Int = 0
    private lateinit var adapter: SelectClassAdapter
    private lateinit var viewModel: SelectClassViewModel
    override fun bindLayout(): TeacherClassRoomFragmentBinding {
        return TeacherClassRoomFragmentBinding.inflate(layoutInflater)
    }

    override fun initView() {
        // 获取从Activity传递过来的参数
        arguments?.let {
            teacherId = it.getInt("teacherId", 0)
        }
        
        viewModel = ViewModelProvider(this)[SelectClassViewModel::class.java]
        // 在获取teacherId后再初始化adapter
        adapter = SelectClassAdapter(teacherId)
        val recyclerView =find.TeacherRecyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())

        // 设置返回按钮点击事件
        find.TeacherBtnBack.setOnClickListener {
            // 返回上一页
            requireActivity().onBackPressed()
        }
    }

    override fun subscribeUi() {
        // 观察ViewModel中的数据变化
        viewModel.selectClassLiveData.observe(this) { result ->
            result.onSuccess { data ->
                if (data != null) {
                    adapter.clearAndAdd(data)
                } else {
                    Log.w("TeacherClassRoomFragment", "数据为空")
                }
            }
            result.onError { error, _ ->
                Log.e("TeacherClassRoomFragment", "数据获取失败：$error")
            }
        }

        // 请求数据
        viewModel.getSelectClass()
    }
}