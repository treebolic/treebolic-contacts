/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
}

val keystorePropertiesFile: File = rootProject.file("keystore.properties")
val keystoreProperties: Properties = Properties()
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

private val vCode by lazy { rootProject.extra["versionCode"] as Int }
private val vName by lazy { rootProject.extra["versionName"] as String }
private val vCompileSdk by lazy { rootProject.extra["compileSdk"] as Int }
private val vMinSdk by lazy { rootProject.extra["minSdk"] as Int }
private val vTargetSdk by lazy { rootProject.extra["targetSdk"] as Int }

android {

    namespace = "org.treebolic.contacts"

    compileSdk = vCompileSdk

    defaultConfig {
        applicationId = "org.treebolic.contacts"
        versionCode = vCode
        versionName = vName
        minSdk = vMinSdk
        targetSdk = vTargetSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }

    signingConfigs {
        create("treebolic") {
            keyAlias = keystoreProperties.getProperty("keyAlias_contacts")
            keyPassword = keystoreProperties.getProperty("keyPassword_contacts")
            storeFile = file(keystoreProperties.getProperty("storeFile_contacts"))
            storePassword = keystoreProperties.getProperty("storePassword_contacts")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.txt")
            signingConfig = signingConfigs.getByName("treebolic")
            versionNameSuffix = "signed"
        }
    }

    buildFeatures {
        buildConfig = true
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
    implementation(libs.treebolic.view)
    implementation(libs.treebolic.model)
    implementation(libs.treebolic.graph)

    implementation(project(":treebolicGlue"))
    implementation(project(":treebolicIface"))
    implementation(project(":treebolicContactsProvider"))
    implementation(project(":commonLib"))
    implementation(project(":searchLib"))
    implementation(project(":guideLib"))
    implementation(project(":preferenceLib"))
    implementation(project(":rateLib"))
    implementation(project(":othersLib"))
    implementation(project(":donateLib"))

    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.preference.ktx)
    implementation(libs.material)

    implementation(libs.core.ktx)
    coreLibraryDesugaring(libs.desugar)

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
}
