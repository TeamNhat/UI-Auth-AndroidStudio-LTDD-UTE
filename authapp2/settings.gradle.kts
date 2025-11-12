pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.google.gms.google-services") version "4.4.1" apply false
        // Cập nhật phiên bản AGP ở đây
        id("com.android.application") version "8.2.2" apply false
        // Cập nhật phiên bản Kotlin ở đây
        id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "authapp"
include(":app")
