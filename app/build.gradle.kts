import java.text.SimpleDateFormat
import java.util.*

val versionNamePrefix = "2.1.1"

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.devtools.ksp") version "1.7.10-1.0.6"
    id("dev.rikka.tools.materialthemebuilder")
}

val androidCompileSdkVersion: Int by rootProject.extra
val androidMinSdkVersion: Int by rootProject.extra
val androidTargetSdkVersion: Int by rootProject.extra
val androidSourceCompatibility: JavaVersion by rootProject.extra
val androidTargetCompatibility: JavaVersion by rootProject.extra
val kotlinJvmTarget: String by rootProject.extra

android {
    compileSdk = androidCompileSdkVersion

    defaultConfig {
        applicationId = "mufanc.tools.applock"
        minSdk = androidMinSdkVersion
        targetSdk = androidTargetSdkVersion
        versionCode = getVersionCode()
        versionName = getVersionName()
    }

    signingConfigs {
        create("release") {
            val props = Properties().apply {
                load(rootProject.file("local.properties").inputStream())
            }
            storeFile = file(props["KEYSTORE_FILE"] as String)
            storePassword = props["KEYSTORE_PASSWORD"] as String
            keyAlias = props["KEYSTORE_ALIAS"] as String
            keyPassword = props["KEYSTORE_ALIAS_PASSWORD"] as String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs["release"]
        }
    }

    compileOptions {
        sourceCompatibility = androidSourceCompatibility
        targetCompatibility = androidTargetCompatibility
    }

    kotlinOptions {
        jvmTarget = kotlinJvmTarget
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }

    packagingOptions {
        jniLibs {
            excludes.addAll(arrayOf("/kotlin/**"))
        }
        resources {
            excludes.addAll(arrayOf("/META-INF/*.version", "/META-INF/*.kotlin_module", "/kotlin/**"))
        }
    }
}

afterEvaluate {
    android.applicationVariants.forEach { variant ->
        variant.assembleProvider.get().doLast {
            variant.outputs.forEach { output ->
                val filename = "AppLock-v${variant.versionName}-${variant.name}.apk"
                val dir = output.outputFile.parent
                val outputFile = File(dir, filename)
                if (outputFile.relativeTo(projectDir).startsWith("build/intermediates").not()) {
                    output.outputFile.renameTo(outputFile)
                }
            }
        }
    }
}

materialThemeBuilder {
    themes {
        for ((name, color) in listOf(
            "Anemo" to "#75C3A9",
            "Cryo" to "#A0D7E4",
            "Dendro" to "#A6C938",
            "Electro" to "#B08FC2",
            "Geo" to "#FAB72E",
            "Hydro" to "#4BC3F1",
            "Pyro" to "#EF7A35"
        )) {
            create("GenshinElement.$name") {
                primaryColor = color
                lightThemeFormat = "Theme.AppLock.Light.%s"
                darkThemeFormat = "Theme.AppLock.Dark.%s"
            }
        }
    }
}

dependencies {
    compileOnly(project(":api-stub"))

    ksp(project(":easyhook:ksp-xposed"))
    implementation(project(":easyhook:api"))
    compileOnly("de.robv.android.xposed:api:82")

    implementation("dev.rikka.shizuku:api:12.1.0")
    implementation("dev.rikka.shizuku:provider:12.1.0")
    implementation("org.lsposed.hiddenapibypass:hiddenapibypass:4.3")

    kapt("com.github.bumptech.glide:compiler:4.13.2")
    implementation("com.github.bumptech.glide:glide:4.13.2")

    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.appcompat:appcompat:1.5.0")
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.1")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.1")
    implementation("androidx.preference:preference:1.2.0")

    implementation("com.google.code.gson:gson:2.9.1")
}

fun getVersionCode(): Int {
    val versionCode = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()).toInt()
    println("version code: $versionCode")
    return versionCode
}

fun getVersionName(): String {
    fun String.execute(): String {
        return Runtime.getRuntime()
            .exec(this.split(" ").toTypedArray())
            .apply { waitFor() }
            .inputStream.bufferedReader().readText().trim()
    }

    val hash = "git rev-parse --short HEAD".execute()
    val count = "git rev-list --count HEAD".execute()
    val versionName = "$versionNamePrefix.r$count.$hash"
    println("version name: $versionName")
    return versionName
}
