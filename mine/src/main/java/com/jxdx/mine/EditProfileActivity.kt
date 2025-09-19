package com.jxdx.mine.service
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.jxdx.mine.R


class EditProfileActivity : AppCompatActivity() {

    private lateinit var etBio: EditText
    private lateinit var tvWordCount: TextView
    private lateinit var btnSave: Button

    // 从Intent获取的原始简介内容
    private var originalBio: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // 初始化视图
        initViews()

        // 设置文本变化监听器
        setupTextWatcher()

        // 设置保存按钮点击事件
        setupSaveButton()

        // 获取传递过来的原始简介（如果有）
        val preferences: SharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userName=preferences.getString("username","用户名")
        val currentBio = preferences.getString("${userName}_bio", "个人简介") ?: "个人简介"

        etBio.setText(currentBio)
        updateWordCount(currentBio.length)
    }

    private fun initViews() {
        etBio = findViewById(R.id.etBio)
        tvWordCount = findViewById(R.id.tvWordCount)
        btnSave = findViewById(R.id.btnSave)
    }

    private fun setupTextWatcher() {
        etBio.addTextChangedListener(object : TextWatcher {
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
                    etBio.setText(currentText.substring(0, MAX_BIO_LENGTH))
                    etBio.setSelection(MAX_BIO_LENGTH) // 将光标移动到末尾
                } else {
                    updateWordCount(currentLength)
                }
            }
        })
    }

    private fun updateWordCount(count: Int) {
        tvWordCount.text = "$count/$MAX_BIO_LENGTH"

        // 当接近字数限制时改变颜色提示
        if (count > MAX_BIO_LENGTH * 0.8) {
            tvWordCount.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
        } else {
            tvWordCount.setTextColor(resources.getColor(android.R.color.darker_gray, null))
        }
    }

    private fun setupSaveButton() {
        btnSave.setOnClickListener {
            val newBio = etBio.text.toString().trim()

            // 验证字数
            if (newBio.length > MAX_BIO_LENGTH) {
                Toast.makeText(this, "个人简介不能超过${MAX_BIO_LENGTH}字", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 保存逻辑 - 这里可以添加保存到数据库或服务器的代码
            saveBio(newBio)

            // 返回结果给上一个Activity
            val resultIntent = Intent().apply {
                putExtra("newBio", newBio)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun saveBio(bio: String) {
        // 这里实现保存逻辑，例如保存到SharedPreferences或发送到服务器
        val preferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userName = preferences.getString("username", "用户名")
        preferences.edit()
        {
            putString("${userName}_bio", bio)
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