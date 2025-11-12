package com.jxdx.classroom.entrance

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

import android.widget.TextView


import androidx.recyclerview.widget.RecyclerView
import com.jxdx.classroom.R
import com.jxdx.classroom.Subject


class SubjectAdapter(
    private val subjects: List<Subject>,
    private val onSubjectClick: (Context) -> Unit
) : RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder>() {

    inner class SubjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: LinearLayout = view.findViewById(R.id.iv_subject_icon)
        val name: TextView = view.findViewById(R.id.tv_subject_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_subject, parent, false)
        return SubjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        if (subjects.isEmpty()) {
            // 设置默认值
            holder.name.text = "暂无科目"
            holder.icon.setBackgroundResource(R.drawable.ic_chemistry)
            holder.icon.setOnClickListener {
                onSubjectClick(holder.itemView.context)
            }
            return
        }else{
            val subject = subjects[position % subjects.size] // 无限循环
            holder.icon.setBackgroundResource(subject.iconRes)
            holder.name.text = subject.name

            holder.icon.setOnClickListener {
                onSubjectClick(holder.itemView.context)
            }
        }
    }
    override fun getItemCount(): Int = Int.MAX_VALUE // 无限滚动
}
