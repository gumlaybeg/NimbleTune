@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.application")
    kotlin("android")
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.gumlaybeg.nimbletune"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.gumlaybeg.nimbletune"
        minSdk = 26
        targetSdk = 36
        versionCode = 7
        versionName = "0.2.2.3"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isCrunchPngs = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            applicationIdSuffix = ".debug"
        }
        create("userdebug") {
            initWith(getByName("release"))
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    splits {
        abi {
            isEnable = true
            reset()
            isUniversalApk = false
        }
    }

    flavorDimensions.add("abi")

    productFlavors {
        create("universal") {
            isDefault = true
            dimension = "abi"
            ndk {
                abiFilters.addAll(listOf("x86", "x86_64", "armeabi-v7a", "arm64-v8a"))
            }
        }
        create("arm64") {
            dimension = "abi"
            ndk {
                abiFilters.add("arm64-v8a")
            }
        }
        create("x86_64") {
            dimension = "abi"
            ndk {
                abiFilters.add("x86_64")
            }
        }
    }

    applicationVariants.all {
        val variant = this
        variant.outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                output.outputFileName = "NimbleTune-${variant.versionName}-${variant.baseName}.apk"
            }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        jvmToolchain(21)
    }

    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
        jvmTarget = "21"
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.isReturnDefaultValues = true
    }

    lint {
        disable += "MissingTranslation"
        disable += "ImpliedQuantity"
        disable += "ByteOrderMark"
    }

    sourceSets {
        getByName("main") {
            java.setSrcDirs(listOf("src/main/java"))
        }
    }

    packaging {
        resources {
            excludes += "META-INF/services/javax.script.ScriptEngineFactory"
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.guava)
    implementation(libs.coroutines.guava)
    implementation(libs.concurrent.futures)

    implementation(libs.activity)
    implementation(libs.navigation)
    implementation(libs.hilt.navigation)
    implementation(libs.datastore)

    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.util)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.animation)
    implementation(libs.compose.reorderable)
    implementation(libs.compose.icons.extended)

    implementation(libs.viewmodel)
    implementation(libs.viewmodel.compose)

    implementation(libs.material3)
    implementation(libs.palette)
    implementation(projects.materialColorUtilities)

    implementation(libs.coil)
    implementation(libs.shimmer)

    implementation(libs.media3)
    implementation(libs.media3.session)
    implementation(libs.media3.okhttp)
    implementation(libs.media3.ui)

    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)

    implementation(libs.apache.lang3)

    implementation(libs.hilt)
    ksp(libs.hilt.compiler)

    implementation(projects.innertube)
    implementation(projects.kugou)
    implementation(projects.lrclib)

    coreLibraryDesugaring(libs.desugaring)

    implementation(libs.timber)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.serialization.json)

    implementation(files("../prebuilt/taglib_1.0.0.aar"))
}