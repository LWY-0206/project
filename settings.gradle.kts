pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        maven("https://maven.aliyun.com/repository/central/")
        maven("https://maven.aliyun.com/repository/google/")
        maven("https://maven.aliyun.com/repository/public/")
        maven("https://maven.aliyun.com/repository/gradle-plugin/")

        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven("https://maven.aliyun.com/repository/central/")
        maven("https://maven.aliyun.com/repository/google/")
        maven("https://maven.aliyun.com/repository/public/")
        maven("https://jitpack.io")
        google()
        mavenCentral()
    }
}

rootProject.name = "Loding"
include(":app")
include(":corekit")
