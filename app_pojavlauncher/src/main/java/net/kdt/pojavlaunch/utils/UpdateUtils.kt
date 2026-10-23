package net.kdt.pojavlaunch.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.ashmeet.hyperlauncher.BuildConfig
import net.kdt.pojavlaunch.Tools
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object UpdateUtils {
    private const val GITHUB_API_URL = "https://api.github.com/repos/hollowlauncher/HyperLauncher/releases/latest"

    data class UpdateInfo(
        val hasUpdate: Boolean,
        val latestVersion: String,
        val updateUrl: String,
        val changelog: String
    )

    suspend fun checkForUpdates(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL(GITHUB_API_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val latestTagName = json.getString("tag_name").trimStart('v')
                val htmlUrl = json.getString("html_url")
                val body = json.optString("body", "No changelog provided.")

                val currentVersion = BuildConfig.VERSION_NAME.substringBefore('-').trimStart('v')

                val hasUpdate = isNewerVersion(currentVersion, latestTagName)

                return@withContext UpdateInfo(
                    hasUpdate = hasUpdate,
                    latestVersion = latestTagName,
                    updateUrl = htmlUrl,
                    changelog = body
                )
            }
        } catch (e: Exception) {
            Log.e("UpdateUtils", "Failed to check for updates", e)
        }
        return@withContext null
    }

    private fun isNewerVersion(current: String, latest: String): Boolean {
        try {
            val currentParts = current.split('.').mapNotNull { it.toIntOrNull() }
            val latestParts = latest.split('.').mapNotNull { it.toIntOrNull() }

            val size = maxOf(currentParts.size, latestParts.size)
            for (i in 0 until size) {
                val curr = currentParts.getOrNull(i) ?: 0
                val late = latestParts.getOrNull(i) ?: 0
                if (late > curr) return true
                if (late < curr) return false
            }
        } catch (e: Exception) {
            return latest != current
        }
        return false
    }
}
