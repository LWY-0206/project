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
            "https://classroom-interaction.oss-cn-hangzhou.aliyuncs.com/%E8%BD%AE%E6%92%AD%E5%9B%BE/%E5%AE%89%E5%8D%93%E7%95%8C%E9%9D%A2%E5%BC%80%E5%8F%91%20%282%29.png?Expires=1758705206&OSSAccessKeyId=TMP.3KmGefmTYJr9VBcL1u72ZBuPCvRb6rV8qCjPheumURttoJdEvQ8nCiZyPNtk2HVbzAVWyVdVBHWRjuryY4nYHZUQkjzwWC&Signature=LcnAmvvwkfhdrFkkO3MyaK7Oa0k%3D&x-oss-request-payer=requester"
        ) // 替换为实际图片 URL
        imageUrls.add(
            "https://classroom-interaction.oss-cn-hangzhou.aliyuncs.com/%E8%BD%AE%E6%92%AD%E5%9B%BE/%E5%AE%89%E5%8D%93%E7%95%8C%E9%9D%A2%E5%BC%80%E5%8F%91%20%281%29.png?Expires=1758705849&OSSAccessKeyId=TMP.3KmGefmTYJr9VBcL1u72ZBuPCvRb6rV8qCjPheumURttoJdEvQ8nCiZyPNtk2HVbzAVWyVdVBHWRjuryY4nYHZUQkjzwWC&Signature=lSZVx5kJoU3KH6rrt05voPp439Y%3D&x-oss-request-payer=requester"
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
            StaggeredItem("https://classroom-interaction.oss-cn-hangzhou.aliyuncs.com/6eb67003-bf57-476a-95d4-164185bbe585.png?Expires=1758180374&OSSAccessKeyId=TMP.3KmTD5G3H5bUGu1uheE2QxEByUn4sK9srcCa4gGvZsy3zn8vKZzeY86JMyzhQtx8sDPut5U1FcQT2TArF7p7vZHnPP1k8U&Signature=niV7AjFEVWuVDJ9h5kbctBC%2Bbu0%3D&x-oss-request-payer=requester",
                "高中优质数学资源",
                "100讲基础必看"
            )
        )
        dataList.add(
            StaggeredItem(
                "https://classroom-interaction.oss-cn-hangzhou.aliyuncs.com/6eb67003-bf57-476a-95d4-164185bbe585.png?Expires=1758180374&OSSAccessKeyId=TMP.3KmTD5G3H5bUGu1uheE2QxEByUn4sK9srcCa4gGvZsy3zn8vKZzeY86JMyzhQtx8sDPut5U1FcQT2TArF7p7vZHnPP1k8U&Signature=niV7AjFEVWuVDJ9h5kbctBC%2Bbu0%3D&x-oss-request-payer=requester",
                "联考试卷",
                "2025优质模拟"
            )
        )
        dataList.add(
            StaggeredItem("https://classroom-interaction.oss-cn-hangzhou.aliyuncs.com/6eb67003-bf57-476a-95d4-164185bbe585.png?Expires=1758180374&OSSAccessKeyId=TMP.3KmTD5G3H5bUGu1uheE2QxEByUn4sK9srcCa4gGvZsy3zn8vKZzeY86JMyzhQtx8sDPut5U1FcQT2TArF7p7vZHnPP1k8U&Signature=niV7AjFEVWuVDJ9h5kbctBC%2Bbu0%3D&x-oss-request-payer=requester","数栋优质题库",
                "分类汇编"
            )
        )
        // 可继续添加更多条目...
    }
}