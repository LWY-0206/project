package com.jxdx.mine.teacherhomework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jxdx.mine.HomeworkDetail
import com.jxdx.mine.databinding.ItemHomeworkBinding

class HomeworkAdapter(
    private val homeworkList: List<HomeworkDetail>,
    private val onClick: (HomeworkDetail) -> Unit
) : RecyclerView.Adapter<HomeworkAdapter.HomeworkViewHolder>() {
    inner class HomeworkViewHolder(val binding: ItemHomeworkBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeworkViewHolder {
        val binding = ItemHomeworkBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HomeworkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeworkViewHolder, position: Int) {
        val homework = homeworkList[position]
        holder.binding.tvHomeworkTitle.text = homework.title
        holder.binding.tvDeadTime.text = "截止时间: ${homework.dueDate}"

        // 计算提交和批改状态
        val submissionCount = homework.submissions.filter { it.content.isNotEmpty() }.size
        val reviewedCount = homework.submissions.filter { it.isReviewed }.size

        // 显示作业状态
        if (submissionCount == 0) {
            holder.binding.tvStatus.text = "暂无提交"
        } else {
            holder.binding.tvStatus.text = "已提交: " + submissionCount + "人 (已批改: " + reviewedCount + "人)"
        }

        holder.itemView.setOnClickListener { onClick(homework) }
    }

    override fun getItemCount(): Int = homeworkList.size
}