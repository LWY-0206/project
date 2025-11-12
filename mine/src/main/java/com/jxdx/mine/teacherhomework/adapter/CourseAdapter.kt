package com.jxdx.mine.teacherhomework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jxdx.mine.CourseDetail
import com.jxdx.mine.databinding.ItemCourseBinding

class CourseAdapter(
    private val courses: List<CourseDetail>,
    private val onClick: (CourseDetail) -> Unit
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {
    inner class CourseViewHolder(val binding: ItemCourseBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = ItemCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]
        holder.binding.tvCourseName.text = course.subjectName
        holder.itemView.setOnClickListener { onClick(course) }
    }

    override fun getItemCount(): Int = courses.size

}