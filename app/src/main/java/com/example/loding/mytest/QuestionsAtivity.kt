package com.example.loding.mytest

import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.corekit.common.BaseActivity
import com.example.corekit.recyclerview.CommonItemDecoration
import com.example.loding.databinding.ActivityQuestionBinding

class QuestionsAtivity : BaseActivity<ActivityQuestionBinding>() {
    val viewModel: QuestionViewModel by lazy {
        ViewModelProvider(this)[QuestionViewModel::class.java]
    }
    lateinit var questionAdapter: QuestionAdapter

    override fun bindLayout(): ActivityQuestionBinding = ActivityQuestionBinding.inflate(layoutInflater)

    override fun initView() {
        questionAdapter = QuestionAdapter()

        // 2. 配置RecyclerView（布局管理器 + 绑定适配器）
        // 假设布局中RecyclerView的id是rv_type（需替换为你实际的id）
        view.recyclerView.layoutManager = LinearLayoutManager(this) // 纵向列表（可按需改为横向）
        view.recyclerView.adapter = questionAdapter // 将适配器绑定到布局中的RecyclerView
        // （可选）添加间距（参考文档 🔶1-32：通用间距装饰）
        view.recyclerView.addItemDecoration(CommonItemDecoration(10f))

        // 初始化视图后立即加载数据
        initData()
    }

    fun initData() {
        // 调用ViewModel的getQuestions方法加载数据
        // 参数含义：
        // start: 起始位置（0表示从第一条开始）
        // size: 每页数量（这里设为10条）
        // courseType: 课程类型（根据实际需求调整）
        // showType: 显示类型（根据实际需求调整）
        // isRand: 是否随机（0表示不随机，1表示随机）
        // viewModel.getQuestions(1, 5, 1, 3, 0)
        viewModel.getQuestionsByIds(1, 5, listOf(1, 2, 3))
    }

    override fun subscribeUi() {
        viewModel.questionsLiveData.observe(this) { resource ->
            resource.onSuccess { questionList ->
                // （原有逻辑：如vpAdapter.add(it)）

                // -------------------------- 新增适配器数据设置代码 --------------------------
                // 参考文档 🔶1-23：SelectTypeAdapter提供add/clearAndAdd方法更新数据
                // 方式1：清空旧数据，添加新数据（适合刷新列表）
                questionAdapter.clearAndAdd(questionList)
            }
        }
    }
}
