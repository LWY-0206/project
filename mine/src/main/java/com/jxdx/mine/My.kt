package com.jxdx.mine.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.jxdx.mine.ProfileActivity
import com.jxdx.mine.R
import com.jxdx.mine.databinding.FragmnetMymBinding


class My : Fragment() {

    private var _binding: FragmnetMymBinding? = null
    private val binding get() = _binding!!

    private var currentPhotoPath: String? = null


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









    // 保存头像URI到SharedPreferences
    private fun saveAvatarUri(uriString: String) {
        val preferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val uesName=preferences.getString("username","用户名")
        preferences.edit {
            putString("${uesName}_uri", uriString)
            // 同时清除路径，避免冲突
            remove("${uesName}_path")
        }
    }
    // 保存头像路径到SharedPreferences
    private fun saveAvatarPath(path: String) {
        val preferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val uesName=preferences.getString("username","用户名")
        preferences.edit {
            putString("${uesName}_path", path)
            // 同时清除URI，避免冲突
            remove("${uesName}_uri")
        }
    }
    private fun loadImageFromUri(uri: Uri) {
        // 保存URI
        saveAvatarUri(uri.toString())

        loadImageIntoTarget(uri,binding.userAvatar)
        loadImageIntoTarget(uri,binding.navAvatar)
        loadImageIntoTarget(uri,binding.menuIcon)
    }

    private fun loadImageFromPath(path: String) {
        // 保存路径
        saveAvatarPath(path)

        loadImageIntoTarget(path,binding.userAvatar)
        loadImageIntoTarget(path,binding.navAvatar)
        loadImageIntoTarget(path,binding.menuIcon)
    }
    private fun loadImageIntoTarget(source: Any, target: ImageView) {
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

            currentPhotoPath = photoFile.absolutePath
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
        if (success && currentPhotoPath != null) {
            loadImageFromPath(currentPhotoPath!!)
            Toast.makeText(requireContext(), "拍照成功，头像已更新", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImageResult = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            loadImageFromUri(it)
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
        binding.editUserBio.setOnClickListener {
            var intent= Intent(requireActivity(), EditProfileActivity::class.java)
            startActivityForResult(intent, 1)
        }

        //加载用户信息
        loadSavedDate()
        //加载学习时间分布柱状图
        setupWebViewChart()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == AppCompatActivity.RESULT_OK) {
            val newBio = data?.getStringExtra("newBio")
            newBio?.let {
                binding.userBio.text = it
                // 可选：立即保存到 SharedPreferences
                saveBioToPrefs(it)
            }
        }
    }

    private fun saveBioToPrefs(bio: String) {
        val preferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userName = preferences.getString("username", "未命名")
        preferences.edit {
            putString("${userName}_bio", bio)
        }
    }







    // 加载保存的信息
    private fun loadSavedDate() {
        //用户信息 我的 中   有  用户名/班级/个人简介/头像（uri/path）
        val preferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userName=preferences.getString("user_name","小明")
        val grade=preferences.getString("user_grade","高一1班")
        val bio=preferences.getString("user_bio","点击添加兴趣爱好")


        val uriString = preferences.getString("${userName}_uri", null)
        val path = preferences.getString("${userName}_path", null)


        binding.navName.text=userName//更新侧边栏名字

        binding.userName.text=userName//更新我的 名字
        binding.userGrade.text=grade//更新我的 班级
        binding.userBio.text=bio//更新我的 个人简介
        when {
            uriString != null -> {
                val uri = Uri.parse(uriString)
                loadImageIntoTarget(uri, binding.userAvatar)
                loadImageIntoTarget(uri, binding.navAvatar)
                loadImageIntoTarget(uri, binding.menuIcon)
            }
            path != null -> {
                loadImageIntoTarget(path, binding.userAvatar)
                loadImageIntoTarget(path, binding.navAvatar)
                loadImageIntoTarget(path, binding.menuIcon)
            }
        }
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