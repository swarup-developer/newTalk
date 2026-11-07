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

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCheckerTest {
  private fun release(build: Int, vararg commits: String): String =
    JSONObject()
      .put("body", (listOf("build: $build", "") + commits.map { "- $it" }).joinToString("\n"))
      .put(
        "assets",
        JSONArray()
          .put(
            JSONObject()
              .put("name", "newtalk.apk")
              .put("browser_download_url", "https://example.com/newtalk.apk")
          ),
      )
      .toString()

  @Test
  fun olderOrSameBuildIsNotAnUpdate() {
    assertNull(UpdateChecker.parseRelease(release(10), 10, ""))
    assertNull(UpdateChecker.parseRelease(release(9), 10, ""))
  }

  @Test
  fun newerBuildIsAnUpdate() {
    val update = UpdateChecker.parseRelease(release(11, "bbbbbbb Two", "aaaaaaa One"), 10, "aaaaaaa0")
    assertEquals(11, update!!.build)
    assertEquals(listOf("Two"), update.changes)
    assertFalse(update.move)
  }

  @Test
  fun moveIsOfferedWhateverTheBuild() {
    val move = UpdateChecker.parseMove(release(5, "bbbbbbb New ID", "aaaaaaa Old"), "aaaaaaa0")
    assertTrue(move!!.move)
    assertEquals(5, move.build)
    assertEquals(listOf("New ID"), move.changes)
  }

  @Test
  fun channels() {
    assertEquals("latest", UpdateChecker.OLD_CHANNEL)
    assertEquals("dev", UpdateChecker.NEW_CHANNEL)
  }

  private fun releaseWithBothBuilds(build: Int): String {
    val release = JSONObject(release(build))
    release
      .getJSONArray("assets")
      .put(
        JSONObject()
          .put("name", "newtalk-wear.apk")
          .put("browser_download_url", "https://example.com/newtalk-wear.apk")
      )
    return release.toString()
  }

  @Test
  fun eachDeviceGetsItsOwnBuild() {
    val json = releaseWithBothBuilds(11)
    assertEquals(
      "https://example.com/newtalk.apk",
      UpdateChecker.parseRelease(json, 10, "", UpdateChecker.PHONE_APK_NAME)!!.downloadUrl,
    )
    assertEquals(
      "https://example.com/newtalk-wear.apk",
      UpdateChecker.parseRelease(json, 10, "", UpdateChecker.WATCH_APK_NAME)!!.downloadUrl,
    )
  }

  @Test(expected = JSONException::class)
  fun aWatchNeverTakesThePhoneBuild() {
    UpdateChecker.parseRelease(release(11), 10, "", UpdateChecker.WATCH_APK_NAME)
  }

  @Test
  fun theBuildWithoutPlayServicesGetsItsOwnBuild() {
    val release = JSONObject(releaseWithBothBuilds(11))
    release
      .getJSONArray("assets")
      .put(
        JSONObject()
          .put("name", "newtalk-nogms.apk")
          .put("browser_download_url", "https://example.com/newtalk-nogms.apk")
      )
    assertEquals(
      "https://example.com/newtalk-nogms.apk",
      UpdateChecker.parseRelease(release.toString(), 10, "", UpdateChecker.NO_PLAY_SERVICES_APK_NAME)!!
        .downloadUrl,
    )
  }

  @Test(expected = JSONException::class)
  fun theBuildWithoutPlayServicesNeverTakesThePhoneBuild() {
    UpdateChecker.parseRelease(release(11), 10, "", UpdateChecker.NO_PLAY_SERVICES_APK_NAME)
  }

  @Test
  fun buildNumberCanBeExtractedFromTagName() {
    val json = JSONObject()
      .put("tag_name", "dev-15")
      .put("body", "Release notes without explicit build prefix")
      .put(
        "assets",
        JSONArray().put(
          JSONObject()
            .put("name", "newtalk.apk")
            .put("browser_download_url", "https://example.com/newtalk.apk")
        ),
      )
      .toString()
    val update = UpdateChecker.parseRelease(json, 10, "")
    assertEquals(15, update!!.build)
    assertEquals("https://example.com/newtalk.apk", update.downloadUrl)
  }
}
