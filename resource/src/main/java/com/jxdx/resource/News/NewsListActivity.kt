package com.jxdx.resource.News

import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.corekit.common.BaseActivity
import com.jxdx.resource.databinding.ActivityNewsListBinding
import org.jxxy.debug.h5.activity.WebViewActivity

class NewsListActivity : BaseActivity<ActivityNewsListBinding>() {
    private lateinit var viewModel: NewVIewModel
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var newsList: List<NewsItem>
    private lateinit var recycle: RecyclerView
    override fun bindLayout(): ActivityNewsListBinding {
        return ActivityNewsListBinding.inflate(layoutInflater)
    }

    override fun initView() {
        viewModel = ViewModelProvider(this)[NewVIewModel::class.java]
        newsAdapter = NewsAdapter()
        recycle = view.newsList
        getNewsList()
        Log.d("获取资讯列表","success")
        recycle.adapter = newsAdapter
        recycle.layoutManager = LinearLayoutManager(this)
        newsAdapter.onItemClickListener = { item ->
            viewModel.getNewsDetail(item.id)
        }
        view.toolbar.title = "新闻列表"
        setupCommonControls()
    }
    override fun subscribeUi() {
        viewModel.newsLiveData.observe(this) { result ->
                result.onSuccess { data ->
                        Log.d("获取资讯列表","${data}")
                        if (data != null) {
                            newsList = data
                            newsAdapter.updateData(newsList)
                            Log.d("获取资讯列表","${ data}")
                    }
                }
            }
        viewModel.detailLiveData.observe(this) { result ->
            result.onSuccess { data ->
                if (data != null) {
                    WebViewActivity.actionStart(this, data.summary, data.title)
                }
            }
        }
    }
    private fun getNewsList() {
        viewModel.getNewsList()
        Log.d("获取资讯列表","form viewmodel")
    }
    private fun setupCommonControls() {
        view.apply {
            // 设置工具栏返回按钮
            toolbar.setNavigationOnClickListener {
                onBackPressed()
            }
        }
    }

}