plugins {
    id("com.android.application")
}

android {
    namespace = "app.iris"
    compileSdk = 37

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

    // The engine's native libraries compressed in the APK (unpacked once at install): about 200 MB
    // uncompressed, a fraction of that to download.
    packaging { jniLibs { useLegacyPackaging = true } }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
}

dependencies {
    // GeckoView, release channel (Firefox 161). Moved up by hand with each Firefox release.
    implementation("org.mozilla.geckoview:geckoview:161.0.20261118180411")
}
