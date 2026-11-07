/*
 * Copyright 2026 Backtalk contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.android.accessibility.talkback.update

import android.content.Context
import android.net.Network
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import com.google.android.accessibility.talkback.BuildConfig
import com.google.android.accessibility.talkback.migration.AppIdMove
import com.google.android.accessibility.utils.FormFactorUtils
import com.google.android.libraries.accessibility.utils.log.LogUtils
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/** A development build that is newer than the running one. */
data class UpdateInfo(
  /** The build number, which is the number of commits in the build. */
  val build: Int,
  /** Where to download the APK. */
  val downloadUrl: String,
  /** The subjects of the commits that are newer than the running build, newest first. */
  val changes: List<String>,
  /** True if the build is newTalk under its new app ID, which installs as a new app. */
  val move: Boolean = false,
)

/**
 * Checks GitHub releases for a newer development build.
 *
 * newTalk builds are on the GitHub releases of the configured repository ([BuildConfig.GITHUB_REPO]).
 * A build running under the old app ID [AppIdMove.OLD_PACKAGE] checks "latest" for its own updates,
 * and is offered the move to newTalk whenever "dev" exists.
 *
 * CI writes the release notes as a line "build: <commit count>", then one line per commit as "-
 * <7-character hash> <subject>", newest first. A release is newer only when its commit count is
 * higher, so a local build of a newer commit is never offered an older build.
 */
object UpdateChecker {
  private const val TAG = "UpdateChecker"

  @VisibleForTesting
  var releasesBaseUrlOverride: String? = null

  private val releasesBaseUrl: String
    get() = releasesBaseUrlOverride ?: "https://api.github.com/repos/${BuildConfig.GITHUB_REPO}/releases"

  /** The pre-release for the old app ID com.android.talkback. */
  const val OLD_CHANNEL = "latest"

  /** The pre-release for the app ID io.github.swarupdeveloper.newtalk. */
  const val NEW_CHANNEL = "dev"

  /** The phone build, which is also what builds before the watch build was published look for. */
  @VisibleForTesting const val PHONE_APK_NAME = "newtalk.apk"
  /** The watch build, so that a watch never installs the phone app. */
  @VisibleForTesting const val WATCH_APK_NAME = "newtalk-wear.apk"
  /**
   * The phone build with ML Kit's text recognition model, for phones without Google Play services,
   * so that it never updates to a build whose text recognition needs them.
   */
  @VisibleForTesting const val NO_PLAY_SERVICES_APK_NAME = "newtalk-nogms.apk"
  private const val SHORT_HASH_LENGTH = 7
  private const val MAX_CHANGES = 50
  private const val TIMEOUT_MS = 30_000

  private val BUILD_LINE = Regex("""build:\s*(\d+)""")
  private val TAG_BUILD_REGEX = Regex("""\b(\d+)\b""")

  /**
   * Returns the newer build, or null if this build is current. This blocks on the network.
   *
   * @throws IOException if GitHub cannot be reached or returns an error.
   */
  @WorkerThread
  fun check(context: Context): UpdateInfo? {
    val channel =
      if (context.packageName == AppIdMove.OLD_PACKAGE) {
        checkMove(context)?.let {
          return it
        }
        OLD_CHANNEL
      } else {
        NEW_CHANNEL
      }
    val json = fetchReleaseWithFallback(context, channel) ?: return null
    return try {
      parseRelease(json, BuildConfig.COMMIT_COUNT, BuildConfig.COMMIT_HASH, apkName())
    } catch (e: JSONException) {
      LogUtils.w(TAG, "Release JSON cannot be used: %s", e)
      null
    }
  }

  /**
   * Returns the newest build under the new app ID, whatever its build number, since the new app
   * is always the way forward. Returns null if there is none yet or it cannot be read, so the old
   * app still gets updates of its own.
   */
  @WorkerThread
  private fun checkMove(context: Context): UpdateInfo? =
    try {
      fetchRelease(context, NEW_CHANNEL)?.let { parseMove(it, BuildConfig.COMMIT_HASH, apkName()) }
    } catch (e: Exception) {
      LogUtils.w(TAG, "Cannot check for the new app: %s", e)
      null
    }

  /** Returns the release with the [tag] as JSON, or null if GitHub has no such release. */
  @WorkerThread
  fun fetchRelease(context: Context, tag: String): String? =
    fetchReleaseWithFallback(context, tag)

  /**
   * Queries releases with fallback:
   * 1. /tags/$tag
   * 2. /latest
   * 3. /releases (taking the first release in the repository)
   * Returns null if no release exists.
   */
  @WorkerThread
  fun fetchReleaseWithFallback(context: Context, tag: String): String? {
    // 1. Try specific tag: /tags/$tag
    val tagUrl = "$releasesBaseUrl/tags/$tag"
    val tagJson = fetchUrl(context, tagUrl)
    if (tagJson != null) {
      return tagJson
    }
    // 2. Try latest release: /latest
    val latestUrl = "$releasesBaseUrl/latest"
    val latestJson = fetchUrl(context, latestUrl)
    if (latestJson != null) {
      return latestJson
    }
    // 3. Try releases list
    val listJson = fetchUrl(context, releasesBaseUrl)
    if (listJson != null) {
      try {
        val array = JSONArray(listJson)
        if (array.length() > 0) {
          return array.getJSONObject(0).toString()
        }
      } catch (e: Exception) {
        LogUtils.w(TAG, "Cannot parse releases list: %s", e)
      }
    }
    return null
  }

  @WorkerThread
  fun fetchUrl(context: Context, url: String): String? {
    val connection = openConnection(context, url)
    try {
      connection.setRequestProperty("Accept", "application/vnd.github+json")
      val code = connection.responseCode
      if (code == HttpURLConnection.HTTP_NOT_FOUND) {
        return null
      }
      if (code != HttpURLConnection.HTTP_OK) {
        throw IOException("GitHub returned HTTP $code for $url")
      }
      return connection.inputStream.bufferedReader().use { it.readText() }
    } finally {
      connection.disconnect()
    }
  }

  /** Parses a release under the new app ID as a move, which is offered whatever its build. */
  @VisibleForTesting
  fun parseMove(
    json: String,
    currentHash: String,
    apkName: String = PHONE_APK_NAME,
  ): UpdateInfo? = parseRelease(json, currentBuild = 0, currentHash, apkName)?.copy(move = true)

  /**
   * Opens a connection with the timeouts and user agent that GitHub requests should use. The
   * connection goes over [network] if there is one, and over the default network otherwise. Only
   * this connection uses [network], including its name lookups and redirects.
   */
  fun openConnection(context: Context, url: String, network: Network? = null): HttpURLConnection {
    val target = URL(url)
    val connection =
      (network?.openConnection(target) ?: target.openConnection()) as HttpURLConnection
    connection.connectTimeout = TIMEOUT_MS
    connection.readTimeout = TIMEOUT_MS
    connection.setRequestProperty("User-Agent", "newTalk/${versionName(context)}")
    return connection
  }

  private fun apkName(): String =
    when {
      FormFactorUtils.isAndroidWear() -> WATCH_APK_NAME
      BuildConfig.BUNDLED_OCR -> NO_PLAY_SERVICES_APK_NAME
      else -> PHONE_APK_NAME
    }

  @VisibleForTesting
  fun parseRelease(
    json: String,
    currentBuild: Int,
    currentHash: String,
    apkName: String = PHONE_APK_NAME,
  ): UpdateInfo? {
    val release = JSONObject(json)
    val lines = release.optString("body").lines().map { it.trim() }
    val build =
      lines.firstNotNullOfOrNull { BUILD_LINE.matchEntire(it)?.groupValues?.get(1)?.toIntOrNull() }
        ?: extractBuildFromTag(release.optString("tag_name"))
        ?: throw JSONException("The release has no build number")
    if (build <= currentBuild) {
      return null
    }

    val assets = release.getJSONArray("assets")
    val downloadUrl =
      (0 until assets.length())
        .map { assets.getJSONObject(it) }
        .firstOrNull { it.optString("name") == apkName }
        ?.getString("browser_download_url")
        ?: throw JSONException("The release has no $apkName")

    val commits = lines.filter { it.startsWith("- ") }.map { it.removePrefix("- ") }
    val shortHash = currentHash.take(SHORT_HASH_LENGTH)
    val currentIndex =
      if (shortHash.length == SHORT_HASH_LENGTH) {
        commits.indexOfFirst { it.substringBefore(' ') == shortHash }
      } else {
        -1
      }
    val newCommits =
      if (currentIndex >= 0) {
        commits.subList(0, currentIndex)
      } else {
        commits.take((build - currentBuild).coerceAtMost(MAX_CHANGES))
      }
    return UpdateInfo(build, downloadUrl, newCommits.map { it.substringAfter(' ') })
  }

  private fun extractBuildFromTag(tag: String?): Int? {
    if (tag.isNullOrEmpty()) return null
    return TAG_BUILD_REGEX.find(tag)?.groupValues?.get(1)?.toIntOrNull()
  }

  private fun versionName(context: Context): String =
    try {
      context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
    } catch (e: Exception) {
      ""
    }
}
