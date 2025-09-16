package com.example.loding.Login

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.example.loding.Home.Home
import com.example.loding.R
import com.example.loding.databinding.ActivityRegisterBinding

// 注册页
class RegisterActivity : AppCompatActivity() {
    private var _binding: ActivityRegisterBinding? = null
    private val binding get() = _binding!!
    private val gradeList =
        arrayOf(
            "软件111",
            "软件112",
            "软件113",
            "软件114",
            "软件115",
            "软件116",
            "计科111",
            "计科112",
            "计科113",
            "计科114",
            "计科115",
            "计科116",
        )
    private var isTeacher: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGradeDropdown()
        setupListeners()
    }

    private fun setupGradeDropdown() {
        // 创建适配器
        val adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                gradeList,
            )

        // 设置适配器到AutoCompleteTextView
        binding.etGrade.setAdapter(adapter)

        // 设置项目点击监听
        binding.etGrade.setOnItemClickListener { parent, view, position, id ->
            val selectedGrade = parent.getItemAtPosition(position) as String
            // 处理选中的年级
            binding.etGrade.setText(selectedGrade, false)
        }
        // 设置默认值（可选）
        binding.etGrade.setText("软件111", false) // false表示不触发过滤
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            if (validateInput()) {
                performRegistration()
            }
        }

        binding.tvLogin.setOnClickListener {
            // 跳转到登录页面
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun validateInput(): Boolean {
        // 获取选中的身份
        val selectedId = binding.rgIdentity.checkedRadioButtonId
        isTeacher = (selectedId == R.id.rb_teacher)

        val username =
            binding.etUsername.text
                .toString()
                .trim()
        val grade =
            binding.etGrade.text
                .toString()
                .trim()
        val phone =
            binding.etPhone.text
                .toString()
                .trim()
        val password =
            binding.etPassword.text
                .toString()
                .trim()
        val confirmPassword =
            binding.etConfirmPassword.text
                .toString()
                .trim()

        var isValid = true

        // 验证用户名
        if (username.isEmpty()) {
            binding.tilUsername.error = "用户名不能为空"
            isValid = false
        } else if (username.length < 3) {
            binding.tilUsername.error = "用户名长度不能少于3位"
            isValid = false
        } else {
            binding.tilUsername.error = null
        }

        // 验证手机号
        if (phone.isEmpty()) {
            binding.tilPhone.error = "手机号不能为空"
            isValid = false
        } else if (!isValidPhone(phone)) {
            binding.tilPhone.error = "请输入有效的手机号码"
            isValid = false
        } else {
            binding.tilPhone.error = null
        }

        // 验证密码
        if (password.isEmpty()) {
            binding.tilPassword.error = "密码不能为空"
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "密码长度不能少于6位"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        // 验证确认密码
        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = "请确认密码"
            isValid = false
        } else if (confirmPassword != password) {
            binding.tilConfirmPassword.error = "两次输入的密码不一致"
            isValid = false
        } else {
            binding.tilConfirmPassword.error = null
        }
        return isValid
    }

    private fun isValidPhone(phone: String): Boolean = phone.length == 11

    // 保存注册信息
    private fun performRegistration() {
        val username =
            binding.etUsername.text
                .toString()
                .trim()
        val grade =
            binding.etGrade.text
                .toString()
                .trim()
        val phone =
            binding.etPhone.text
                .toString()
                .trim()
        val password =
            binding.etPassword.text
                .toString()
                .trim()

        // 显示加载对话框
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("注册中...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        // 模拟网络请求
        Handler(Looper.getMainLooper()).postDelayed({
            progressDialog.dismiss()

            // 模拟注册成功
            // 在实际应用中，这里应该发送注册请求到服务器
            // 并处理服务器的响应

            // 保存用户信息（模拟注册成功）
            val preferences: SharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE) // MODE_PRIVATE私有模式
            // 写入数据
            preferences.edit {
                // 使用用户名作为key的前缀来存储用户信息
                if (isTeacher) {
                    putString("${username}_identity", "教师")
                } else {
                    putString("${username}_identity", "学生")
                }

                putString("${username}_grade", grade)
                putString("${username}_phone", phone)
                putString("${username}_password", password)

                putBoolean("is_logged_in", true)
                // 登录状态个人主页可能会需要的个人信息
                putString("username", username)

                // 提交更改apply(): 异步写入磁盘，不会阻塞UI线程，没有返回值。
                // commit(): 同步写入磁盘，会阻塞UI线程直到写入完成，并返回一个 boolean 值表示成功与否。
            }

            Toast.makeText(this@RegisterActivity, "注册成功", Toast.LENGTH_SHORT).show()

            // 后续应该是直接跳转到进入应用的首页
            // 这里跳转到主页
            if (isTeacher) {
                // 跳转到教师端
                val intent = Intent(this@RegisterActivity, Home::class.java)
                startActivity(intent)
                Toast.makeText(this@RegisterActivity, "已登录教师端", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                // 跳转到学生端
                val intent = Intent(this@RegisterActivity, Home::class.java)
                startActivity(intent)
                Toast.makeText(this@RegisterActivity, "已登录学生端", Toast.LENGTH_SHORT).show()
                finish()
            }
        }, 1500)
    }
}
