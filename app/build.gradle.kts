plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.rhythmcoderzzf.androidstudysystem"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rhythmcoderzzf.androidstudysystem"
        minSdk = 29
        //noinspection ExpiredTargetSdkVersion
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                cppFlags += ""
            }
        }
    }
    buildFeatures {
        viewBinding = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    //将libs目录设置为存放 JNI 本地库文件（.so文件） 的根目录。libs目录需位于模块的 src/main/下（如 app/src/main/libs）
    //子目录需按 CPU 架构命名（例如 armeabi-v7a、arm64-v8a）
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.extensions)
    implementation(libs.viewbinding)
    implementation(project(":baselib"))
    implementation(project(":ezandroid"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}