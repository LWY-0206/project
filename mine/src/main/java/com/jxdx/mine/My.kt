package com.jxdx.mine.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.example.corekit.http.bean.BaseResp
import com.jxdx.mine.ProfileActivity
import com.jxdx.mine.R
import com.jxdx.mine.UserInfo
import com.jxdx.mine.course.CourseActivity
import com.jxdx.mine.databinding.FragmnetMymBinding
import com.jxdx.mine.grade.GradeActivity
import com.jxdx.mine.homework.HomeworkActivity
import com.jxdx.mine.http.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class My : Fragment() {

    private var _binding: FragmnetMymBinding? = null
    private val binding get() = _binding!!

    private var currentAvater: String? = null


    // 权限请求Launcher

    private fun showPermissionDeniedDialog(permissionName: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("权限被拒绝")
            .setMessage("$permissionName 权限被拒绝，无法使用该功能")
            .setPositiveButton("确定", null)
            .show()
    }
    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePhoto()
        } else {
            showPermissionDeniedDialog("相机")
        }
    }

    private val requestStoragePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            chooseFromGallery()
        } else {
            showPermissionDeniedDialog("存储")
        }
    }

    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            showImagePickerDialog()
        } else {
            Toast.makeText(requireContext(), "需要所有权限才能使用此功能", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadImage(str: String) {
        loadImageIntoTarget(str,binding.userAvatar)
        loadImageIntoTarget(str,binding.navAvatar)
        loadImageIntoTarget(str,binding.menuIcon)
    }
    private fun loadImageIntoTarget(source: String?, target: ImageView) {
        Glide.with(this)
            .load(source)
            .circleCrop()
            .into(target)
    }





    private fun checkPermissionsAndShowDialog() {
        val requiredPermissions = getRequiredPermissions()

        val hasAllPermissions = requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
        }

        if (hasAllPermissions) {
            showImagePickerDialog()
        } else {
            requestMultiplePermissions.launch(requiredPermissions.toTypedArray())
        }
    }

    private fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf(Manifest.permission.CAMERA)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        return permissions
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("从相册选择", "拍照上传", "取消")

        AlertDialog.Builder(requireContext())
            .setTitle("选择头像")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> checkStoragePermission()
                    1 -> checkCameraPermission()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            takePhoto()
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun checkStoragePermission() {
        val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(requireContext(), storagePermission) == PackageManager.PERMISSION_GRANTED) {
            chooseFromGallery()
        } else {
            requestStoragePermission.launch(storagePermission)
        }
    }

    private fun takePhoto() {
        try {
            val photoFile = createImageFile()
            val photoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )

            currentAvater = photoFile.absolutePath
            takePictureResult.launch(photoUri)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "创建文件失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun chooseFromGallery() {
        pickImageResult.launch("image/*")
    }






    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }




    // 相机和相册结果处理
    private val takePictureResult = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentAvater != null) {
            loadImage(currentAvater!!)
            Toast.makeText(requireContext(), "拍照成功，头像已更新", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImageResult = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            loadImage(it.toString())
            Toast.makeText(requireContext(), "头像已更新", Toast.LENGTH_SHORT).show()
        }
    }






    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }







    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmnetMymBinding.inflate(inflater, container, false)

        return binding.root
    }






    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //点击查看个人主页
        binding.navProfile.setOnClickListener {
            var intent= Intent(requireActivity(), ProfileActivity::class.java)
            startActivity(intent)
        }

        //点击我的里面的头像
        binding.userAvatar.setOnClickListener {
            checkPermissionsAndShowDialog()
        }
        //点击修改个人简介
        binding.editUserProfile.setOnClickListener {
            var intent= Intent(requireActivity(), EditProfileActivity::class.java)
            startActivityForResult(intent, 1)
        }
        //点击作业跳转到作业页面
        binding.navSettings.setOnClickListener {
            startActivity(Intent(requireActivity(), HomeworkActivity::class.java))
        }
        //点击班级跳转班级页面
        binding.navClass.setOnClickListener {
            startActivity(Intent(requireActivity(), GradeActivity::class.java))
        }

        //点击课程跳转课程界面
        binding.navCourses.setOnClickListener {
            startActivity(Intent(requireActivity(), CourseActivity::class.java))
        }

        //加载用户信息
        loadDate()
        //加载学习时间分布柱状图
        setupWebViewChart()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == AppCompatActivity.RESULT_OK) {
            val newBio = data?.getStringExtra("newBio")
            newBio?.let {
                binding.userProfile.text = it
            }
        }
    }
    // 加载保存的信息
    private fun loadDate() {
        //用户信息 我的 中   有  用户名/班级/个人简介/头像（uri/path）
        RetrofitClient.apiService.getUserInfo().enqueue(object : Callback<BaseResp<UserInfo>> {
            override fun onResponse(
                call: Call<BaseResp<UserInfo>?>,
                response: Response<BaseResp<UserInfo>?>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.code == 0) {
                            binding.navName.text=it.data?.userName//更新侧边栏名字
                            binding.userName.text=it.data?.userName//更新我的 名字
                            binding.userGrade.text=it.data?.className//更新我的 班级
                            binding.userProfile.text=it.data?.profile//更新我的 个人简介
                            currentAvater=it.data?.avatarUrl
                            if(currentAvater != null){
                                loadImageIntoTarget(currentAvater, binding.userAvatar)
                                loadImageIntoTarget(currentAvater, binding.navAvatar)
                                loadImageIntoTarget(currentAvater, binding.menuIcon)
                            }
                        } else {
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }

                    }
                }
                else{
                    Toast.makeText(
                        requireContext(),
                        "获取用户信息失败，错误: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(
                call: Call<BaseResp<UserInfo>?>,
                t: Throwable
            ) {
                Toast.makeText(requireContext(), "网络连接失败，请检查网络设置", Toast.LENGTH_SHORT).show()

            }
        })
    }



    //柱状图
    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebViewChart() {
        val webView = view?.findViewById<WebView>(R.id.barChartWebView)

        // 启用JavaScript
        webView?.settings?.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
        }

        // 加载本地HTML文件
        webView?.loadUrl("file:///android_asset/chart.html")

        // 可选：设置WebViewClient来处理页面加载
        webView?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // 页面加载完成后的操作
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                // 阻止在WebView中打开外部链接
                return false
            }
        }
    }

}