pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // GeckoView (the Firefox engine) is published here, not on Maven Central.
        maven("https://maven.mozilla.org/maven2/")
    }
}
rootProject.name = "Iris"
include(":app")
