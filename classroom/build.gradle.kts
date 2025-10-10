plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.jxdx.classroom"
    compileSdk = 36
    ndkVersion = "22.1.7171670"

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        externalNativeBuild {
            cmake {
                cppFlags ("")
            }
        }

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/CMakeLists.txt")
            version ="3.18.1"
        }
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    implementation(project(":common"))
    implementation(project(":login"))
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
//    // IJKPlayer核心依赖
//    implementation(libs.ijkplayer.java)
//    implementation(libs.ijkplayer.armv7a)
//
//    // 其他架构（可选）
//    implementation(libs.ijkplayer.armv5)
//    implementation(libs.ijkplayer.arm64)
//    implementation(libs.ijkplayer.x86)

    implementation(libs.exoplayer.core)
    implementation(libs.exoplayer.extension.rtmp)
    implementation(libs.ffmpegKitVideo)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("androidx.activity:activity-ktx:1.8.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("com.airbnb.android:lottie:6.1.0")

    // PDF查看器依赖
    implementation(libs.android.pdf.viewer)

    // 图片加载库
    implementation("com.github.bumptech.glide:glide:4.16.0")
    
    // OkHttp依赖
    implementation("com.squareup.okhttp3:okhttp:5.1.0")
    
    // XPopup依赖
    implementation("com.github.li-xiaojun:XPopup:2.9.1")
    
    // 腾讯X5 WebView依赖
    implementation("com.tencent.tbs:tbssdk:44286")
    
    // CircleImageView依赖
    implementation("de.hdodenhof:circleimageview:3.1.0")
}