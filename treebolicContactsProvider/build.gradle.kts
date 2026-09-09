/*
 * Copyright (c) 2026. Bernard Bou
 */

plugins {
    alias(libs.plugins.androidLibrary)
}

android {

    namespace = "treebolic.provider.contacts"

    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.treebolic.model)
    implementation(libs.treebolic.mutable)
    implementation(libs.treebolic.loadbalancer)

    implementation(project(":treebolicGlue"))

    implementation(libs.preference.ktx)
    implementation(libs.annotation)

    implementation(libs.core.ktx)
    implementation(platform(libs.kotlin.bom))
    implementation(kotlin("stdlib"))
    coreLibraryDesugaring(libs.desugar)
}
