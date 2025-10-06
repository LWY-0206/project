package com.jxdx.mine.teacherhomework.adapter

import android.R
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jxdx.mine.StudentSubmission
import com.jxdx.mine.databinding.ItemStudentSubmissionBinding

class StudentSubmissionAdapter(
    private val submissions: List<StudentSubmission>,
    private val onItemClick: (StudentSubmission) -> Unit
) : RecyclerView.Adapter<StudentSubmissionAdapter.SubmissionViewHolder>() {

    inner class SubmissionViewHolder(val binding: ItemStudentSubmissionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionViewHolder {
        val binding = ItemStudentSubmissionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SubmissionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubmissionViewHolder, position: Int) {
        val submission = submissions[position]

        holder.binding.tvStudentName.text = submission.studentName

        // 显示批改状态
        if (submission.isReviewed) {
            holder.binding.tvReviewStatus.text = "已批改"
            holder.binding.tvReviewStatus.setTextColor(
                holder.itemView.context.resources.getColor(
                    R.color.holo_green_dark
                )
            )
        } else {
            holder.binding.tvReviewStatus.text = "待批改"
            holder.binding.tvReviewStatus.setTextColor(
                holder.itemView.context.resources.getColor(
                    R.color.holo_orange_light
                )
            )
        }

        // 设置点击事件
        holder.itemView.setOnClickListener {
            onItemClick(submission)
        }
    }

    override fun getItemCount(): Int = submissions.size
}