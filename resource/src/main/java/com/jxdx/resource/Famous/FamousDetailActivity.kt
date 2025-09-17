package com.jxdx.resource.Famous
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.corekit.common.BaseActivity
import com.jxdx.resource.R
import com.jxdx.resource.databinding.ActivityFamousDetailBinding
import com.jxdx.resource.famousChat.FamousChatActivity

class FamousDetailActivity : BaseActivity<ActivityFamousDetailBinding>() {

    private lateinit var viewModel: FamousDetailViewModel
    private var celebrityId: Int = -1
    private lateinit var famousdetail: FamousDetail
    override fun bindLayout(): ActivityFamousDetailBinding {
        return ActivityFamousDetailBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 隐藏状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        // 设置内容延伸到状态栏区域
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    )
        }

        // 设置状态栏颜色为透明
        window.statusBarColor = Color.TRANSPARENT
    }

    override fun initView() {
        // 初始化 ViewModel
        viewModel = ViewModelProvider(this)[FamousDetailViewModel::class.java]

        // 设置退出按钮点击监听

        celebrityId = intent.getIntExtra("CELEBRITY_ID", -1)
        if (celebrityId == -1) {
            Log.e("FamousDetailActivity", "未获取到有效的名人ID")
            finish()
            return
        }
        // 加载名人详情
        loadFamousDetail()
    }

    override fun subscribeUi() {
        viewModel.famousDetailLiveData.observe(this) { resource ->
            // 使用 Resource 类提供的 onSuccess 和 onError 方法
            resource
                .onSuccess { data ->
                    // 成功状态处理
                    view.progressBar.visibility = View.GONE
                    data?.let { famousDetail ->
                        bindFamousDetail(famousDetail)
                        Log.d("FamousDetailActivity", "名人详情加载成功: ${famousDetail.celebrityName}")
                    } ?: run {
                        Log.w("FamousDetailActivity", "数据加载成功但data为null")
                        showErrorView("未获取到名人详情数据")
                    }
                }
                .onError { error, data ->
                    // 错误状态处理
                    view.progressBar.visibility = View.GONE

                    // 检查是否有数据在错误响应中
                    if (data != null) {
                        // 即使请求状态是错误，但如果有数据，仍然显示
                        bindFamousDetail(data)
                        Toast.makeText(this, "注意: ${error?.message}", Toast.LENGTH_SHORT).show()
                    } else {
                        showErrorView("加载失败: ${error?.message}")
                        Log.e("FamousDetailActivity", "名人详情加载失败: $error")
                    }
                }

        }
    }

    private fun loadFamousDetail() {
        // 显示加载进度条
        view.progressBar.visibility = View.VISIBLE
        view.errorView.visibility = View.GONE
        view.contentView.visibility = View.GONE

        viewModel.getFamousDetail(celebrityId)
    }

    private fun bindFamousDetail(famousDetail: FamousDetail) {
        famousdetail = famousDetail
        // 显示内容视图
        view.contentView.visibility = View.VISIBLE
        view.errorView.visibility = View.GONE

        // 设置名人基本信息
        view.tvCelebrityName.text = famousDetail.celebrityName
        view.tvProfession.text = famousDetail.profession
        view.tvEra.text = famousDetail.era
        view.tvDescription.text = famousDetail.description

        view.btnTalk.setOnClickListener {
            val intent = Intent(this, FamousChatActivity::class.java)
            intent.putExtra("CELEBRITY_ID", famousDetail.celebrityId)
            startActivity(intent)
        }
        // 使用Glide加载头像
        Glide.with(this)
            .load(famousDetail.avatarUrl)
            .placeholder(R.drawable.ic_person_placeholder)
            .error(R.drawable.ic_person_placeholder)
            .into(view.ivAvatar)
    }
    private fun showErrorView(errorMessage: String) {
        view.errorView.visibility = View.VISIBLE
        view.contentView.visibility = View.GONE
        view.tvErrorMessage.text = errorMessage

        // 设置错误视图的重试按钮
        view.btnRetry.setOnClickListener {
            loadFamousDetail()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("FamousDetailActivity", "Activity销毁")
    }
}