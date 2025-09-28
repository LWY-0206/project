package com.jxdx.mine.course

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jxdx.mine.Course
import com.jxdx.mine.R

class CourseAdapter(
    private val onItemClick: (Course) -> Unit
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    private val allCourses = mutableListOf<Course>()

    fun submitList(list: List<Course>?) {
        allCourses.clear()
        if (list != null) {
            allCourses.addAll(list)
        }
        notifyDataSetChanged()
    }

    inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCourseName: TextView = itemView.findViewById(R.id.tv_course_name)
        private val tvTeacherName: TextView = itemView.findViewById(R.id.tv_teacher_name)
        private val tvTeacherAvatar: ImageView = itemView.findViewById(R.id.tv_teacher_avatar)

        fun bind(course: Course) {
            tvCourseName.text = course.subjectName

            tvTeacherName.text = "任课老师：${course.teacherName}"
            Glide.with(itemView.context)
                .load(course.avatarUrl)
                .into(tvTeacherAvatar)
            itemView.setOnClickListener { onItemClick(course) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun getItemCount() = allCourses.size
    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(allCourses[position])
    }
}
