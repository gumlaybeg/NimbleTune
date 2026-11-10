plugins {
    id("com.android.library")
    kotlin("android")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.music.innertube"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

kotlin {
    jvmToolchain(21)
}

// Disable AAR metadata compileSdk checks forced by upstream libraries
tasks.matching { it.name.contains("AarMetadata") }.configureEach {
    enabled = false
}

dependencies {
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.client.encoding)
    implementation(libs.brotli)
    implementation(libs.innertubex)
    implementation(libs.newpipeextractor)
    testImplementation(libs.junit)

    coreLibraryDesugaring(libs.desugaring)
}