package com.samyak.repostore.util

import com.samyak.gitcore.util.AppNameResolver
import com.samyak.repostore.data.model.GitHubRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object AppNameFetcher {

    // Simple OkHttp client for fast text fetching
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    // Regex to match <string name="app_name">AppNameHere</string>
    private val xmlRegex = Pattern.compile("<string\\s+name=[\"']app_name[\"'][^>]*>([^<]+)</string>", Pattern.CASE_INSENSITIVE)
    
    // Regex for Pubspec/Json configs (matches: "name": "AppName" or name: AppName)
    private val configRegex = Pattern.compile("name\\s*[:=]\\s*[\"']?([^\"'\\r\\n,]+)[\"']?", Pattern.CASE_INSENSITIVE)

    /**
     * Loops through potential paths and tries to extract the real app name
     */
    suspend fun fetchRealName(repo: GitHubRepo): String? {
        return fetchRealName(repo.owner.login, repo.name, repo.defaultBranch, repo.language)
    }

    /**
     * Loops through potential paths and tries to extract the real app name using raw fields
     */
    suspend fun fetchRealName(owner: String, name: String, defaultBranch: String?, language: String?): String? = withContext(Dispatchers.IO) {
        val urls = AppNameResolver.resolve(owner, name, defaultBranch, language)

        for (url in urls) {
            try {
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val content = response.body?.string()
                    if (content != null) {
                        val parsedName = parseNameFromContent(url, content)
                        if (parsedName != null && parsedName.length > 1 && parsedName.length < 50) {
                            return@withContext parsedName
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore network error for this specific URL and try next
            }
        }
        return@withContext null
    }

    private fun parseNameFromContent(url: String, content: String): String? {
        val lowerUrl = url.lowercase()

        // If it's an XML file (Android)
        if (lowerUrl.endsWith(".xml")) {
            val matcher = xmlRegex.matcher(content)
            if (matcher.find()) {
                // Sometimes string resources point to another resource like @string/name
                // In that case, we can't resolve it deeply via regex easily, so we fallback
                val name = matcher.group(1)?.trim()
                if (name?.startsWith("@string/") == false) {
                    return name
                }
            }
        } 
        // If it's a generic config file (YAML/JSON)
        else if (lowerUrl.endsWith(".yaml") || lowerUrl.endsWith(".json") || lowerUrl.endsWith(".txt")) {
            // For simple txt files like fastlane
            if (lowerUrl.endsWith(".txt")) {
                if (content.isNotBlank()) return content.trim()
            }
            
            val matcher = configRegex.matcher(content)
            if (matcher.find()) {
                val name = matcher.group(1)?.trim()
                return name
            }
        }
        // If it's a markdown file like Readme, finding the App Name is complex via regex
        // We generally skip README parsing here since it's just a raw dump
        return null
    }
}
