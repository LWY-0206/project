package com.jxdx.classroom.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jxdx.classroom.R
import com.jxdx.classroom.entity.PreviewTask
import com.jxdx.classroom.entity.PreviewStatus
import com.jxdx.classroom.entity.StudentPreviewStatus

/**
 * 预习任务适配器
 */
class PreviewTaskAdapter(
    private val onTaskClick: (PreviewTask) -> Unit
) : RecyclerView.Adapter<PreviewTaskAdapter.TaskViewHolder>() {

    private var tasks = mutableListOf<PreviewTask>()

    fun updateTasks(newTasks: List<PreviewTask>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_preview_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task)
    }

    override fun getItemCount(): Int = tasks.size

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_task_title)
        private val tvContent: TextView = itemView.findViewById(R.id.tv_task_content)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_task_status)
        private val tvPublishTime: TextView = itemView.findViewById(R.id.tv_publish_time)
        private val tvDeadline: TextView = itemView.findViewById(R.id.tv_deadline)
        private val tvCompletionRate: TextView = itemView.findViewById(R.id.tv_completion_rate)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar)

        fun bind(task: PreviewTask) {
            tvTitle.text = task.title
            tvContent.text = task.content
            tvPublishTime.text = task.publishTime
            tvDeadline.text = task.deadline
            tvCompletionRate.text = "完成率: ${task.completionRate}%"
            
            // 设置状态
            if (task.isCompleted) {
                tvStatus.text = "已完成"
                tvStatus.setBackgroundResource(R.drawable.status_completed_background)
            } else {
                tvStatus.text = "进行中"
                tvStatus.setBackgroundResource(R.drawable.status_ongoing_background)
            }
            
            // 设置进度条
            progressBar.progress = task.completionRate
            
            // 设置点击事件
            itemView.setOnClickListener {
                onTaskClick(task)
            }
        }
    }
}

/**
 * 学生预习情况适配器
 */
class StudentPreviewAdapter(
    private val onStudentClick: (StudentPreviewStatus) -> Unit
) : RecyclerView.Adapter<StudentPreviewAdapter.StudentViewHolder>() {

    private var students = mutableListOf<StudentPreviewStatus>()

    fun updateStudents(newStudents: List<StudentPreviewStatus>) {
        students.clear()
        students.addAll(newStudents)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_preview, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]
        holder.bind(student)
    }

    override fun getItemCount(): Int = students.size

    inner class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_student_name)
        private val tvId: TextView = itemView.findViewById(R.id.tv_student_id)
        private val tvCompletionRate: TextView = itemView.findViewById(R.id.tv_completion_rate)
        private val tvLastUpdate: TextView = itemView.findViewById(R.id.tv_last_update)
        private val ivStatusIcon: android.widget.ImageView = itemView.findViewById(R.id.iv_status_icon)
        private val tvStatusText: TextView = itemView.findViewById(R.id.tv_status_text)

        fun bind(student: StudentPreviewStatus) {
            tvName.text = student.studentName
            tvId.text = student.studentId
            tvCompletionRate.text = "完成率: ${student.completionRate}%"
            tvLastUpdate.text = "最后更新: ${student.lastUpdateTime}"
            
            // 根据完成率设置状态
            when (student.status) {
                PreviewStatus.EXCELLENT -> {
                    ivStatusIcon.setImageResource(R.drawable.ic_star)
                    ivStatusIcon.setColorFilter(itemView.context.getColor(android.R.color.holo_orange_dark))
                    tvStatusText.text = "优秀"
                    tvStatusText.setTextColor(itemView.context.getColor(android.R.color.holo_orange_dark))
                }
                PreviewStatus.GOOD -> {
                    ivStatusIcon.setImageResource(R.drawable.ic_check_circle)
                    ivStatusIcon.setColorFilter(itemView.context.getColor(android.R.color.holo_green_dark))
                    tvStatusText.text = "良好"
                    tvStatusText.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
                }
                PreviewStatus.AVERAGE -> {
                    ivStatusIcon.setImageResource(R.drawable.ic_warning)
                    ivStatusIcon.setColorFilter(itemView.context.getColor(android.R.color.holo_orange_dark))
                    tvStatusText.text = "一般"
                    tvStatusText.setTextColor(itemView.context.getColor(android.R.color.holo_orange_dark))
                }
                PreviewStatus.POOR -> {
                    ivStatusIcon.setImageResource(R.drawable.ic_error)
                    ivStatusIcon.setColorFilter(itemView.context.getColor(android.R.color.holo_red_dark))
                    tvStatusText.text = "较差"
                    tvStatusText.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
                }
            }
            
            // 设置点击事件
            itemView.setOnClickListener {
                onStudentClick(student)
            }
        }
    }
}
