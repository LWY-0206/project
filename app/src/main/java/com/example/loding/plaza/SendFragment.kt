package com.example.loding.plaza

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.corekit.common.BaseFragment
import com.example.loding.adapter.DynamicBody
import com.example.loding.adapter.PostDynamicViewModel
import com.example.loding.common.CommonViewModel
import com.example.loding.databinding.FragmentSendBinding
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

class SendFragment : BaseFragment<FragmentSendBinding>() {
    // 1111111111111111111
    // 权限请求码和图片选择请求码
    private val permissionRequestCode = 101
    private val pickImagesLauncher =
        registerForActivityResult(
            ActivityResultContracts.GetMultipleContents(),
        ) { uris ->
            // 处理返回的图片 URI 列表
            if (uris.isNotEmpty()) {
                // 上传选中的图片
                uploadImages(uris)
            } else {
                Toast.makeText(context, "未选择图片", Toast.LENGTH_SHORT).show()
            }
        }

    // 222222222222222222
    // 存储上传后的图片URL列表
    private val uploadedImageUrls = mutableListOf<String>()
    
    // 添加一个标志，用于标记是否是新的发布请求
    private var isNewPublishRequest = false
    // 添加一个标志，用于标记是否是新的上传请求
    private var isNewUploadRequest = false

    // 发布动态回调接口
    interface OnPublishListener {
        fun onPublishSuccess()
    }

    private var publishListener: OnPublishListener? = null

    fun setOnPublishListener(listener: OnPublishListener) {
        this.publishListener = listener
    }

    // 初始化PostDynamicViewModel
    private val viewModel: PostDynamicViewModel by lazy {
        ViewModelProvider(requireActivity())[PostDynamicViewModel::class.java]
    }
    private val viewModel2: CommonViewModel by lazy {
        ViewModelProvider(requireActivity())[CommonViewModel::class.java]
    }

    companion object {
        fun newInstance(): SendFragment = SendFragment()
    }

    override fun bindLayout(): FragmentSendBinding = FragmentSendBinding.inflate(layoutInflater)

    override fun initView() {
        // 每次初始化视图时清空已上传的图片URL列表
        uploadedImageUrls.clear()
        setListeners()
    }

    override fun subscribeUi() {
        // 观察postDynamicLiveData，处理发布动态的结果
        viewModel.postDynamicLiveData.observe(this) { resource ->
            // 只有在isNewPublishRequest为true时才处理结果
            if (isNewPublishRequest) {
                resource.onSuccess { data ->
                    // 重置标志位
                    isNewPublishRequest = false
                    
                    // 即使data为null，也能正常处理成功情况
                    Toast.makeText(context, "发布成功", Toast.LENGTH_SHORT).show()
                    // 回调发布成功事件
                    publishListener?.onPublishSuccess()
                    // 清空已上传的图片URL列表，以便下次发布
                    uploadedImageUrls.clear()
                    // 返回上一页
                    activity?.supportFragmentManager?.popBackStack()
                }

                resource.onError { error, _ ->
                    // 重置标志位
                    isNewPublishRequest = false
                    Toast.makeText(context, "发布失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // 3333333333333333333
        // 观察uploadLiveData，处理图片上传结果
        viewModel2.uploadLiveData.observe(this) {
            // 只有在isNewUploadRequest为true时才处理结果
            if (isNewUploadRequest) {
                it.onSuccess { imageUrls ->
                    try {
                        if (imageUrls != null && imageUrls.isNotEmpty()) {
                            // 服务器返回的data字段是List<String>，直接添加到uploadedImageUrls
                            uploadedImageUrls.addAll(imageUrls)
                            Toast.makeText(context, "图片上传成功", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "图片上传失败，未获取到URL", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("SendFragment", "图片上传成功但处理URL失败: ${e.message}")
                        Toast.makeText(context, "图片上传成功但处理URL失败", Toast.LENGTH_SHORT).show()
                    } finally {
                        // 无论成功失败，都重置标志位
                        isNewUploadRequest = false
                    }
                }

                it.onError { error, _ ->
                    Log.e("SendFragment", "图片上传失败: ${error?.message}")
                    Toast.makeText(context, "图片上传失败", Toast.LENGTH_SHORT).show()
                    // 错误情况也需要重置标志位
                    isNewUploadRequest = false
                }
            }
        }
    }

    private fun setListeners() {
        // 取消按钮点击事件
        find.tvCancel.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }

        // 发布按钮点击事件
        find.tvPublish.setOnClickListener {
            publishDynamic()
        }

        // 添加图片按钮点击事件
        find.ivAddImage.setOnClickListener {
            checkPermissionAndOpenAlbum()
        }
    }

    // 4444444444444444444444444444444
    private fun checkPermissionAndOpenAlbum() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 请求权限
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                permissionRequestCode,
            )
        } else {
            // 权限已授予，打开相册
            openAlbum()
        }
    }

    private fun openAlbum() {
        pickImagesLauncher.launch("image/*")
    }

    private fun publishDynamic() {
        // 获取用户输入的内容
        val dynamicBody = DynamicBody(
            title = "写死的标题",
            content = find.etContent.text.toString().trim(),
            // 使用已上传的图片URL列表
            contentImageUrls = if (uploadedImageUrls.isEmpty()) null else uploadedImageUrls,
        )

        // 检查内容是否为空
        if (TextUtils.isEmpty(dynamicBody.content)) {
            Toast.makeText(context, "请输入内容", Toast.LENGTH_SHORT).show()
            return
        }

        // 设置标志位，表示这是一个新的发布请求
        isNewPublishRequest = true
        
        // 使用ViewModel发送网络请求发布动态
        viewModel.postDynamic(dynamicBody)
    }

    // 5555555555555555555555
    // 上传图片列表
    private fun uploadImages(imageUris: List<Uri>) {
        // 确保每次上传前清空之前的URL列表
        uploadedImageUrls.clear()
        // 设置标志位，表示这是一个新的上传请求
        isNewUploadRequest = true

        // 遍历所有选中的图片URI
        for (uri in imageUris) {
            // 将URI转换为文件路径
            val filePath = uriToFilePath(uri)
            if (filePath != null) {
                // 上传文件
                viewModel2.uploadFile(filePath)
            } else {
                Toast.makeText(context, "无法获取图片文件", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 6666666666666666666666
    // 将URI转换为文件路径
    private fun uriToFilePath(uri: Uri): String? =
        try {
            val inputStream: InputStream? = context?.contentResolver?.openInputStream(uri)
            val cacheDir = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            if (inputStream != null && cacheDir != null) {
                val fileName = "${UUID.randomUUID()}.jpg"
                val file = File(cacheDir, fileName)
                val outputStream = FileOutputStream(file)
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()
                file.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("SendFragment", "URI转文件失败: ${e.message}")
            null
        }
        //777777777这个是为了修复bug
    override fun onDestroyView() {
        super.onDestroyView()
        // 确保Fragment销毁时清空已上传的图片URL列表
        uploadedImageUrls.clear()
        // 重置发布请求标志位
        isNewPublishRequest = false
        // 重置上传请求标志位
        isNewUploadRequest = false
        // 移除LiveData观察者，防止内存泄漏和状态残留
        viewModel.postDynamicLiveData.removeObservers(this)
        viewModel2.uploadLiveData.removeObservers(this)
    }
}
