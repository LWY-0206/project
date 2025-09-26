package com.jxdx.classroom.com.jxdx.classroom.activity

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.example.corekit.recyclerview.SingleTypeAdapter
import com.example.corekit.recyclerview.SingleViewHolder
import com.jxdx.classroom.activity.ClassActivity
import com.jxdx.classroom.com.jxdx.classroom.entity.ClassLive
import com.jxdx.classroom.databinding.ItemClassenterBinding

class ClassEnterAdapter : SingleTypeAdapter<ClassLive>() {
    override fun createViewHolder(
        viewType: Int,
        inflater: LayoutInflater,
        parent: ViewGroup
    ): SingleViewHolder<ViewBinding, Any>? {
        return ClassRoomViewHolder(
            ItemClassenterBinding.inflate(inflater, parent, false)
        ) as? SingleViewHolder<ViewBinding, Any>
    }

    class ClassRoomViewHolder(view: ItemClassenterBinding) :
        SingleViewHolder<ItemClassenterBinding, ClassLive>(view) {

        override fun setHolder(entity: ClassLive) {
            view.tvClassNameValue.text = entity.roomName
            view.tvCourseNameValue.text = entity.className
            view.tvTeacherValue.text = entity.teacherName
            view.tvSubjectValue.text = entity.subjectName
            view.tvStatusValue.text = when(entity.status){
                0 -> "未开始"
                1 -> "正在直播"
                else -> "未知"
            }
            
            // 设置点击事件
            view.btnEnterClass.setOnClickListener {
                val intent = Intent(context, ClassActivity::class.java)
                context.startActivity(intent)
            }
        }

        override fun setHolder(entity: ClassLive, payload: Any) {
            setHolder(entity)
        }
    }
}