plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "app.iris"
    compileSdk = 35

    defaultConfig {
        applicationId = "app.iris"
        minSdk = 26
        targetSdk = 35
        versionCode = (System.getenv("GITHUB_RUN_NUMBER") ?: "1").toInt()
        versionName = "0.1." + (System.getenv("GITHUB_RUN_NUMBER") ?: "0")
        // Phones only (64-bit ARM): keeps the APK small; GeckoView ships one engine per ABI.
        ndk { abiFilters += listOf("arm64-v8a") }
    }

    signingConfigs {
        // A fixed key in the repository, so each new APK installs over the last one.
        // For the test builds only: a real release gets a key kept out of the repository.
        create("test") {
            storeFile = file("../keystore/test.jks")
            storePassword = "iris-test"
            keyAlias = "iris"
            keyPassword = "iris-test"
        }
    }

    buildTypes {
        debug { signingConfig = signingConfigs.getByName("test") }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("test")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    // GeckoView, release channel: the newest release when the APK is built (pinned once the test is done).
    implementation("org.mozilla.geckoview:geckoview:+")
}
