package com.jxdx.classroom.activity

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.example.corekit.recyclerview.SingleTypeAdapter
import com.example.corekit.recyclerview.SingleViewHolder
import com.jxdx.classroom.databinding.ItemSelectclassBinding
import com.jxdx.classroom.entity.SelectClass

class SelectClassAdapter: SingleTypeAdapter<SelectClass>() {
    override fun createViewHolder(
        viewType: Int,
        inflater: LayoutInflater,
        parent: ViewGroup
    ): SingleViewHolder<ViewBinding, Any>? {
        return SelectClassHolder(
            ItemSelectclassBinding.inflate(inflater, parent, false)
        ) as? SingleViewHolder<ViewBinding, Any>
    }

    class SelectClassHolder(view: ItemSelectclassBinding):SingleViewHolder<ItemSelectclassBinding, SelectClass>(view) {
        override fun setHolder(entity: SelectClass) {
            view.tvCourseIdValue.text = entity.subjectId.toString()
            view.tvCourseNameValue.text = entity.subjectName
            
            // 设置整个item的点击事件
            view.root.setOnClickListener {
                val intent = Intent(context, ActivityClassDynamic::class.java)
                intent.putExtra("subjectId", entity.subjectId)
                intent.putExtra("subjectName", entity.subjectName)
                context.startActivity(intent)
            }
        }
        override fun setHolder(entity: SelectClass, payload: Any) {
            setHolder(entity)
        }
    }


}