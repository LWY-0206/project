package org.jxxy.debug.manager

import android.content.Context
import android.provider.Settings
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class DeviceManager private constructor(){
    companion object{
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            DeviceManager()
        }
    }

    /**
     * 获取 ANDROID_ID 并返回其 SHA-256 哈希值
     */
    fun getDeviceIdHash(context: Context): String? {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: return null // 如果 ANDROID_ID 为 null，返回 null

        return toSha256(androidId)
    }

    /**
     * 使用 SHA-256 对字符串进行哈希
     */
    private fun toSha256(input: String): String? {
        return try {
            val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(input.toByteArray())

            // 将字节数组转换为十六进制字符串
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            null
        }
    }
}