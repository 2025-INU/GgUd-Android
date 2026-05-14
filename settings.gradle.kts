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
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven("https://devrepo.kakao.com/nexus/content/groups/public/")
        maven("https://devrepo.kakao.com/nexus/repository/kakaomap-releases/")

        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "GgUd"
include(":app")
 