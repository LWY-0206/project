package com.jxdx.mine.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jxdx.mine.Member
import com.jxdx.mine.R

class ClassMemberAdapter(
    private val onItemClick: (Member) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_SECTION = 0
        const val TYPE_TEACHER = 1
        const val TYPE_STUDENT = 2
    }

    private val dataList = mutableListOf<Member>()

    fun submitList(data: List<Member>) {
        dataList.clear()
        dataList.addAll(data)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return dataList[position].itemType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_SECTION -> SectionViewHolder(inflater.inflate(R.layout.item_section_header, parent, false))
            TYPE_TEACHER -> TeacherViewHolder(inflater.inflate(R.layout.item_teacher, parent, false))
            else -> StudentViewHolder(inflater.inflate(R.layout.item_student, parent, false))
        }
    }

    override fun getItemCount() = dataList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val member = dataList[position]
        when (holder) {
            is SectionViewHolder -> holder.bind(member.name)
            is TeacherViewHolder -> holder.bind(member)
            is StudentViewHolder -> holder.bind(member)
        }
    }

    inner class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSectionTitle = itemView.findViewById<TextView>(R.id.tv_section_title)
        fun bind(title: String) {
            tvSectionTitle.text = title
        }
    }

    inner class TeacherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivAvatar = itemView.findViewById<ImageView>(R.id.iv_teacher_avatar)
        private val tvName = itemView.findViewById<TextView>(R.id.tv_teacher_name)
        private val tvRole = itemView.findViewById<TextView>(R.id.tv_teacher_role)

        fun bind(member: Member) {
            tvName.text = member.name
            tvRole.text = member.role ?: ""
            Glide.with(itemView)
                .load(member.avatarUrl)
                .placeholder(R.drawable.ic_teacher_default)
                .circleCrop()
                .into(ivAvatar)

            itemView.setOnClickListener { onItemClick(member) }
        }
    }

    inner class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivAvatar = itemView.findViewById<ImageView>(R.id.iv_student_avatar)
        private val tvName = itemView.findViewById<TextView>(R.id.tv_student_name)

        fun bind(member: Member) {
            tvName.text = member.name
            Glide.with(itemView)
                .load(member.avatarUrl)
                .placeholder(R.drawable.ic_student_default)
                .circleCrop()
                .into(ivAvatar)

            itemView.setOnClickListener { onItemClick(member) }
        }
    }
}
