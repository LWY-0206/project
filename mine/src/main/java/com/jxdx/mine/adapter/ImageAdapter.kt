package com.jxdx.mine.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.jxdx.mine.R
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 用于显示图片列表的适配器
 */
class ImageAdapter(private val context: Context) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    private var imageUrls: List<String> = emptyList()
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    fun setImageUrls(urls: List<String>) {
        this.imageUrls = urls
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imageUrls[position]
        
        // 设置默认占位图
        holder.imageView.setImageResource(R.drawable.homework)
        
        // 使用线程池加载图片
        executorService.execute {
            try {
                val bitmap = loadImageFromUrl(imageUrl)
                bitmap?.let {
                    // 在UI线程中设置图片
                    holder.imageView.post {
                        holder.imageView.setImageBitmap(it)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // 加载失败时设置错误图片
                holder.imageView.post {
                    holder.imageView.setImageResource(R.drawable.homework)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return imageUrls.size
    }

    // 从URL加载图片
    private fun loadImageFromUrl(imageUrl: String): Bitmap? {
        return try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.iv_image)
    }
}