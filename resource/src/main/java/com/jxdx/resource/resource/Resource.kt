package com.jxdx.resource.resource

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.corekit.common.BaseFragment
import com.jxdx.resource.Famous.FamousListActivity
import com.jxdx.resource.Questions.QuestionSelectionActivity
import com.jxdx.resource.Recommendation.RecommendationItem
import com.jxdx.resource.Recommendation.RecommendationViewModel
import com.jxdx.resource.Recommendation.WaterfallAdapter
import com.jxdx.resource.Schools.SchoolListActivity
import com.jxdx.resource.databinding.FragmentResourceBinding
import com.tencent.smtt.utils.e
import com.youth.banner.Banner
import org.jxxy.debug.h5.activity.GeoGebraActivity
import org.jxxy.debug.h5.activity.ToolActivity
import org.jxxy.debug.h5.activity.WebViewActivity


class Resource : BaseFragment<FragmentResourceBinding>() {
    private lateinit var waterfallRecyclerView: RecyclerView
    private var topBanner: Banner? = null
    private lateinit var rvStaggered: RecyclerView
    private lateinit var waterfallAdapter: WaterfallAdapter
    private val recommendationList = mutableListOf<RecommendationItem>()
    private lateinit var recommendationViewModel: RecommendationViewModel
    override fun bindLayout(): FragmentResourceBinding {
        return FragmentResourceBinding.inflate(layoutInflater)
    }

    override fun initView() {
        recommendationViewModel = ViewModelProvider(this)[RecommendationViewModel::class.java]
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
        val famous = find.tvFamous.setOnClickListener {
            val intent = Intent(requireContext(), FamousListActivity::class.java)
            Log.d("Resource", "跳转名人")
            startActivity(intent)
        }
        val quiz = find.tvQuiz.setOnClickListener {
            val intent = Intent(requireContext(), QuestionSelectionActivity::class.java)
            Log.d("Resource", "跳转错题")
            startActivity(intent)
        }
        val test = find.tvSchools.setOnClickListener {
            val intent = Intent(requireContext(), SchoolListActivity::class.java)
            Log.d("Resource", "跳转学校")
            startActivity(intent)
        }
        val Math=find.tvMath.setOnClickListener {
            val intent = Intent(requireContext(), GeoGebraActivity::class.java)
            Log.d("Resource", "跳转工具")
            startActivity(intent)
        }
        val chem=find.tvChem.setOnClickListener {
            val intent = Intent(requireContext(), ToolActivity::class.java)
            Log.d("Resource", "跳转工具")
            startActivity(intent)
        }
        setupWaterfallRecyclerView()
    }

    override fun subscribeUi() {
        recommendationViewModel.recommendationLiveData.observe(this) { result ->
            result.onSuccess { data ->
                if (data != null) {
                    recommendationList.clear()
                    recommendationList.addAll(data)
                   waterfallAdapter.updateData(recommendationList)
                    Log.d("RecommendationViewModel", "Data loaded successfully, size: ${data}")
                } else {
                }
            }
        }
        recommendationViewModel.recommendationDetailLiveData.observe(this) { result ->
            result.onSuccess { data ->
                if (data != null) {
                    WebViewActivity.actionStart(requireActivity(), data.fileUrl, data.title)
                }
            }
        }

    }
    private fun setupWaterfallRecyclerView() {
        waterfallRecyclerView = find.rvStaggered
        // 设置瀑布流布局管理器，2列
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        waterfallRecyclerView.layoutManager = layoutManager

        // 创建适配器
        waterfallAdapter = WaterfallAdapter(recommendationList)
        waterfallRecyclerView.adapter = waterfallAdapter

        // 设置点击事件
        waterfallAdapter.onItemClickListener = { item ->
            handleItemClick( item)
        }
        // 添加滚动监听实现加载更多
        waterfallRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) {
                    Toast.makeText(requireContext(), "已经到底啦！！！", Toast.LENGTH_SHORT).show()
                }
            }
        })

        // 加载推荐数据
        loadRecommendationData()
    }
    private fun loadRecommendationData() {
        Log.d("Recommendation", "开始加载推荐数据")
        recommendationViewModel.getRecommendationList(2)
    }
    private fun handleItemClick(item: RecommendationItem) {
        // 根据类型处理点击事件
        if (item.isVideo()) {
            recommendationViewModel.getRecommendationDetail(item.id.toInt())
        } else {
            recommendationViewModel.getRecommendationDetail(item.id.toInt())
        }
    }

}