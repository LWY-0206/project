package com.jxdx.classroom.com.jxdx.classroom.activity

import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.example.corekit.common.BaseFragment
import com.jxdx.classroom.databinding.FragmentClassenterBinding


class ClassEnterFragment : BaseFragment<FragmentClassenterBinding>() {
    lateinit var typeAdapter: ClassEnterAdapter
    private lateinit var viewModel: ClassRoomViewModel


    override fun bindLayout(): FragmentClassenterBinding {
        return FragmentClassenterBinding.inflate(layoutInflater)
    }

    override fun initView() {
        val recyclerView = find.recyclerView
        typeAdapter = ClassEnterAdapter()
        recyclerView.adapter = typeAdapter

        // 设置LayoutManager
        recyclerView.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(requireContext())

        // 初始化ViewModel
        viewModel = ViewModelProvider(this)[ClassRoomViewModel::class.java]

        // 添加返回按钮点击事件
        find.btnBack.setOnClickListener {
            if (parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStack()
            } else {
                requireActivity().finish()
            }
        }
    }

    override fun subscribeUi() {
        // 观察ViewModel中的数据变化
        viewModel.classLiveData.observe(this) { result ->
            result.onSuccess { data ->
                if (data != null) {
                    typeAdapter.clearAndAdd( data)
                } else {
                    Log.w("ClassEnterFragment", "数据为空")
                }
            }
            result.onError { error ,_->
                Log.e("ClassEnterFragment", "数据获取失败：$error")
            }
        }

        viewModel.getClassRoom()
    }
}