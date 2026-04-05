package com.samyak.gitcore.util

/**
 * Utility for discovering the "real" application name for a GitHub repository by finding string resources.
 * Generates a prioritized list of potential file URLs (like strings.xml, pubspec.yaml)
 * where the actual app name might be stored across Android, Flutter, React Native, and MAUI projects.
 */
object AppNameResolver {

    private const val RAW_BASE_URL = "https://raw.githubusercontent.com"

    fun resolve(owner: String, name: String, defaultBranch: String?, rawLanguage: String?): List<String> {
        val branch = defaultBranch ?: "main"
        val baseUrl = "$RAW_BASE_URL/$owner/$name/$branch"
        val language = rawLanguage?.lowercase() ?: ""

        val nativePaths = listOf(
            "app/src/main/res/values/strings.xml",
            "app/src/main/res/values-en/strings.xml",
            "app/src/main/res/values/strings_non_localized.xml",
            "app/src/main/res/values/app_name.xml",
            "src/main/res/values/strings.xml",
            "src/main/res/values/strings_non_localized.xml",
            "res/values/strings.xml"
        )

        val kmmPaths = listOf(
            "androidApp/src/main/res/values/strings.xml",
            "androidApp/src/main/res/values/app_name.xml",
            "shared/src/commonMain/resources/strings.xml"
        )

        val crossPlatformPaths = listOf(
            // React Native / Flutter specific string mappings
            "android/app/src/main/res/values/strings.xml",
            "android/app/src/main/res/values/app_name.xml",
            
            // Core config files for framework native logic
            "pubspec.yaml",
            "package.json",
            "app.json",
            "ionic.config.json",
            "capacitor.config.json"
        )

        val dotnetPaths = listOf(
            "Platforms/Android/Resources/values/strings.xml",
            "Platforms/Android/Resources/values/app_name.xml",
            "$name/$name.csproj",
            "Package.appxmanifest"
        )

        val dynamicPaths = listOf(
            "src/$name.Android/Resources/values/strings.xml",
            "$name-android/src/main/res/values/strings.xml",
            "$name/src/main/res/values/strings.xml",
            "$name/app/src/main/res/values/strings.xml",
            "$name-android/src/main/res/values/app_name.xml",
            "$name/src/main/res/values/app_name.xml",
            "$name/app/src/main/res/values/app_name.xml"
        )
        
        val docPaths = listOf(
            "README.md",
            "fastlane/metadata/android/en-US/title.txt",
            "metadata/en-US/title.txt",
            "metadata/android/en-US/title.txt"
        )

        // Prioritize based on language (framework detection)
        val prioritizedPaths = when (language) {
            "dart", "javascript", "typescript", "html", "vue", "css" -> crossPlatformPaths + nativePaths + kmmPaths + dotnetPaths + dynamicPaths + docPaths
            "kotlin", "java" -> nativePaths + dynamicPaths + kmmPaths + crossPlatformPaths + dotnetPaths + docPaths
            "c#" -> dotnetPaths + crossPlatformPaths + nativePaths + dynamicPaths + kmmPaths + docPaths
            else -> nativePaths + dynamicPaths + kmmPaths + crossPlatformPaths + dotnetPaths + docPaths
        }

        return prioritizedPaths.distinct().map { "$baseUrl/$it" }
    }
}
