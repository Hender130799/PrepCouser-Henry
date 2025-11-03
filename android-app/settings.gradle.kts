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
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "CalisteniaApp"

include(
    ":app",
    ":core",
    ":core-model",
    ":core-database",
    ":core-network",
    ":domain",
    ":data",
    ":feature-onboarding",
    ":feature-workouts",
    ":feature-nutrition",
    ":feature-progress",
    ":feature-settings"
)
