pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
    plugins {
        id("com.android.application") version "8.2.0" apply false
        id("org.jetbrains.kotlin.android") version "1.8.22" apply false
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "HealthTracker"
include(":app")
