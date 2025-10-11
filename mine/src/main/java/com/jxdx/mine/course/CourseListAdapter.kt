package com.jxdx.mine.course

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jxdx.mine.Courseware
import com.jxdx.mine.R

class CourseListAdapter(
    private val onPreviewClick: (Courseware) -> Unit,
    private val onDownloadClick: (Courseware) -> Unit
) : RecyclerView.Adapter<CourseListAdapter.CoursewareViewHolder>() {

    private val items  = mutableListOf<Courseware>()

    fun submitList(list: List<Courseware>?) {
        items .clear()
        if(list!=null){
            items .addAll(list)
        }
        // 无论API返回数据如何，始终添加测试项
//        items.add(Courseware("第一章 引论","https://classroom-interaction.oss-cn-hangzhou.aliyuncs.com/updateFiles/48cd1e0d-f1a4-41ea-b158-9dfbd917f1e2.pptx"))
        notifyDataSetChanged()
    }

    inner class CoursewareViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_course_description)
        private val btnPreview: Button = itemView.findViewById(R.id.btn_preview)
        private val btnDownload: Button = itemView.findViewById(R.id.btn_download)

        fun bind(courseware: Courseware) {
            tvTitle.text = courseware.CoursewareName
            btnPreview.setOnClickListener { onPreviewClick(courseware) }
            btnDownload.setOnClickListener { onDownloadClick(courseware) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoursewareViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_courseware, parent, false)
        return CoursewareViewHolder(view)
    }

    override fun getItemCount() = items .size
    override fun onBindViewHolder(holder: CoursewareViewHolder, position: Int) {
        holder.bind(items [position])
    }
}
