package com.samyak.gitcore.util

/**
 * Utility for discovering the "real" application icon for a GitHub repository.
 * Generates a prioritized list of potential icon URLs based on common Android/Flutter/Multi-platform project structures.
 * 
 * Migrated to GitCore for isolated logic management.
 */
object IconResolver {

    private const val RAW_BASE_URL = "https://raw.githubusercontent.com"

    fun resolve(owner: String, name: String, defaultBranch: String?, rawLanguage: String?): List<String> {
        val branch = defaultBranch ?: "main"
        val baseUrl = "$RAW_BASE_URL/$owner/$name/$branch"
        val language = rawLanguage?.lowercase() ?: ""

        val nativePaths = listOf(
            "app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.webp",
            "app/src/main/res/mipmap-xxxhdpi/ic_launcher.png",
            "app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png",
            "app/src/main/ic_launcher-web.png",
            "app/src/main/ic_launcher-playstore.png",
            "app/src/main/res/drawable/app_icon.png",
            "app/src/main/res/mipmap-xxhdpi/ic_launcher.png",
            "app/src/main/res/mipmap-xxhdpi/ic_launcher_foreground.webp",
            "app/src/main/res/drawable/ic_launcher.png"
        )

        val kmmPaths = listOf(
            "androidApp/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.webp",
            "androidApp/src/main/res/mipmap-xxxhdpi/ic_launcher.png",
            "androidApp/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png",
            "androidApp/src/main/ic_launcher-web.png",
            "androidApp/src/main/ic_launcher-playstore.png",
            "androidApp/src/main/res/drawable/app_icon.png",
            "androidApp/src/main/res/mipmap-hdpi/ic_launcher_foreground.webp"
        )

        val crossPlatformPaths = listOf(
            "android/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png",
            "android/app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.webp",
            "android/app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png",
            "android/app/src/main/ic_launcher-web.png",
            "android/app/src/main/ic_launcher-playstore.png",
            "android/app/src/main/res/drawable/app_icon.png",
            "android/app/src/main/res/mipmap-xxhdpi/ic_launcher.png"
        )

        val dotnetPaths = listOf(
            "Platforms/Android/Resources/mipmap-xxxhdpi/ic_launcher.png",
            "Platforms/Android/Resources/mipmap-xxxhdpi/appicon.png",
            "Platforms/Android/Resources/mipmap-xxxhdpi/app_icon.png",
            "Platforms/Android/Resources/mipmap-hdpi/ic_launcher.png"
        )

        val metadataPaths = listOf(
            "fastlane/metadata/android/en-US/images/icon.png",
            "metadata/en-US/images/icon.png",
            "metadata/android/en-US/images/icon.png"
        )

        val dynamicPaths = listOf(
            "src/$name.Android/Icon.png",
            "src/$name.Android/icon.png",
            "src/$name.Android/Icon.svg",
            "src/$name.Android/icon.svg",
            "src/$name.Android/app_icon.png",
            "src/$name.Android/ic_launcher-web.png",
            "src/$name.Android/res/mipmap-xxxhdpi/ic_launcher.png",
            "src/$name.Android/Resources/mipmap-xxxhdpi/ic_launcher.png",
            "$name-android/src/main/res/mipmap-xxxhdpi/ic_launcher.png",
            "$name-android/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.webp",
            "$name-android/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png",
            "$name-android/src/main/ic_launcher-web.png",
            "$name-android/src/main/res/drawable/app_icon.png",
            "$name/src/main/res/mipmap-xxxhdpi/ic_launcher.png",
            "$name/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.webp",
            "$name/src/main/ic_launcher-web.png"
        )

        val docPaths = listOf(
            "doc/image/$name.svg",
            "doc/image/$name.png",
            "doc/image/logo.svg",
            "doc/image/logo.png",
            "doc/image/icon.svg",
            "doc/image/icon.png",
            "docs/image/$name.svg",
            "docs/image/$name.png",
            "docs/image/logo.svg",
            "docs/image/logo.png",
            "docs/image/icon.svg",
            "docs/image/icon.png",
            "docs/$name.svg",
            "docs/$name.png",
            "docs/logo.png",
            "docs/icon.png",
            "app/smartphone/icon.png",
            "app/smartphone/icon.svg",
            "app/smartphone/logo.png",
            "app/smartphone/logo.svg",
            "smartphone/icon.png",
            "smartphone/icon.svg",
            "smartphone/logo.png",
            "smartphone/logo.svg",
            "assets/$name.svg",
            "assets/$name.png",
            "assets/logo.png",
            "assets/icon.png",
            "images/rounded_default.png",
            "images/default.png",
            "image/rounded_default.png",
            "image/default.png",
            "images/app_logo.svg",
            "images/app_logo.png",
            "image/app_logo.svg",
            "image/app_logo.png",
            "images/$name.svg",
            "images/$name.png",
            "logo.png",
            "icon.png",
            "logo.svg",
            "icon.svg",
            "$name.svg",
            "$name.png"
        )

        // Prioritize based on language (framework detection)
        val prioritizedPaths = when (language) {
            "dart", "javascript", "typescript", "html", "vue", "css" -> crossPlatformPaths + nativePaths + kmmPaths + dotnetPaths + dynamicPaths + metadataPaths + docPaths
            "kotlin", "java" -> nativePaths + dynamicPaths + kmmPaths + crossPlatformPaths + dotnetPaths + metadataPaths + docPaths
            "c#" -> dotnetPaths + crossPlatformPaths + nativePaths + dynamicPaths + kmmPaths + metadataPaths + docPaths
            else -> nativePaths + dynamicPaths + kmmPaths + crossPlatformPaths + dotnetPaths + metadataPaths + docPaths
        }

        return prioritizedPaths.distinct().map { "$baseUrl/$it" }
    }
}
