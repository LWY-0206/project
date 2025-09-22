package com.jxdx.mine.service
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import com.jxdx.mine.UserInfo
import com.jxdx.mine.databinding.ActivityEditProfile2Binding
import com.jxdx.mine.http.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfile2Binding


    // 从Intent获取的原始简介内容
    private var originalBio: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfile2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        // 设置保存按钮点击事件
        setupSaveButton()

        // 设置返回按钮点击事件
        setupBackButton()

        setupTextWatcher()
        RetrofitClient.apiService.getUserInfo().enqueue(object : Callback<BaseResp<UserInfo>> {
            override fun onResponse(
                call: Call<BaseResp<UserInfo>?>,
                response: Response<BaseResp<UserInfo>?>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.code == 0) {
                            binding.profile.setText(it.data?.profile)
                        } else {
                            Toast.makeText(
                                this@EditProfileActivity,
                                "获取用户信息失败",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(this@EditProfileActivity, "获取用户信息失败", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(
                call: Call<BaseResp<UserInfo>?>,
                t: Throwable
            ) {
                Toast.makeText(this@EditProfileActivity, "网络连接失败", Toast.LENGTH_SHORT).show()
            }

        })
    }

    // 设置返回按钮点击事件
    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupTextWatcher() {
        binding.profile.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 不需要实现
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 不需要实现
            }

            override fun afterTextChanged(s: Editable?) {
                val currentText = s.toString()
                val currentLength = currentText.length

                // 如果超过限制，截断文本
                if (currentLength > MAX_BIO_LENGTH) {
                    binding.profile.setText(currentText.substring(0, MAX_BIO_LENGTH))
                    binding.profile.setSelection(MAX_BIO_LENGTH) // 将光标移动到末尾
                } else {
                    updateWordCount(currentLength)
                }
            }
        })
    }
    private fun updateWordCount(count: Int) {
        binding.tvWordCount.text = "$count/$MAX_BIO_LENGTH"

        // 当接近字数限制时改变颜色提示
        if (count > MAX_BIO_LENGTH * 0.8) {
            binding.tvWordCount.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
        } else {
            binding.tvWordCount.setTextColor(resources.getColor(android.R.color.darker_gray, null))
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val newBio = binding.profile.text.toString().trim()

            // 验证字数
            if (newBio.length > MAX_BIO_LENGTH) {
                Toast.makeText(this, "个人简介不能超过${MAX_BIO_LENGTH}字", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            RetrofitClient.apiService.updateProfile(newBio, TokenManager.getToken()).enqueue(object : Callback<BaseResp<String>> {
                override fun onResponse(
                    call: Call<BaseResp<String>?>,
                    response: Response<BaseResp<String>?>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            if (it.code == 0) {
                                Toast.makeText(
                                    this@EditProfileActivity,
                                    "个人简介更新成功",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@EditProfileActivity,
                                    it.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        Toast.makeText(
                            this@EditProfileActivity,
                            "获取用户信息失败，错误: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                override fun onFailure(
                    call: Call<BaseResp<String>?>,
                    t: Throwable
                ) {
                    Toast.makeText(this@EditProfileActivity, "网络连接失败，请检查网络设置", Toast.LENGTH_SHORT).show()
                }
            })

            // 返回结果给上一个Activity
            val resultIntent = Intent().apply {
                putExtra("newBio", newBio)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
    companion object {
        const val MAX_BIO_LENGTH = 150
    }
    // 添加返回按钮点击事件
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}