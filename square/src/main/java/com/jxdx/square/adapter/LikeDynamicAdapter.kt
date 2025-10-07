package com.jxdx.square.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.example.corekit.recyclerview.SingleTypeAdapter
import com.example.corekit.recyclerview.SingleViewHolder
import com.jxdx.square.R
import com.jxdx.square.databinding.LikeItemBinding

class LikeDynamicAdapter:SingleTypeAdapter<String>() {
    override fun createViewHolder(
        viewType: Int,
        inflater: LayoutInflater,
        parent: ViewGroup
    ): SingleViewHolder<ViewBinding, Any>? {
        return LikeTypeViewHolder(
            LikeItemBinding.inflate(inflater, parent, false)
        ) as? SingleViewHolder<ViewBinding, Any>
    }

    class LikeTypeViewHolder(view: LikeItemBinding): SingleViewHolder<LikeItemBinding, Any>(view) {

        override fun setHolder(entity: Any) {
            view.tvLikeContent.text = entity.toString()
            view.ivAvatar.setImageResource(R.drawable.ic_avatar)
        }


    }

}