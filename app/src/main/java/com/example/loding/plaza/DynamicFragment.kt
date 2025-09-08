package com.example.loding.plaza

import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.corekit.common.BaseFragment
import com.example.corekit.recyclerview.CommonItemDecoration
import com.example.loding.R
import com.example.loding.adapter.DynamicViewModel
import com.example.loding.adapter.Dynamic_Adapter
import com.example.loding.databinding.FragmentDynamicBinding
import com.example.loding.plaza.SendFragment

class DynamicFragment : BaseFragment<FragmentDynamicBinding>() {
    val viewModel: DynamicViewModel by lazy {
        ViewModelProvider(this)[DynamicViewModel::class.java]
    }
    lateinit var dynamicAdapter: Dynamic_Adapter

    override fun bindLayout(): FragmentDynamicBinding = FragmentDynamicBinding.inflate(layoutInflater)

    override fun initView() {
        dynamicAdapter = Dynamic_Adapter()

        // 2. 配置RecyclerView（布局管理器 + 绑定适配器）
        // 假设布局中RecyclerView的id是recyclerView_dynamics（需替换为你实际的id）
        find.recyclerViewDynamics.layoutManager = LinearLayoutManager(requireContext()) // 纵向列表（可按需改为横向）
        find.recyclerViewDynamics.adapter = dynamicAdapter // 将适配器绑定到布局中的RecyclerView
        // （可选）添加间距（参考文档 🔶1-32：通用间距装饰）
        find.recyclerViewDynamics.addItemDecoration(CommonItemDecoration(10f))

        // 添加发布动态按钮的点击事件
        find.tvPublishDynamic.setOnClickListener {
            // 跳转到发布动态页面
            val sendFragment = SendFragment.newInstance()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, sendFragment)
            transaction.addToBackStack(null) // 添加到返回栈，以便可以返回到当前页面
            transaction.commit()
        }

        initData()
    }

    fun initData() {
        viewModel.getDynamics(null, 6)
    }

    override fun subscribeUi() {
        viewModel.dynamicLiveData.observe(this) { resource ->
            resource.onSuccess { dynamicList ->
                // （原有逻辑：如vpAdapter.add(it)）

                // -------------------------- 新增适配器数据设置代码 --------------------------
                // 参考文档 🔶1-23：SelectTypeAdapter提供add/clearAndAdd方法更新数据
                // 方式1：清空旧数据，添加新数据（适合刷新列表）
                dynamicAdapter.clearAndAdd(dynamicList)
            }
        }
    }
}
