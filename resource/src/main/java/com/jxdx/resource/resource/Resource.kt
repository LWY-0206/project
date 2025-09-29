package com.jxdx.resource.resource

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.corekit.common.BaseFragment
import com.jxdx.resource.Famous.FamousListActivity
import com.jxdx.resource.Questions.QuestionSelectionActivity
import com.jxdx.resource.Schools.SchoolListActivity
import com.jxdx.resource.databinding.FragmentResourceBinding
import com.youth.banner.Banner


class Resource : BaseFragment<FragmentResourceBinding>() {
    private var topBanner: Banner? = null
    private lateinit var rvStaggered: RecyclerView
    private lateinit var adapter: StaggeredAdapter
    private val dataList: MutableList<StaggeredItem> = ArrayList() // 去掉可空泛型
    override fun bindLayout(): FragmentResourceBinding {
        return FragmentResourceBinding.inflate(layoutInflater)
    }

    override fun initView() {
        topBanner = find.topBanner
        val imageUrls: MutableList<String?> = ArrayList<String?>()
        imageUrls.add(
            "https://classroom-interaction.oss-cn-hangzhou.aliyuncs.com/updateFiles/8dadc437-9f67-412c-b057-6a902b25e438.png") // 替换为实际图片 URL
        imageUrls.add(
            "https://classroom-interaction.oss-cn-hangzhou.aliyuncs.com/updateFiles/f97fba60-a22b-431d-a55b-a5c182963bc5.png"
        )
        topBanner?.setImages(imageUrls)      // 设置图片列表
            ?.setImageLoader(GlideImageLoader()) // 设置图片加载器
            ?.isAutoPlay(true)           // 开启自动轮播
            ?.setDelayTime(3000)         // 轮播间隔（毫秒）
            ?.start();                   // 启动轮播
        prepareTestData()
        adapter = StaggeredAdapter(requireContext(), dataList)
        rvStaggered = find.rvStaggered
        rvStaggered.adapter = adapter

        val layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        rvStaggered.setLayoutManager(layoutManager)
        val famous = find.tvFamous.setOnClickListener {
            val intent = Intent(requireContext(), FamousListActivity::class.java)
            startActivity(intent)
        }
        val quiz = find.tvQuiz.setOnClickListener {
            val intent = Intent(requireContext(), QuestionSelectionActivity::class.java)
            startActivity(intent)
        }
        val test = find.tvSchools.setOnClickListener {
            val intent = Intent(requireContext(), SchoolListActivity::class.java)
            startActivity(intent)
        }
    }

    override fun subscribeUi() {
    }

    private fun prepareTestData() {
        dataList.add(
            StaggeredItem("https://classroom-interaction.oss-cn-hangzhou.aliyuncs.com/updateFiles/58765522-815b-4191-aeb5-9b8a552ba891.png",
                "高中优质数学资源",
                "100讲基础必看"
            )
        )
        dataList.add(
            StaggeredItem(
                "https://classroom-interaction.oss-cn-hangzhou.aliyuncs.com/updateFiles/58765522-815b-4191-aeb5-9b8a552ba891.png",
                "联考试卷",
                "2025优质模拟"
            )
        )
        dataList.add(
            StaggeredItem("https://classroom-interaction.oss-cn-hangzhou.aliyuncs.com/updateFiles/58765522-815b-4191-aeb5-9b8a552ba891.png","数栋优质题库",
                "分类汇编"
            )
        )
        // 可继续添加更多条目...
    }
}