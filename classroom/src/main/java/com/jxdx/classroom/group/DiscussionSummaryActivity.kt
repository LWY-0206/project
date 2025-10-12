package com.jxdx.classroom.group

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.jxdx.classroom.R
import com.jxdx.classroom.databinding.ActivityDiscussionSummaryBinding

class DiscussionSummaryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDiscussionSummaryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiscussionSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setupListeners()
        loadDiscussionSummary()
    }

    private fun initViews() {
        // 设置标题
        supportActionBar?.title = "讨论结果"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupListeners() {
        // 返回按钮
        binding.btnBack.setOnClickListener {
            finish()
        }

        // 导出按钮
        binding.btnExport.setOnClickListener {
            exportSummary()
        }

        // 刷新按钮
        binding.btnRefresh.setOnClickListener {
            refreshSummary()
        }
    }

    private fun loadDiscussionSummary() {
        // 模拟AI生成的讨论结果
        val summary = DiscussionSummary(
            groupId = intent.getIntExtra("groupId", 0),
            groupName = intent.getStringExtra("groupName") ?: "讨论组",
            summary = "本次小组讨论围绕项目需求分析展开，成员积极参与，形成了较为完整的方案。",
            keyPoints = listOf(
                "需求分析已完成，包括功能需求和非功能需求",
                "技术选型基本确定，采用前后端分离架构",
                "项目时间节点已制定，预计3个月完成",
                "团队成员分工明确，各自负责不同模块"
            ),
            conclusions = listOf(
                "项目可行性较高，技术方案成熟",
                "团队协作良好，沟通效率较高",
                "需要进一步细化具体实现细节",
                "建议定期进行进度汇报和问题讨论"
            ),
            participants = listOf(
                SummaryParticipant("张三", "组长", "负责整体规划和需求分析"),
                SummaryParticipant("李四", "成员", "负责前端技术选型"),
                SummaryParticipant("王五", "成员", "负责后端架构设计"),
                SummaryParticipant("赵六", "成员", "负责数据库设计")
            ),
            nextSteps = listOf(
                "完善需求文档，提交给老师审核",
                "制定详细的技术实现方案",
                "开始搭建项目基础框架",
                "安排下次讨论时间"
            ),
            whiteboardImages = listOf(
                WhiteboardImage(
                    id = 1,
                    title = "需求分析流程图",
                    description = "需求收集 → 需求分析 → 需求确认的完整流程",
                    imageResId = R.drawable.sample_whiteboard_1,
                    timestamp = "14:25",
                    author = "张三"
                ),
                WhiteboardImage(
                    id = 2,
                    title = "项目技术架构思维导图",
                    description = "前端、后端、数据库、测试四大模块的关联关系",
                    imageResId = R.drawable.sample_whiteboard_2,
                    timestamp = "14:35",
                    author = "李四"
                ),
                WhiteboardImage(
                    id = 3,
                    title = "系统三层架构设计图",
                    description = "用户界面层、业务逻辑层、数据存储层的分层设计",
                    imageResId = R.drawable.sample_whiteboard_3,
                    timestamp = "14:45",
                    author = "王五"
                )
            ),
            generatedTime = System.currentTimeMillis()
        )

        displaySummary(summary)
    }

    private fun displaySummary(summary: DiscussionSummary) {
        // 设置基本信息
        binding.tvGroupName.text = summary.groupName
        binding.tvSummary.text = summary.summary
        binding.tvGeneratedTime.text = "生成时间：${formatTime(summary.generatedTime)}"

        // 设置关键要点
        val keyPointsText = summary.keyPoints.joinToString("\n") { "• $it" }
        binding.tvKeyPoints.text = keyPointsText

        // 设置结论
        val conclusionsText = summary.conclusions.joinToString("\n") { "• $it" }
        binding.tvConclusions.text = conclusionsText

        // 设置下一步计划
        val nextStepsText = summary.nextSteps.joinToString("\n") { "• $it" }
        binding.tvNextSteps.text = nextStepsText

        // 设置白板图片
        setupWhiteboardImagesRecyclerView(summary.whiteboardImages)
        
        // 设置参与成员
        setupParticipantsRecyclerView(summary.participants)
    }

    private fun setupWhiteboardImagesRecyclerView(images: List<WhiteboardImage>) {
        val adapter = WhiteboardImageAdapter(images)
        binding.recyclerViewWhiteboardImages.apply {
            layoutManager = LinearLayoutManager(this@DiscussionSummaryActivity)
            this.adapter = adapter
        }
    }

    private fun setupParticipantsRecyclerView(participants: List<SummaryParticipant>) {
        val adapter = SummaryParticipantAdapter(participants)
        binding.recyclerViewParticipants.apply {
            layoutManager = LinearLayoutManager(this@DiscussionSummaryActivity)
            this.adapter = adapter
        }
    }

    private fun exportSummary() {
        Toast.makeText(this, "讨论结果已导出", Toast.LENGTH_SHORT).show()
        // 这里可以添加实际的导出逻辑
    }

    private fun refreshSummary() {
        Toast.makeText(this, "正在重新生成讨论结果...", Toast.LENGTH_SHORT).show()
        // 模拟重新生成
        loadDiscussionSummary()
    }

    private fun formatTime(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}

// 讨论结果数据类
data class DiscussionSummary(
    val groupId: Int,
    val groupName: String,
    val summary: String,
    val keyPoints: List<String>,
    val conclusions: List<String>,
    val participants: List<SummaryParticipant>,
    val nextSteps: List<String>,
    val whiteboardImages: List<WhiteboardImage>,
    val generatedTime: Long
)

// 白板图片数据类
data class WhiteboardImage(
    val id: Int,
    val title: String,
    val description: String,
    val imageResId: Int,
    val timestamp: String,
    val author: String
)

// 参与成员数据类
data class SummaryParticipant(
    val name: String,
    val role: String,
    val contribution: String
)

// 白板图片适配器
class WhiteboardImageAdapter(private val images: List<WhiteboardImage>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<WhiteboardImageAdapter.WhiteboardImageViewHolder>() {

    class WhiteboardImageViewHolder(val binding: com.jxdx.classroom.databinding.ItemWhiteboardImageBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): WhiteboardImageViewHolder {
        val binding = com.jxdx.classroom.databinding.ItemWhiteboardImageBinding.inflate(
            android.view.LayoutInflater.from(parent.context), parent, false
        )
        return WhiteboardImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WhiteboardImageViewHolder, position: Int) {
        val image = images[position]
        holder.binding.ivWhiteboardImage.setImageResource(image.imageResId)
        holder.binding.tvWhiteboardTitle.text = image.title
        holder.binding.tvWhiteboardTime.text = image.timestamp
        holder.binding.tvWhiteboardDescription.text = image.description
    }

    override fun getItemCount(): Int = images.size
}

// 参与成员适配器
class SummaryParticipantAdapter(private val participants: List<SummaryParticipant>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<SummaryParticipantAdapter.SummaryParticipantViewHolder>() {

    class SummaryParticipantViewHolder(val binding: com.jxdx.classroom.databinding.ItemSummaryParticipantBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): SummaryParticipantViewHolder {
        val binding = com.jxdx.classroom.databinding.ItemSummaryParticipantBinding.inflate(
            android.view.LayoutInflater.from(parent.context), parent, false
        )
        return SummaryParticipantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SummaryParticipantViewHolder, position: Int) {
        val participant = participants[position]
        holder.binding.tvName.text = participant.name
        holder.binding.tvRole.text = participant.role
        holder.binding.tvContribution.text = participant.contribution
    }

    override fun getItemCount(): Int = participants.size
}
