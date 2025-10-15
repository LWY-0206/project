package com.jxdx.resource.Schedule

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jxdx.resource.R
import com.jxdx.resource.databinding.ItemScheduleBinding

class ScheduleAdapter(private var scheduleList: List<ScheduleItem>) :
    RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    var onItemClickListener: ((ScheduleItem) -> Unit)? = null

    inner class ViewHolder(private val binding: ItemScheduleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(scheduleItem: ScheduleItem) {
            binding.tvWeek.text = scheduleItem.week
            binding.tvWeekday.text = scheduleItem.weekday
            binding.tvCourseName.text = scheduleItem.courseName
            binding.tvCourseTime.text = scheduleItem.courseTime
            binding.tvLocation.text = scheduleItem.location
            binding.tvTeachName.text = scheduleItem.teachName

            // 如果是当前课程，可以添加特殊样式
            if (scheduleItem.isCurrent == 1) {
                binding.tvCourseName.setTextColor(ContextCompat.getColor(binding.root.context, R.color.blue_500))
            } else {
                binding.tvCourseName.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black))
            }

            // 设置点击事件
            binding.root.setOnClickListener {
                onItemClickListener?.invoke(scheduleItem)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemScheduleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(scheduleList[position])
    }

    override fun getItemCount(): Int = scheduleList.size

    fun updateData(newList: List<ScheduleItem>) {
        scheduleList = newList
        notifyDataSetChanged()
    }
}