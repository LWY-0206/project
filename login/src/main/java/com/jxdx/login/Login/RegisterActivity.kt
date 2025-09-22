package com.jxdx.login.Login


import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.corekit.http.bean.BaseResp
import com.github.dhaval2404.imagepicker.ImagePicker
import com.jxdx.login.R
import com.jxdx.login.UserInfo
import com.jxdx.login.databinding.ActivityRegisterBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

// 注册页
class RegisterActivity : AppCompatActivity() {
    private var _binding: ActivityRegisterBinding? = null
    private val binding get() = _binding!!
    private val gradeList = arrayOf(
        "高一1班", "高一2班", "高一3班", "高一4班",
        "高二1班", "高二2班", "高二3班", "高二4班",
        "高三1班", "高三2班","高三3班", "高三4班",
    )
    private var isTeacher: Boolean = false
    private var avatarUri: Uri? = null

    companion object {
        private const val REQUEST_IMAGE_PICK = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGradeDropdown()
        setupListeners()
    }

    //选择年级选项
    private fun setupGradeDropdown() {
        val adapter = ArrayAdapter(
            this,
            R.layout.simple_dropdown_item_1line,
            gradeList
        )
        binding.etGrade.setAdapter(adapter)
        binding.etGrade.setOnItemClickListener { parent, view, position, id ->
            val selectedGrade = parent.getItemAtPosition(position) as String
            binding.etGrade.setText(selectedGrade, false)
        }
        binding.etGrade.setText("软件111", false)
    }

    private fun setupListeners() {
        binding.ivAvatar.setOnClickListener {
            openImagePicker()
        }

        binding.btnRegister.setOnClickListener {
            if (validateInput()) {
                if (avatarUri != null) {
                    uploadAvatarAndRegister()
                } else {
                    performRegistration(null)
                }
            }
        }

        binding.tvLogin.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun openImagePicker() {
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .start(REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == REQUEST_IMAGE_PICK) {
            avatarUri = data?.data
            avatarUri?.let {
                binding.ivAvatar.setImageURI(it)
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, "uri错误"+ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        }
    }

    //检查注册信息
    private fun validateInput(): Boolean {
        val selectedId = binding.rgIdentity.checkedRadioButtonId
        isTeacher = (selectedId == R.id.rb_teacher)

        val username = binding.etUsername.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        var isValid = true

        if (username.isEmpty()) {
            binding.tilUsername.error = "用户名不能为空"
            isValid = false
        } else if (username.length < 3) {
            binding.tilUsername.error = "用户名长度不能少于3位"
            isValid = false
        } else {
            binding.tilUsername.error = null
        }

        if (phone.isEmpty()) {
            binding.tilPhone.error = "手机号不能为空"
            isValid = false
        } else if (phone.length != 11) {
            binding.tilPhone.error = "请输入有效的手机号码"
            isValid = false
        } else {
            binding.tilPhone.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "密码不能为空"
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "密码长度不能少于6位"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

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

    private fun uploadAvatarAndRegister() {
        val progressDialog = ProgressDialog(this).apply {
            setMessage("上传头像中...")
            setCancelable(false)
            show()
        }

        try {
            avatarUri?.let { uri ->
                val fileList = createTempFileFromUri(uri)
                val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), fileList)
                val avatarPart = MultipartBody.Part.createFormData("fileList", fileList.name, requestFile)

                Log.d("---头像文件地址:", requestFile.toString())
                Log.d("---上传头像地址:", avatarPart.toString())


                RetrofitClient.apiService.uploadAvatar(avatarPart)
                    .enqueue(object : Callback<BaseResp<List<String>>> {
                        override fun onResponse(
                            call: Call<BaseResp<List<String>>>,
                            response: Response<BaseResp<List<String>>>
                        ) {
                            progressDialog.dismiss()


                            if (response.isSuccessful) {
                                response.body()?.let { uploadResponse ->
                                    if (uploadResponse.code == 0) {
                                        val imageUrls = uploadResponse.data
                                        imageUrls?.let {
                                            if (it.isNotEmpty()) {
                                                Log.d("---上传成功", "URL: ${it[0]}")
                                                performRegistration(it[0])
                                            } else {
                                                Log.d("---上传成功但无URL", uploadResponse.message.toString())
                                                performRegistration(null)
                                            }
                                        }
                                    } else {
                                        Log.d("---Response.code == ", uploadResponse.code.toString())
                                        Log.d("---业务失败", uploadResponse.message.toString())
                                        performRegistration(null)
                                    }
                                } ?: run {
                                    performRegistration(null)
                                }
                            } else {
                                performRegistration(null)
                            }
                        }

                        override fun onFailure(
                            call: Call<BaseResp<List<String>>>,
                            t: Throwable)
                        {
                            progressDialog.dismiss()
                            Log.e("---网络错误", t.message ?: "未知错误")
                            performRegistration(null)
                        }
                    })

            } ?: run {
                progressDialog.dismiss()
                // 用户没有选择头像，直接注册
                performRegistration(null)
            }
        } catch (e: Exception) {
            progressDialog.dismiss()
            Log.e("---文件处理错误", e.message ?: "未知错误")
            performRegistration(null)
        }
    }

    // 更可靠的文件创建方法
    private fun createTempFileFromUri(uri: Uri): File {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val file = File.createTempFile("avatar_${System.currentTimeMillis()}", ".jpg", cacheDir)

        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

    //保存注册信息
    private fun performRegistration(avatarUrl: String?) {
        val userName = binding.etUsername.text.toString().trim()
        val className = binding.etGrade.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val identity = if (isTeacher) 1 else 0     //身份：1-老师，0-学生

        val progressDialog = ProgressDialog(this).apply {
            setMessage("注册中...")
            setCancelable(false)
            show()
        }

        val registerRequest = RegisterRequest(userName=userName,avatarUrl=avatarUrl, phone = phone,identity=identity, password = password,className=className)

        RetrofitClient.apiService.register(registerRequest).enqueue(object : Callback<BaseResp<UserInfo>> {
            override fun onResponse(call: Call<BaseResp<UserInfo>>, response: Response<BaseResp<UserInfo>>) {
                progressDialog.dismiss()

                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.code==0) {
                            Toast.makeText(this@RegisterActivity, "注册成功", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)//注册成功跳转
                            startActivity(intent)
                            finish()
                        } else {
                            Log.d("---注册响应：", it.message.toString())
                            Log.d("---registerResponse.code==", it.code.toString())
                            Toast.makeText(this@RegisterActivity, it.code.toString()+it.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    when (response.code()) {
                        409 -> Toast.makeText(this@RegisterActivity, "用户名已存在", Toast.LENGTH_SHORT).show()
                        500 -> Toast.makeText(this@RegisterActivity, "服务器内部错误，请稍后再试", Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(this@RegisterActivity, "注册失败，错误代码: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<BaseResp<UserInfo>>, t: Throwable) {
                progressDialog.dismiss()
                Log.d("---Register_onFailure",t.message.toString())
                Toast.makeText(this@RegisterActivity, "网络连接失败，请检查网络设置", Toast.LENGTH_SHORT).show()
            }
        })
    }


}