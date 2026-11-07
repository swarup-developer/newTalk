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

import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.VisibleForTesting
import com.google.android.libraries.accessibility.utils.log.LogUtils
import org.json.JSONArray
import org.json.JSONException

/**
 * The text that was last copied, newest first, so that the user can paste an older copy.
 *
 * Since Android 10, only the app in front and the keyboard can read the clipboard, so the history
 * holds what newTalk copies itself: its copy and cut commands, the last spoken phrase, and image
 * descriptions. Before Android 10, it also holds what other apps copy.
 *
 * The history is kept in the app's credential-protected storage, so it is not readable before the
 * device is first unlocked. It is never part of a settings backup.
 */
object ClipboardHistory {
  private const val TAG = "ClipboardHistory"
  private const val PREFS = "clipboard_history"
  private const val KEY_ITEMS = "items"

  /** The most copies kept. */
  const val MAX_ITEMS = 20

  /** Longer text, such as a whole screen tree, is copied but not kept. */
  const val MAX_LENGTH = 20_000

  @Volatile private var appContext: Context? = null
  private var clipboard: ClipboardManager? = null
  private val listener = ClipboardManager.OnPrimaryClipChangedListener { recordPrimaryClip() }

  /** Starts keeping copies. Called when the screen reader starts. */
  @JvmStatic
  fun start(context: Context) {
    val app = context.applicationContext
    appContext = app
    if (!canReadClipboard() || clipboard != null) {
      return
    }
    clipboard =
      (app.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.also {
        it.addPrimaryClipChangedListener(listener)
      }
  }

  /** Stops listening for other apps' copies. Called when the screen reader stops. */
  @JvmStatic
  fun stop() {
    clipboard?.removePrimaryClipChangedListener(listener)
    clipboard = null
  }

  /** Adds [text] that newTalk put on the clipboard, if the history has started. */
  @JvmStatic
  fun record(text: CharSequence?) {
    appContext?.let { record(it, text) }
  }

  /** Adds [text] as the newest copy. A copy that is already kept moves to the top. */
  @JvmStatic
  @Synchronized
  fun record(context: Context, text: CharSequence?) {
    val value = text?.toString() ?: return
    val prefs = prefs(context) ?: return
    val items = read(prefs)
    val updated = added(items, value)
    if (updated != items) {
      write(prefs, updated)
    }
  }

  /** Returns the kept copies, newest first. */
  @JvmStatic
  @Synchronized
  fun items(context: Context): List<String> = prefs(context)?.let { read(it) } ?: emptyList()

  /** Forgets every kept copy. */
  @JvmStatic
  @Synchronized
  fun clear(context: Context) {
    prefs(context)?.edit()?.remove(KEY_ITEMS)?.apply()
  }

  /** Returns [items] with [text] added at the top, or [items] if [text] isn't kept. */
  @VisibleForTesting
  @JvmStatic
  fun added(items: List<String>, text: String): List<String> {
    if (text.isBlank() || text.length > MAX_LENGTH) {
      return items
    }
    if (items.firstOrNull() == text) {
      return items
    }
    return (listOf(text) + items.filter { it != text }).take(MAX_ITEMS)
  }

  private fun recordPrimaryClip() {
    val board = clipboard ?: return
    val context = appContext ?: return
    try {
      val clip = board.primaryClip ?: return
      if (clip.itemCount > 0) {
        record(context, clip.getItemAt(0).coerceToText(context))
      }
    } catch (e: SecurityException) {
      LogUtils.w(TAG, "Cannot read the clipboard: %s", e)
    }
  }

  private fun canReadClipboard() = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

  private fun prefs(context: Context): SharedPreferences? =
    try {
      context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    } catch (e: IllegalStateException) {
      // Credential-protected storage is locked until the device is first unlocked after boot.
      null
    }

  private fun read(prefs: SharedPreferences): List<String> {
    val saved = prefs.getString(KEY_ITEMS, null) ?: return emptyList()
    return try {
      val array = JSONArray(saved)
      (0 until array.length()).map { array.getString(it) }
    } catch (e: JSONException) {
      emptyList()
    }
  }

  private fun write(prefs: SharedPreferences, items: List<String>) {
    prefs.edit().putString(KEY_ITEMS, JSONArray(items).toString()).apply()
  }
}
