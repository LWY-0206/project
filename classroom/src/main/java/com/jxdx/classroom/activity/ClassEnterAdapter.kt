package com.jxdx.classroom.activity


import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.example.corekit.recyclerview.SingleTypeAdapter
import com.example.corekit.recyclerview.SingleViewHolder
import com.jxdx.classroom.entity.ClassLive
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
            view.tvClassNameValue.text = entity.className
            view.tvCourseNameValue.text = entity.roomName
            view.tvTeacherValue.text = entity.teacherName
            view.tvSubjectValue.text = entity.subjectName
            
            // 根据状态设置显示文本和按钮状态
            when(entity.status){
                0 -> {
                    // 未开始状态
                    view.tvStatusValue.text = "未开始"
                    view.tvStatusValue.setTextColor(context.getColor(android.R.color.holo_orange_dark))
                    view.btnEnterClass.text = "等待开始"
                    view.btnEnterClass.isEnabled = false
                    view.btnEnterClass.alpha = 0.5f
                }
                1 -> {
                    // 正在直播状态
                    view.tvStatusValue.text = "正在直播"
                    view.tvStatusValue.setTextColor(context.getColor(android.R.color.holo_green_dark))
                    view.btnEnterClass.text = "进入课堂"
                    view.btnEnterClass.isEnabled = true
                    view.btnEnterClass.alpha = 1.0f
                }
                else -> {
                    // 未知状态
                    view.tvStatusValue.text = "未知"
                    view.tvStatusValue.setTextColor(context.getColor(android.R.color.darker_gray))
                    view.btnEnterClass.text = "无法进入"
                    view.btnEnterClass.isEnabled = false
                    view.btnEnterClass.alpha = 0.5f
                }
            }
            
            // 设置点击事件 - 只有正在直播时才能点击
            view.btnEnterClass.setOnClickListener {
                if (entity.status == 1) {
                    val intent = Intent(context, ClassActivity::class.java)
                    // 传递liveId
                    intent.putExtra("liveId", entity.liveId)
                    context.startActivity(intent)
                } else {
                    // 显示提示信息
                    android.widget.Toast.makeText(
                        context, 
                        when(entity.status) {
                            0 -> "直播尚未开始，请耐心等待"
                            else -> "当前状态无法进入直播间"
                        }, 
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        override fun setHolder(entity: ClassLive, payload: Any) {
            setHolder(entity)
        }
    }
}