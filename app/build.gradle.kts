import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "idv.fan.iisigroup.android.test"
    compileSdk = 36

    defaultConfig {
        applicationId = "idv.fan.iisigroup.android.test"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "idv.fan.iisigroup.android.test.HiltTestRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "FLIGHT_BASE_URL", "\"https://www.kia.gov.tw/\"")
            buildConfigField("String", "EXCHANGE_RATE_BASE_URL", "\"https://api.freecurrencyapi.com/v1/\"")
            buildConfigField("String", "EXCHANGE_RATE_API_KEY", "\"fca_live_iG1uxn9wgTllfRilqJbwrkQzYj9gYfCUbCvOGjda\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "FLIGHT_BASE_URL", "\"https://www.kia.gov.tw/\"")
            buildConfigField("String", "EXCHANGE_RATE_BASE_URL", "\"https://api.freecurrencyapi.com/v1/\"")
            buildConfigField("String", "EXCHANGE_RATE_API_KEY", "\"fca_live_iG1uxn9wgTllfRilqJbwrkQzYj9gYfCUbCvOGjda\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.moshi)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.datastore.preferences)
    implementation(libs.coil.compose)
    implementation(libs.timber)
    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)

    add("ksp", libs.hilt.android.compiler)
    add("ksp", libs.moshi.kotlin.codegen)
    add("ksp", libs.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    add("kspAndroidTest", libs.hilt.android.compiler)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
