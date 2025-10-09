plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}
android {
    namespace = "com.jxdx.resource"
    compileSdk = 36
    packaging {
        jniLibs {
            pickFirsts += setOf("**/libc++_shared.so")
        }
    }
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
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation("com.github.barteksc:android-pdf-viewer:3.2.0-beta.1")
    implementation ("com.google.auto.service:auto-service-annotations:1.0.1")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.drakeet.multitype:multitype:4.3.0")
    implementation(project(":common"))
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlin.stdlib)
    implementation(libs.swiperefreshlayout)
    implementation(libs.circleimageview)
    implementation(libs.fragment.ktx)
    implementation(libs.glide)
    implementation(libs.imagepicker)
    implementation(libs.xpopup)
    annotationProcessor(libs.glide.compiler)
    implementation("com.github.li-xiaojun:XPopup:2.9.1")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.tencent.tbs:tbssdk:44286")
    implementation(libs.banner)
    implementation(libs.bannerviewpager)
}