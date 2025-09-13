package com.example.loding.webtest

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.corekit.common.BaseActivity
import com.example.corekit.util.load
import com.example.loding.R
import com.example.loding.databinding.ActivityUserBinding
import com.example.loding.webtest.UserData
import com.example.loding.webtest.UserViewModel

// 确保 BaseActivity 正确处理数据绑定

class UserTest : BaseActivity<ActivityUserBinding>() {
    private lateinit var viewModel: UserViewModel
    private var currentTokenIndex = 0
    private val tokenList = listOf("token1", "token2", "token3")
    private lateinit var sharedPreferences: SharedPreferences// 用于保存当前 token 索引

    override fun bindLayout(): ActivityUserBinding = ActivityUserBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化 SharedPreferences
        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)//
        // 获取上次保存的 token 索引
        currentTokenIndex = sharedPreferences.getInt("current_token_index", 0)
        // 设置按钮点击事件
        initButtonClick()
    }

    override fun initView() {
        // 初始化 ViewModel
        viewModel = ViewModelProvider(this)[UserViewModel::class.java]
        // 初始请求数据
        fetchUserData()
    }

    override fun subscribeUi() {
        // 观察用户数据变化
        Log.d("UserTest", "开始观察用户数据变化")
        viewModel.userLiveData.observe(this) {
            Log.d("UserTest", "接收到数据变化通知 - Resource状态: ${it.status}, 错误: ${it.error?.message}")

            // 打印完整的Resource对象信息和详细内容
            Log.d("UserTest", "Resource完整信息: $it")
            Log.d("UserTest", "Resource.response 数据: ${it.response}")
            Log.d("UserTest", "Resource.error 数据: 错误类型=${it.error?.errorType}, 错误代码=${it.error?.errorCode}, 错误消息=${it.error?.message}")

            // 无论成功还是错误状态，都尝试获取并显示数据
            // 因为服务器返回的code是200，但框架定义的成功code是0，导致数据被错误标记
            val userData = it.response
            if (userData != null) {
                Log.d("UserTest", "发现可用用户数据: userName=${userData.userName}, userId=${userData.userId}, currentTime=${userData.currentTime}")
                Log.d("UserTest", "准备更新UI，控件引用检查: view=${view != null}")

                // 更新UI显示
                try {
                    view.tvUserName.text = userData.userName
                    view.tvUserId.text = userData.userId.toString()
                    view.tvOnlineTime.text = userData.currentTime

                    // ViewBinding会自动将XML中的下划线命名ID转换为驼峰命名
                    // 因此即使XML中是iv_user_image，这里也应该使用ivUserImage
                    try {
                        // 使用用户ID作为随机参数，确保每次加载不同的图片
                        val imageUrl = "https://tongue-srt.oss-cn-hangzhou.aliyuncs.com/2025/09/05/a0bf2736-b843-4505-ad82-36c38a925f7c.jpg"
                        Log.d("UserTest", "准备加载图片: $imageUrl")

                        // 设置应用内已有的占位图资源
                        view.ivUserImage.setBackgroundResource(R.drawable.ic_avatar)

                        // 加载网络图片
                        view.ivUserImage.load(imageUrl, 1)
                        Log.d("UserTest", "图片加载请求已发送: $imageUrl")
                    } catch (e: Exception) {
                        // 加载失败时设置占位图并记录错误
                        Log.e("UserTest", "图片加载异常: ${e.message}", e)
                        view.ivUserImage.setImageResource(R.drawable.ic_image_placeholder)
                    }

                    Log.d("UserTest", "UI更新完成 - 用户名: ${view.tvUserName.text}, 用户ID: ${view.tvUserId.text}")
                    Toast.makeText(this, "数据加载成功", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e("UserTest", "UI更新异常: ${e.message}", e)
                    Toast.makeText(this, "UI更新失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                // 真正没有数据的情况
                Log.d("UserTest", "userData为null，无法显示数据")
                Log.d("UserTest", "服务器返回状态码: ${it.error?.errorCode}, 消息: ${it.error?.message}")
                Toast.makeText(this, "未获取到用户数据", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initButtonClick() {
        view.btnAction.setOnClickListener {
            // 切换 token 并重新请求数据
            currentTokenIndex = (currentTokenIndex + 1) % tokenList.size
            // 保存当前 token 索引
            sharedPreferences.edit().putInt("current_token_index", currentTokenIndex).apply()
            // 显示切换提示
            Toast
                .makeText(
                    this,
                    "已切换到 token: ${tokenList[currentTokenIndex]}",
                    Toast.LENGTH_SHORT,
                ).show()
            // 重新请求数据
            fetchUserData()
        }
    }

    private fun fetchUserData() {
        // 使用不同的参数来模拟不同的用户数据
        val type =
            if (currentTokenIndex == 0) {
                1
            } else if (currentTokenIndex == 1) {
                2
            } else {
                3
            }
        val a = 10 + currentTokenIndex * 5
        val b = 5 + currentTokenIndex * 3
        viewModel.getUsers(type, a, b)
    }
}
