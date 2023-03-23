plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.icebem.akt"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.icebem.akt"
        minSdk = 21
        targetSdk = 33
        versionCode = 65
        versionName = "2.11.0"
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
    applicationVariants.configureEach {
        outputs.configureEach {
            (this as? com.android.build.gradle.internal.api.ApkVariantOutputImpl)?.outputFileName = "ArkTap-v$versionName.apk"
        }
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    buildFeatures {
        viewBinding = true
    }
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("com.google.android.material:material:1.8.0")
}