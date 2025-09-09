plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

val TAG = "corekit"

android {
    namespace = "com.example.corekit"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    buildFeatures {
        viewBinding = true
    }}

dependencies {
    api(libs.logger)
    api(libs.core.ktx)
    api(libs.tencent.mmkv)
    api(libs.appcompat)
    api(libs.material)
    api(libs.gson)
    api(libs.glide)
    api(libs.smartrefreshlayout)
    api(libs.bundles.retrofit)
    api(libs.androidx.lifecycle.runtime.ktx)
}