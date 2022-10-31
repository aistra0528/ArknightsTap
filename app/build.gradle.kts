plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.icebem.akt"
    compileSdk = 33
    buildToolsVersion = "33.0.0"

    defaultConfig {
        applicationId = "com.icebem.akt"
        minSdk = 21
        targetSdk = 30
        versionCode = 64
        versionName = "2.10.5"
        resourceConfigurations += arrayOf("zh-rCN", "en", "ja", "in")
    }
    val signing = if (file("../signing.properties").exists()) {
        signingConfigs.create("release") {
            val props = `java.util`.Properties().apply { load(file("../signing.properties").reader()) }
            storeFile = file(props.getProperty("storeFile"))
            storePassword = props.getProperty("storePassword")
            keyAlias = props.getProperty("keyAlias")
            keyPassword = props.getProperty("keyPassword")
        }
    } else signingConfigs.getByName("debug")
    buildTypes {
        debug {
            applicationIdSuffix = ".luna"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signing
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    applicationVariants.all {
        outputs.all {
            (this as? com.android.build.gradle.internal.api.ApkVariantOutputImpl)
                    ?.outputFileName = "ArkTap-v$versionName.apk"
        }
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

dependencies {
    val kotlinVersion: String by rootProject
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.0")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("com.google.android.material:material:1.6.1")
    /** @Deprecated */
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")
}