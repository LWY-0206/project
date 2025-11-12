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
import com.jxdx.mine.SubjectsVO

class CourseAdapter(
    private val identity: Int,
    private val onCourseClick: (Course) -> Unit,
    private val onSubjectClick: (SubjectsVO) -> Unit
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    private val allCourses = mutableListOf<Course>()
    private val allTeacherCourses = mutableListOf<SubjectsVO>()

    fun submitList(list: List<Course>?) {
        allCourses.clear()
        if (list != null) {
            allCourses.addAll(list)
        }
        notifyDataSetChanged()
    }
    fun submitTeacherList(list: List<SubjectsVO>?) {
        allTeacherCourses.clear()
        if (list != null) {
            allTeacherCourses.addAll(list)
        }
//        items.add(Courseware("第一章 引论","https://classroom-interaction.oss-cn-hangzhou.aliyuncs.com/updateFiles/48cd1e0d-f1a4-41ea-b158-9dfbd917f1e2.pptx"))
        notifyDataSetChanged()
    }


    inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCourseName: TextView = itemView.findViewById(R.id.tv_course_name)
        private val tvTeacherName: TextView = itemView.findViewById(R.id.tv_teacher_name)
        private val tvTeacherAvatar: ImageView = itemView.findViewById(R.id.tv_teacher_avatar)

        fun bindCourse(course: Course) {
            tvCourseName.text = course.subjectName

            // 学生身份显示任课老师
            tvTeacherName.text = "任课老师：${course.teacherName}"
            Glide.with(itemView.context)
                .load(course.avatarUrl)
                .into(tvTeacherAvatar)
            itemView.setOnClickListener { onCourseClick(course) }
        }

        fun bindSubject(subject: SubjectsVO) {
            tvCourseName.text = subject.subjectName

            // 老师身份显示课程ID
            tvTeacherName.text = "课程ID：${subject.subjectId}"
            // 老师身份时不需要显示头像，可以隐藏或使用默认头像
            tvTeacherAvatar.visibility = View.GONE
            itemView.setOnClickListener { onSubjectClick(subject) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (identity == 1) allTeacherCourses.size else allCourses.size
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        if (identity == 1) {
            holder.bindSubject(allTeacherCourses[position])
        } else {
            holder.bindCourse(allCourses[position])
        }
    }
}
