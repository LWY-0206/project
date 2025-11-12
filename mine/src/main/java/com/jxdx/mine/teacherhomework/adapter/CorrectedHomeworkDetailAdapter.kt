package com.jxdx.mine.teacherhomework.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jxdx.mine.R
import com.jxdx.mine.http.vo.CorrectedHomeworkDetailVO

class CorrectedHomeworkDetailAdapter(
    private var homeworkDetailList: List<CorrectedHomeworkDetailVO> = emptyList(),
    private val onItemClick: (CorrectedHomeworkDetailVO) -> Unit = {}
) : RecyclerView.Adapter<CorrectedHomeworkDetailAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvStudentName: TextView = itemView.findViewById(R.id.tv_student_name)
        val tvScore: TextView = itemView.findViewById(R.id.tv_score)
        val tvComment: TextView = itemView.findViewById(R.id.tv_comment)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("CorrectedHomeworkDetailAdapter", "onCreateViewHolder")
        try {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_corrected_homework_detail, parent, false)
            return ViewHolder(view)
        } catch (e: Exception) {
            Log.e("CorrectedHomeworkDetailAdapter", "onCreateViewHolder失败", e)
            throw e
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("CorrectedHomeworkDetailAdapter", "onBindViewHolder position: $position")
        try {
            val homeworkDetail = homeworkDetailList[position]
            
            holder.tvStudentName.text = homeworkDetail.studentName ?: "未知学生"
            holder.tvScore.text = "得分: ${homeworkDetail.score ?: 0.0}分"
            holder.tvComment.text = homeworkDetail.teacherComment ?: "暂无评语"
            
            // 设置状态标签
            holder.tvStatus.text = "已批改"
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_reviewed_new)
            
            // 设置点击事件
            holder.itemView.setOnClickListener {
                Log.d("CorrectedHomeworkDetailAdapter", "点击已批改作业详情: ${homeworkDetail.studentName}")
                onItemClick(homeworkDetail)
            }
        } catch (e: Exception) {
            Log.e("CorrectedHomeworkDetailAdapter", "onBindViewHolder失败", e)
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        Log.d("CorrectedHomeworkDetailAdapter", "getItemCount: ${homeworkDetailList.size}")
        return homeworkDetailList.size
    }

    fun updateData(newList: List<CorrectedHomeworkDetailVO>) {
        Log.d("CorrectedHomeworkDetailAdapter", "updateData 数据量: ${newList.size}")
        try {
            homeworkDetailList = newList
            notifyDataSetChanged()
        } catch (e: Exception) {
            Log.e("CorrectedHomeworkDetailAdapter", "updateData失败", e)
            e.printStackTrace()
        }
    }
}
