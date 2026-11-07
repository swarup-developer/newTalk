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

package com.google.android.accessibility.talkback.scripting

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ScriptSourcesListTest {
  private val context: Context = RuntimeEnvironment.getApplication()
  private val one = ScriptSource.parse("github.com/a/b")!!
  private val two = ScriptSource.parse("github.com/c/d")!!

  private fun addresses(kind: SourceKind = SourceKind.SCRIPTS) =
    ScriptSources.all(context, kind).map { it.address }

  @Test
  fun scriptSourcesStartEmpty() {
    assertEquals(emptyList<String>(), addresses())
  }

  @Test
  fun soundThemeSourcesStartEmpty() {
    assertEquals(emptyList<String>(), addresses(SourceKind.SOUND_THEMES))
  }

  @Test
  fun addedSourcesKeepTheirOrder() {
    assertTrue(ScriptSources.add(context, one))
    assertTrue(ScriptSources.add(context, two))

    assertEquals(listOf(one.address, two.address), addresses())
  }

  @Test
  fun aSourceIsAddedOnlyOnce() {
    assertTrue(ScriptSources.add(context, one))

    assertEquals(false, ScriptSources.add(context, one))
  }

  @Test
  fun aThemeSourceLeavesScriptSourcesAlone() {
    ScriptSources.add(context, one, SourceKind.SOUND_THEMES)

    assertEquals(listOf(one.address), addresses(SourceKind.SOUND_THEMES))
    assertEquals(emptyList<String>(), addresses())
  }

  @Test
  fun aRemovedSourceStaysRemoved() {
    ScriptSources.add(context, one)
    ScriptSources.remove(context, one)

    assertEquals(emptyList<String>(), addresses())
  }
}
