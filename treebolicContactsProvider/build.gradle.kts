/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

private val vCompileSdk by lazy { rootProject.extra["compileSdk"] as Int }
private val vMinSdk by lazy { rootProject.extra["minSdk"] as Int }

android {

    namespace = "treebolic.provider.contacts"

    compileSdk = vCompileSdk

    defaultConfig {
        minSdk = vMinSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
    }
}

dependencies {
    implementation(libs.treebolic.model)
    implementation(libs.treebolic.mutable)
    implementation(libs.treebolic.loadbalancer)

    implementation(project(":treebolicGlue"))

    implementation(libs.core.ktx)
    implementation(libs.preference.ktx)
    implementation(libs.annotation)

    implementation(libs.core.ktx)
    coreLibraryDesugaring(libs.desugar)
}
