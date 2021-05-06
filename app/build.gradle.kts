plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdk = 30
    buildToolsVersion = "30.0.3"

    defaultConfig {
        applicationId = "com.icebem.akt"
        minSdk = 21
        targetSdk = 30
        versionCode = 52
        versionName = "2.7.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        resConfigs("zh-rCN", "en", "ja", "in")
    }
    buildFeatures {
        viewBinding = false
        dataBinding = false
    }
    buildTypes {
        getByName("debug") {
            versionNameSuffix = "-Lune-${`java.text`.SimpleDateFormat("yyyyMMdd").format(System.currentTimeMillis())}"
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    applicationVariants.all {
        outputs.all {
            (this as com.android.build.gradle.internal.api.ApkVariantOutputImpl).outputFileName = "ArkTap-v$versionName.apk"
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.5.0")

    implementation("androidx.annotation:annotation:1.2.0")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("androidx.core:core-ktx:1.3.1")
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.0.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.1")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.1")
    implementation("androidx.preference:preference-ktx:1.1.1")
    implementation("com.google.android:flexbox:2.0.1")
    implementation("com.google.android.material:material:1.2.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}