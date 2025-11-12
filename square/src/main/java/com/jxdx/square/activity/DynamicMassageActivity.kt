package com.jxdx.square.activity

import androidx.recyclerview.widget.LinearLayoutManager
import com.example.corekit.common.BaseActivity
import com.example.corekit.util.singleClick
import com.jxdx.square.adapter.CommentDynamicAdapter
import com.jxdx.square.adapter.LikeDynamicAdapter
import com.jxdx.square.databinding.ActivityDynamicMessageBinding

class DynamicMassageActivity: BaseActivity<ActivityDynamicMessageBinding>() {
    private lateinit var commentDynamicAdapter: CommentDynamicAdapter
    private lateinit var likeDynamicAdapter: LikeDynamicAdapter

    override fun bindLayout(): ActivityDynamicMessageBinding {
        return ActivityDynamicMessageBinding.inflate(layoutInflater)
    }

    override fun initView() {
        // 初始化适配器
        commentDynamicAdapter = CommentDynamicAdapter()
        likeDynamicAdapter = LikeDynamicAdapter()

        // 配置点赞列表RecyclerView
        view.rvLikes.layoutManager = LinearLayoutManager(this)
        view.rvLikes.adapter = likeDynamicAdapter

        // 配置评论列表RecyclerView
        view.rvComments.layoutManager = LinearLayoutManager(this)
        view.rvComments.adapter = commentDynamicAdapter

        // 设置返回按钮点击事件
        view.ivBack.singleClick {
            finish()
        }

        // 加载测试数据
        loadTestData()
    }

    override fun subscribeUi() {
        // 可以在这里添加数据监听或其他UI更新逻辑
    }

    private fun loadTestData() {
        // 模拟点赞数据
        val likeData = listOf(
            "张三点赞了你的动态",
            "李四点赞了你的评论",
            "王五点赞了你的动态",
            "赵六点赞了你的评论"
        )
        likeDynamicAdapter.add(likeData)

        // 模拟评论数据（格式：用户名评论：\n评论内容\n评论时间）
        val commentData = listOf(
            "张三评论：\n这个可以，今天很好\n10分钟前",
            "李四评论：\n正能量满满，感谢分享\n30分钟前",
            "王五评论：\n写得很用心，受益匪浅\n1小时前",
            "赵六评论：\n非常有见地，学到了\n2小时前",
            "小明评论：\n继续保持，期待更多优质内容\n3小时前",
            "小红评论：\n观点很独特，值得思考\n昨天 18:30",
            "小刚评论：\n读了很受启发，感谢\n昨天 16:45",
            "小丽评论：\n内容充实，很有价值\n前天 14:20"
        )
        commentDynamicAdapter.add(commentData)
    }
}