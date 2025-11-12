package com.jxdx.mine.teacherhomework.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jxdx.mine.R
import com.jxdx.mine.http.vo.UncorrectedHomeworkVO
import com.jxdx.mine.teacherhomework.CorrectedHomeworkDetailActivity

class CorrectedHomeworkAdapter(
    private var homeworkList: List<UncorrectedHomeworkVO> = emptyList(),
    private val onItemClick: (UncorrectedHomeworkVO) -> Unit = {}
) : RecyclerView.Adapter<CorrectedHomeworkAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSubjectName: TextView = itemView.findViewById(R.id.tv_subject_name)
        val tvHomeworkName: TextView = itemView.findViewById(R.id.tv_homework_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("CorrectedHomeworkAdapter", "onCreateViewHolder")
        try {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_uncorrected_homework, parent, false)
            return ViewHolder(view)
        } catch (e: Exception) {
            Log.e("CorrectedHomeworkAdapter", "onCreateViewHolder失败", e)
            throw e
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("CorrectedHomeworkAdapter", "onBindViewHolder position: $position")
        try {
            val homework = homeworkList[position]
            
            holder.tvSubjectName.text = homework.subjectName ?: "未知科目"
            holder.tvHomeworkName.text = homework.homeworkName ?: "未知作业"
            
            // 设置点击事件
            holder.itemView.setOnClickListener {
                Log.d("CorrectedHomeworkAdapter", "点击已批改作业: ${homework.homeworkName}")
                // 跳转到已批改作业详情页面
                CorrectedHomeworkDetailActivity.start(
                    holder.itemView.context,
                    homework.subjectId ?: 0,
                    homework.homeworkId ?: 0,
                    homework.homeworkName ?: "",
                    homework.subjectName ?: ""
                )
            }
        } catch (e: Exception) {
            Log.e("CorrectedHomeworkAdapter", "onBindViewHolder失败", e)
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        Log.d("CorrectedHomeworkAdapter", "getItemCount: ${homeworkList.size}")
        return homeworkList.size
    }

    fun updateData(newList: List<UncorrectedHomeworkVO>) {
        Log.d("CorrectedHomeworkAdapter", "updateData 数据量: ${newList.size}")
        try {
            homeworkList = newList
            notifyDataSetChanged()
        } catch (e: Exception) {
            Log.e("CorrectedHomeworkAdapter", "updateData失败", e)
            e.printStackTrace()
        }
    }
}
