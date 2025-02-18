plugins {
	id("com.android.library")
	id("org.jetbrains.kotlin.android")
}

apply<ModuleStagingPlugin>()

android {
	compileSdk = Android.compileSdk
	namespace = "com.looker.core_common"

	defaultConfig {
		minSdk = Android.minSdk
		targetSdk = Android.compileSdk

		consumerProguardFiles("consumer-rules.pro")
	}

	buildTypes {
		release {
			isMinifyEnabled = true
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
	}
	buildFeatures {
		buildConfig = false
		aidl = false
		renderScript = false
		resValues = false
		shaders = false
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
	kotlinOptions {
		jvmTarget = "11"
	}
}

dependencies {
	implementation(Coroutines.core)
	implementation(Coroutines.android)
}