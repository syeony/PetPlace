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
        maven("https://jitpack.io") //crop 가져오려면 필요
//        maven("https://devrepo.kakao.com/nexus/repository/kakaomap-releases/")
        maven ("https://devrepo.kakao.com/nexus/content/groups/public/")
    }
}

rootProject.name = "PetPlace"
include(":app")
 