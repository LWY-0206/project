package com.jxdx.mine.teacherhomework.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jxdx.mine.R
import com.jxdx.mine.http.vo.UncorrectedHomeworkVO

class UncorrectedHomeworkAdapter(
    private var homeworkList: List<UncorrectedHomeworkVO> = emptyList(),
    private val onItemClick: (UncorrectedHomeworkVO) -> Unit = {}
) : RecyclerView.Adapter<UncorrectedHomeworkAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSubjectName: TextView = itemView.findViewById(R.id.tv_subject_name)
        val tvHomeworkName: TextView = itemView.findViewById(R.id.tv_homework_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("UncorrectedHomeworkAdapter", "onCreateViewHolder")
        try {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_uncorrected_homework, parent, false)
            return ViewHolder(view)
        } catch (e: Exception) {
            Log.e("UncorrectedHomeworkAdapter", "onCreateViewHolder失败", e)
            throw e
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("UncorrectedHomeworkAdapter", "onBindViewHolder position: $position")
        try {
            val homework = homeworkList[position]
            
            // 设置科目名称
            holder.tvSubjectName.text = homework.subjectName ?: "未知科目"
            
            // 设置作业名称
            holder.tvHomeworkName.text = homework.homeworkName ?: "未命名作业"
            
            // 设置点击事件
            holder.itemView.setOnClickListener {
                Log.d("UncorrectedHomeworkAdapter", "作业点击: ${homework.homeworkName}")
                onItemClick(homework)
            }
            
        } catch (e: Exception) {
            Log.e("UncorrectedHomeworkAdapter", "onBindViewHolder失败", e)
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int = homeworkList.size

    fun updateData(newList: List<UncorrectedHomeworkVO>) {
        Log.d("UncorrectedHomeworkAdapter", "updateData 数据量: ${newList.size}")
        try {
            homeworkList = newList
            notifyDataSetChanged()
        } catch (e: Exception) {
            Log.e("UncorrectedHomeworkAdapter", "updateData失败", e)
            e.printStackTrace()
        }
    }
}
