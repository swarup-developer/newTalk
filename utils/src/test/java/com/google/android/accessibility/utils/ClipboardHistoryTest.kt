/*
 * Copyright 2026 newTalk contributors
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

package com.google.android.accessibility.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class ClipboardHistoryTest {
  @Test
  fun aNewCopyGoesFirst() {
    assertEquals(listOf("b", "a"), ClipboardHistory.added(listOf("a"), "b"))
  }

  @Test
  fun copyingTheNewestAgainChangesNothing() {
    val items = listOf("a", "b")
    assertEquals(items, ClipboardHistory.added(items, "a"))
  }

  @Test
  fun anOlderCopyMovesToTheTopOnce() {
    assertEquals(listOf("c", "a", "b"), ClipboardHistory.added(listOf("a", "b", "c"), "c"))
  }

  @Test
  fun onlyTheNewestCopiesAreKept() {
    val full = (1..ClipboardHistory.MAX_ITEMS).map { "item $it" }

    val updated = ClipboardHistory.added(full, "new")

    assertEquals(ClipboardHistory.MAX_ITEMS, updated.size)
    assertEquals("new", updated.first())
    assertEquals("item ${ClipboardHistory.MAX_ITEMS - 1}", updated.last())
  }

  @Test
  fun blankAndVeryLongTextIsNotKept() {
    val items = listOf("a")
    assertEquals(items, ClipboardHistory.added(items, "  \n "))
    assertEquals(items, ClipboardHistory.added(items, "x".repeat(ClipboardHistory.MAX_LENGTH + 1)))
  }
}
