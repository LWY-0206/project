package com.example.loding.Schools

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.corekit.common.BaseActivity
import com.example.corekit.http.bean.Resource
import com.example.loding.R
import com.example.loding.databinding.ActivitySchoolDetailBinding

class SchoolDetailActivity : BaseActivity<ActivitySchoolDetailBinding>() {

    private lateinit var viewModel: SchoolDetailViewModel
    private var schoolId: Int = -1

    override fun bindLayout(): ActivitySchoolDetailBinding {
        return ActivitySchoolDetailBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 隐藏状态栏
    }

    override fun initView() {
        // 初始化 ViewModel
        viewModel = ViewModelProvider(this)[SchoolDetailViewModel::class.java]
        schoolId = intent.getIntExtra("SCHOOL_ID", -1)
        Log.d("SchoolDetailActivity", "获取到的学校ID: $schoolId")
        if (schoolId == -1) {
            Log.e("SchoolDetailActivity", "未获取到有效的学校ID")
            finish()
            return
        }


        // 加载学校详情
        loadSchoolDetail()
    }

    override fun subscribeUi() {
        viewModel.schoolDetailLiveData.observe(this) { resource ->
            // 使用 Resource 类提供的 onSuccess 和 onError 方法
            resource
                .onSuccess { data ->
                    // 成功状态处理
                    view.progressBar.visibility = View.GONE
                    data?.let { schoolDetail ->
                        bindSchoolDetail(schoolDetail)
                        Log.d("SchoolDetailActivity", "学校详情加载成功: ${schoolDetail.schoolName}")
                    } ?: run {
                        Log.w("SchoolDetailActivity", "数据加载成功但data为null")
                        showErrorView("未获取到学校详情数据")
                    }
                }
                .onError { error, data ->
                    // 错误状态处理
                    view.progressBar.visibility = View.GONE

                    // 检查是否有数据在错误响应中
                    if (data != null) {
                        // 即使请求状态是错误，但如果有数据，仍然显示
                        bindSchoolDetail(data)
                        Toast.makeText(this, "注意: ${error?.message}", Toast.LENGTH_SHORT).show()
                    } else {
                        showErrorView("加载失败: ${error?.message}")
                        Log.e("SchoolDetailActivity", "学校详情加载失败: $error")
                    }
                }
        }
    }

    private fun loadSchoolDetail() {
        // 显示加载进度条
        view.progressBar.visibility = View.VISIBLE
        view.errorView.visibility = View.GONE
        view.contentView.visibility = View.GONE

        viewModel.getSchoolDetail(schoolId)
    }

    private fun bindSchoolDetail(schoolDetail: SchoolDetail) {
        // 显示内容视图
        view.contentView.visibility = View.VISIBLE
        view.errorView.visibility = View.GONE

        // 设置学校基本信息
        view.tvSchoolName.text = schoolDetail.schoolName
        view.tvSimpleAddress.text = schoolDetail.simpleAddress
        view.tvDetailedAddress.text = schoolDetail.detailedAddress
        view.tvSchoolProfile.text = schoolDetail.schoolProfile

        // 设置985/211标识
        if (schoolDetail.is985) {
            view.tv985.visibility = View.VISIBLE
        } else {
            view.tv985.visibility = View.GONE
        }

        if (schoolDetail.is211) {
            view.tv211.visibility = View.VISIBLE
        } else {
            view.tv211.visibility = View.GONE
        }

        // 设置历年分数线表格
        view.tvScoreThisYear.text = schoolDetail.schoolScoreThisYear.toString()
        view.tvScoreLastYear.text = schoolDetail.schoolScoreLastYear.toString()
        view.tvScoreLastLastYear.text = schoolDetail.schoolScoreLastLastYear.toString()
        view.btnClose.setOnClickListener {
            //返回上一个activity
            finish()
        }
        // 使用Glide加载校徽
        Glide.with(this)
            .load(schoolDetail.emblemUrl)
            .into(view.ivEmblem)
    }
    private fun showErrorView(errorMessage: String) {
        view.errorView.visibility = View.VISIBLE
        view.contentView.visibility = View.GONE
        view.tvErrorMessage.text = errorMessage
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("SchoolDetailActivity", "Activity销毁")
    }
}