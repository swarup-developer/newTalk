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

package com.google.android.accessibility.talkback.contextmenu

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ClipboardHistoryMenuProcessorTest {
  @Test
  fun aShortCopyIsSaidWhole() {
    assertEquals("Meet at 5", ClipboardHistoryMenuProcessor.title("Meet at 5"))
  }

  @Test
  fun aCopyIsSaidOnOneLine() {
    assertEquals(
      "Address: 12 Park Road Delhi",
      ClipboardHistoryMenuProcessor.title("  Address:\n12 Park Road\n\tDelhi \n"),
    )
  }

  @Test
  fun aLongCopyIsCutShort() {
    val title = ClipboardHistoryMenuProcessor.title("abcdefghij".repeat(30))

    assertEquals(ClipboardHistoryMenuProcessor.MAX_TITLE_LENGTH + 1, title.length)
    assertEquals('…', title.last())
  }

  @Test
  fun nothingIsATextBoxWithoutFocus() {
    assertFalse(ClipboardHistoryMenuProcessor.isTextBox(null))
  }
}
