package com.jxdx.mine.teacherhomework.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jxdx.mine.R
import com.jxdx.mine.http.vo.UncorrectedHomeworkDetailVO

class UncorrectedHomeworkDetailAdapter(
    private var homeworkDetailList: List<UncorrectedHomeworkDetailVO> = emptyList(),
    private val onItemClick: (UncorrectedHomeworkDetailVO) -> Unit = {},
    private val onReviewClick: (UncorrectedHomeworkDetailVO) -> Unit = {}
) : RecyclerView.Adapter<UncorrectedHomeworkDetailAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvStudentName: TextView = itemView.findViewById(R.id.tv_student_name)
        val tvSubmitTime: TextView = itemView.findViewById(R.id.tv_submit_time)
        val tvScore: TextView = itemView.findViewById(R.id.tv_score)
        val tvComment: TextView = itemView.findViewById(R.id.tv_comment)
        val tvSubmitContent: TextView = itemView.findViewById(R.id.tv_submit_content)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("UncorrectedHomeworkDetailAdapter", "onCreateViewHolder")
        try {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_uncorrected_homework_detail, parent, false)
            return ViewHolder(view)
        } catch (e: Exception) {
            Log.e("UncorrectedHomeworkDetailAdapter", "onCreateViewHolder失败", e)
            throw e
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("UncorrectedHomeworkDetailAdapter", "onBindViewHolder position: $position")
        try {
            val homeworkDetail = homeworkDetailList[position]
            
            // 设置学生姓名
            holder.tvStudentName.text = homeworkDetail.studentName ?: "未知学生"
            
            // 设置提交时间
            holder.tvSubmitTime.text = if (homeworkDetail.submitTime != null) {
                "提交时间: ${homeworkDetail.submitTime}"
            } else {
                "未提交"
            }
            
            // 设置分数
            holder.tvScore.text = if (homeworkDetail.score != null) {
                "分数: ${homeworkDetail.score}"
            } else {
                "未评分"
            }
            
            // 设置评语
            holder.tvComment.text = homeworkDetail.comment ?: "暂无评语"
            
            // 设置提交内容
            val submitContent = homeworkDetail.submitContent
            if (submitContent != null && submitContent.isNotEmpty()) {
                holder.tvSubmitContent.text = "提交内容:\n${submitContent.joinToString("\n")}"
                holder.tvSubmitContent.visibility = View.VISIBLE
            } else {
                holder.tvSubmitContent.text = "暂无提交内容"
                holder.tvSubmitContent.visibility = View.VISIBLE
            }
            
            // 设置状态
            val hasScore = homeworkDetail.score != null
            val hasComment = !homeworkDetail.comment.isNullOrEmpty()
            val hasSubmitContent = !homeworkDetail.submitContent.isNullOrEmpty()
            
            if (hasScore && hasComment) {
                holder.tvStatus.text = "已批改"
                holder.tvStatus.setTextColor(holder.itemView.context.getColor(android.R.color.white))
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_reviewed_new)
            } else if (hasSubmitContent) {
                holder.tvStatus.text = "待批改"
                holder.tvStatus.setTextColor(holder.itemView.context.getColor(android.R.color.white))
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending_new)
            } else {
                holder.tvStatus.text = "未提交"
                holder.tvStatus.setTextColor(holder.itemView.context.getColor(android.R.color.white))
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_not_submitted)
            }
            
            // 设置点击事件
            holder.itemView.setOnClickListener {
                Log.d("UncorrectedHomeworkDetailAdapter", "学生作业点击: ${homeworkDetail.studentName}")
                onReviewClick(homeworkDetail)
            }
            
        } catch (e: Exception) {
            Log.e("UncorrectedHomeworkDetailAdapter", "onBindViewHolder失败", e)
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int = homeworkDetailList.size

    fun updateData(newList: List<UncorrectedHomeworkDetailVO>) {
        Log.d("UncorrectedHomeworkDetailAdapter", "updateData 数据量: ${newList.size}")
        try {
            homeworkDetailList = newList
            notifyDataSetChanged()
        } catch (e: Exception) {
            Log.e("UncorrectedHomeworkDetailAdapter", "updateData失败", e)
            e.printStackTrace()
        }
    }
}
