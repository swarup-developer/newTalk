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

import android.content.ClipboardManager
import android.content.Context
import android.view.Menu
import androidx.annotation.VisibleForTesting
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.google.android.accessibility.talkback.Feedback
import com.google.android.accessibility.talkback.Pipeline
import com.google.android.accessibility.talkback.R
import com.google.android.accessibility.talkback.utils.ClipboardUtils
import com.google.android.accessibility.utils.ClipboardHistory
import com.google.android.accessibility.utils.Performance.EVENT_ID_UNTRACKED
import com.google.android.accessibility.utils.Role

/**
 * Fills the Clipboard history menu, which lists the text copied most recently, newest first.
 *
 * It only pastes: choosing a copy pastes it into the focused text box. Away from a text box, the
 * menu item says that there is none instead of listing the copies.
 */
object ClipboardHistoryMenuProcessor {
  /** The longest part of a copy that its menu item says. */
  @VisibleForTesting const val MAX_TITLE_LENGTH = 100

  /** Whether [node] is a text box that copies can be pasted into. */
  @JvmStatic
  fun isTextBox(node: AccessibilityNodeInfoCompat?): Boolean =
    node != null && (Role.getRole(node) == Role.ROLE_EDIT_TEXT || node.isEditable)

  /**
   * Adds Clipboard history to the newTalk menu: a submenu of copies in a text box, or an item that
   * says why there is nothing to paste.
   */
  @JvmStatic
  fun addToMenu(
    context: Context,
    pipeline: Pipeline.FeedbackReturner,
    node: AccessibilityNodeInfoCompat?,
    menu: ContextMenu,
    itemId: Int,
    order: Int,
  ) {
    val title = context.getString(R.string.title_clipboard_history)
    val reason = unavailableReason(context, node)
    if (reason != null) {
      val item = ContextMenu.createMenuItem(context, Menu.NONE, itemId, order, title)
      item.setOnMenuItemClickListener {
        pipeline.returnFeedback(EVENT_ID_UNTRACKED, Feedback.speech(reason))
        true
      }
      menu.add(item)
      return
    }
    val subMenu = menu.addSubMenu(/* groupId= */ 0, itemId, order, title)
    addItems(context, pipeline, node!!, subMenu::add)
  }

  /** Fills the menu that a gesture opens, which [unavailableReason] has allowed. */
  @JvmStatic
  fun prepareMenu(
    context: Context,
    pipeline: Pipeline.FeedbackReturner,
    node: AccessibilityNodeInfoCompat?,
    menu: ContextMenu,
  ) {
    if (unavailableReason(context, node) == null) {
      addItems(context, pipeline, node!!, menu::add)
    }
  }

  /**
   * Returns what to say instead of listing copies: that there is no text box, or nothing copied.
   * Returns null when the copies can be listed.
   */
  @JvmStatic
  fun unavailableReason(context: Context, node: AccessibilityNodeInfoCompat?): String? =
    when {
      !isTextBox(node) -> context.getString(R.string.clipboard_history_no_text_box)
      ClipboardHistory.items(context).isEmpty() ->
        context.getString(R.string.clipboard_history_empty)
      else -> null
    }

  private fun addItems(
    context: Context,
    pipeline: Pipeline.FeedbackReturner,
    field: AccessibilityNodeInfoCompat,
    add: (ContextMenuItem) -> Unit,
  ) {
    for (text in ClipboardHistory.items(context)) {
      val item =
        ContextMenu.createMenuItem(
          context,
          R.id.group_clipboard_history,
          Menu.NONE,
          Menu.NONE,
          title(text),
        )
      item.setOnMenuItemClickListener {
        paste(context, pipeline, field, text)
        true
      }
      add(item)
    }
    val clear =
      ContextMenu.createMenuItem(
        context,
        R.id.group_clipboard_history,
        R.id.clipboard_history_clear,
        Menu.NONE,
        context.getString(R.string.clipboard_history_clear),
      )
    clear.setOnMenuItemClickListener {
      ClipboardHistory.clear(context)
      pipeline.returnFeedback(
        EVENT_ID_UNTRACKED,
        Feedback.speech(context.getString(R.string.clipboard_history_cleared)),
      )
      true
    }
    add(clear)
  }

  /** Returns what the menu item for [text] says: its start, on one line. */
  @VisibleForTesting
  @JvmStatic
  fun title(text: String): String {
    val line = text.trim().replace(Regex("""\s+"""), " ")
    return if (line.length <= MAX_TITLE_LENGTH) line else line.take(MAX_TITLE_LENGTH).trimEnd() + "…"
  }

  /** Puts [text] on the clipboard and pastes it into [field]. */
  private fun paste(
    context: Context,
    pipeline: Pipeline.FeedbackReturner,
    field: AccessibilityNodeInfoCompat,
    text: String,
  ) {
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    if (clipboard == null || !ClipboardUtils.copyToClipboard(clipboard, text)) {
      pipeline.returnFeedback(EVENT_ID_UNTRACKED, Feedback.sound(R.raw.complete))
      return
    }
    pipeline.returnFeedback(EVENT_ID_UNTRACKED, Feedback.edit(field, Feedback.EditText.Action.PASTE))
  }
}
