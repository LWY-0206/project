package com.jxdx.classroom.entrance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

import android.widget.TextView
import android.widget.Toast

import androidx.recyclerview.widget.RecyclerView
import com.jxdx.classroom.R

class SubjectAdapter(private val subjects: List<Subject>) :
    RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder>() {

    inner class SubjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: LinearLayout = view.findViewById(R.id.iv_subject_icon)
        val name: TextView = view.findViewById(R.id.tv_subject_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_subject, parent, false)
        return SubjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        val subject = subjects[position % subjects.size] // 无限循环
        holder.icon.setBackgroundResource(subject.iconRes)
        holder.name.text = subject.name

        holder.icon.setOnClickListener {
            Toast.makeText(holder.itemView.context, "进入 ${subject.name}", Toast.LENGTH_SHORT).show()
        }
    }
    override fun getItemCount(): Int = Int.MAX_VALUE // 无限滚动
}
