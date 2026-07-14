import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val localProperties = Properties().apply {
    rootProject.file("local.properties").inputStream().use(::load)
}

android {
    namespace = "com.leaf.qrcodegenerator"
    compileSdk = 36

    signingConfigs {
        create("release") {
            storeFile = file("/Users/leafwai/Documents/leafkey.jks")
            storePassword = localProperties.getProperty("RELEASE_STORE_PASSWORD")
                ?.takeIf(String::isNotBlank)
            keyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS")
                ?.takeIf(String::isNotBlank)
            keyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD")
                ?.takeIf(String::isNotBlank)
        }
    }

    defaultConfig {
        applicationId = "com.leaf.qrcodegenerator"
        minSdk = 21
        targetSdk = 36
        versionCode = 5
        versionName = "1.3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.permissionx)
    implementation(libs.mlkit.barcode.scanning)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.zxing.lite)
    implementation(libs.viewbinding.ktx)
    implementation(libs.viewbinding.brvah)
    implementation(libs.brvah)
    implementation(libs.mmkv)
    implementation(libs.gson)
    implementation(libs.dialogx.core)
    implementation(libs.dialogx.miui.style)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}
