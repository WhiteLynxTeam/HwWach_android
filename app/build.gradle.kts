import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.whitelynxteam.hwwach"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.whitelynxteam.hwwach"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_AUTH_URL", "\"https://ts-back-hwwach.onrender.com/\"")
            buildConfigField("String", "BASE_MAIN_URL", "\"https://hwwach.onrender.com/\"")
//            buildConfigField("String", "BASE_AUTH_URL", "\"http://149.154.65.57:3033/\"")
//            buildConfigField("String", "BASE_MAIN_URL", "\"http://149.154.65.57:8080/\"")
//            buildConfigField("String", "BASE_AUTH_URL", "\"http://10.0.2.2:3033/\"")

        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
//            buildConfigField("String", "BASE_AUTH_URL", "\"http://149.154.65.57:3033/\"")
            buildConfigField("String", "BASE_AUTH_URL", "\"https://ts-back-hwwach.onrender.com/\"")
            buildConfigField("String", "BASE_MAIN_URL", "\"https://hwwach.onrender.com/\"")
//            buildConfigField("String", "BASE_MAIN_URL", "\"http://149.154.65.57:8080/\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation)
    implementation(libs.androidx.material.icons.extended)

    /** Coroutines */
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    /** Coil */
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.telephoto.zoomable.coil)

    /** Retrofit */
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    /** Retrofit - okHttp */
    implementation(libs.okhttp.logging.interceptor)

    /** DataStore */
    implementation(libs.androidx.datastore.preferences)

    /** Hilt */
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    /** Room */
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    testImplementation(libs.junit)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}