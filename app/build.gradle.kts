plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdk = Android.compileSdk

    defaultConfig {
        applicationId = Android.appId
        minSdk = Android.minSdk
        targetSdk = Android.targetSdk
        versionCode = Android.versionCode
        versionName = Android.versionName

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
        all {
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = false
        aidl = false
        renderScript = false
        resValues = false
        shaders = false
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Compose.composeVersion
    }
    packagingOptions {
        resources {
            excludes += Excludes.exclude
        }
    }
}

dependencies {

    implementation(project(Modules.constants))
    implementation(project(Modules.onBoarding))
    implementation(project(Modules.uiSongs))
    implementation(project(Modules.uiAlbums))
    implementation(project(Modules.dataSongs))
    implementation(project(Modules.dataAlbums))

    implementation(Core.core)

    implementation(Accompanist.insets)
    implementation(Compose.activity)
    implementation(Compose.icons)
    implementation(Compose.material)
    implementation(Compose.navigation)
    implementation(Compose.tooling)
    implementation(Compose.ui)

    debugImplementation(Compose.preview)
}