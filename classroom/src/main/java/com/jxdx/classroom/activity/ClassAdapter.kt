package com.jxdx.classroom.activity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.example.corekit.recyclerview.SingleTypeAdapter
import com.example.corekit.recyclerview.SingleViewHolder
import com.jxdx.classroom.databinding.ItemClassInfoBinding
import com.jxdx.classroom.entity.Classroom

class ClassAdapter:SingleTypeAdapter<Classroom>() {
    
    // 存储选中的班级ID列表
    private val selectedClassIds = mutableSetOf<Int>()
    
    // 选择状态变化的回调
    var onSelectionChanged: ((Set<Int>) -> Unit)? = null
    
    override fun createViewHolder(
        viewType: Int,
        inflater: LayoutInflater,
        parent: ViewGroup
    ): SingleViewHolder<ViewBinding, Any>? {
        return ClassRoomHolder(
            ItemClassInfoBinding.inflate(inflater, parent, false),
            this
        ) as? SingleViewHolder<ViewBinding, Any>
    }
    
    // 获取选中的班级ID列表
    fun getSelectedClassIds(): List<Int> {
        return selectedClassIds.toList()
    }
    
    // 清除所有选择
    fun clearSelection() {
        selectedClassIds.clear()
        notifyDataSetChanged()
        onSelectionChanged?.invoke(selectedClassIds)
    }

    class ClassRoomHolder(
        view: ItemClassInfoBinding,
        private val adapter: ClassAdapter
    ):SingleViewHolder<ItemClassInfoBinding, Classroom>(view) {
        override fun setHolder(entity: Classroom) {
            view.tvClassName.text = entity.className
            view.tvClassInfo.text = entity.classId.toString()
            
            // 设置选择框状态
            view.cbSelect.isChecked = adapter.selectedClassIds.contains(entity.classId)
            
            // 设置整个item的点击事件
            view.root.setOnClickListener {
                val isSelected = adapter.selectedClassIds.contains(entity.classId)
                if (isSelected) {
                    adapter.selectedClassIds.remove(entity.classId)
                } else {
                    adapter.selectedClassIds.add(entity.classId)
                }
                view.cbSelect.isChecked = !isSelected
                adapter.onSelectionChanged?.invoke(adapter.selectedClassIds)
            }
        }
        override fun setHolder(entity: Classroom, payload: Any) {
        }

    }

}