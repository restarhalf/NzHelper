plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

fun getGitCommitCount(): Int {
    return try {
        val process = ProcessBuilder("git", "rev-list", "--count", "HEAD")
            .redirectErrorStream(true)
            .start()
        process.inputStream.bufferedReader().readText().trim().toInt()
    } catch (_: Exception) {
        1 // fallback
    }
}

fun getGitShortHash(): String {
    return try {
        val process = ProcessBuilder("git", "rev-parse", "--short", "HEAD")
            .redirectErrorStream(true)
            .start()
        process.inputStream.bufferedReader().readText().trim()
    } catch (_: Exception) {
        "unknown"
    }
}

android {
    namespace = "me.restarhalf.deer"
    compileSdk = 36

    defaultConfig {
        applicationId = "me.restarhalf.deer"
        minSdk = 26
        targetSdk = 36

        val commitCount = getGitCommitCount()
        val gitHash = getGitShortHash()
        versionCode = getGitCommitCount()
        versionName = "1.0.0.beta.$commitCount.$gitHash"

        val envFile = rootProject.file(".env")
        fun readEnv(key: String): String? {
            if (!envFile.exists()) return null
            return envFile.readLines()
                .firstOrNull { it.startsWith("$key=") }
                ?.substringAfter('=')
                ?.trim()
                ?.takeIf { it.isNotBlank() }
        }

        fun escapeForBuildConfig(value: String): String =
            value.replace("\\", "\\\\").replace("\"", "\\\"")

        val supabaseUrl = readEnv("SUPABASE_URL")
            ?: ""
        val supabaseAnonKey = readEnv("SUPABASE_ANON_KEY")
            ?: ""
        val imgbbApiKey = readEnv("IMGBB_API_KEY")
            ?: ""

        buildConfigField("String", "SUPABASE_URL", "\"${escapeForBuildConfig(supabaseUrl)}\"")
        buildConfigField(
            "String",
            "SUPABASE_ANON_KEY",
            "\"${escapeForBuildConfig(supabaseAnonKey)}\""
        )
        buildConfigField("String", "IMGBB_API_KEY", "\"${escapeForBuildConfig(imgbbApiKey)}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = true
            isShrinkResources = true // 移除无用资源
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
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    // 构建时的包名
    android.applicationVariants.all {
        outputs.all {
            if (this is com.android.build.gradle.internal.api.ApkVariantOutputImpl) {
                val config = project.android.defaultConfig
                val versionName = config.versionName
                this.outputFileName = "NzHelper_v${versionName}.apk"
            }
        }
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation("top.yukonga.miuix.kmp", "miuix-android", "0.7.2")
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.window.size.class1)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}