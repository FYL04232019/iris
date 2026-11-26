buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // GeckoView 161 brings Kotlin's standard library 2.4: compile with Kotlin 2.4 too
        // (AGP 9.1 alone would use 2.2, which cannot read it).
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.21")
    }
}

plugins {
    // AGP 9: Kotlin is built in (no separate Kotlin plugin). GeckoView 161 needs AGP 9.1+ and compileSdk 37.
    id("com.android.application") version "9.1.1" apply false
}
