package org.jxxy.debug.h5.adapter.viewbinder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.drakeet.multitype.ItemViewBinder
import com.example.corekit.util.load
import com.example.corekit.util.singleClick
import com.jxdx.resource.databinding.ItemToolCardBinding
import com.lxj.xpopup.util.XPopupUtils
import org.jxxy.debug.h5.activity.ToolActivity
import org.jxxy.debug.h5.model.ToolCard

class ToolCardViewBinder : ItemViewBinder<ToolCard, ToolCardViewBinder.ViewHolder>() {
    inner class ViewHolder(binding: ItemToolCardBinding) : RecyclerView.ViewHolder(binding.root) {
        val root = binding.root
        val container = binding.container
        val image = binding.toolIv
        val title = binding.toolNameTv
    }

    override fun onBindViewHolder(holder: ViewHolder, item: ToolCard) {
        holder.apply {
            image.apply {
                load(item.image)
                layoutParams.apply {
                    width = XPopupUtils.getScreenWidth(itemView.context) / 2
                    height = XPopupUtils.getScreenWidth(itemView.context) / 2
                }
            }
            title.text = item.title
            root.singleClick {
                ToolActivity.actionStart(holder.itemView.context, item.path)
            }
        }
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        val view = ItemToolCardBinding.inflate(inflater, parent, false)
        return ViewHolder(view)
    }
}