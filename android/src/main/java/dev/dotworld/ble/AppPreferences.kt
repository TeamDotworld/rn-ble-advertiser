package dev.dotworld.ble

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {
  private const val NAME = "beambox"
  private const val MODE = Context.MODE_PRIVATE
  private lateinit var preferences: SharedPreferences

  private val USER_ID = Pair("user_id", null)

  fun init(context: Context) {
    preferences = context.getSharedPreferences(NAME, MODE)
  }

  private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
    val editor = edit()
    operation(editor)
    editor.apply()
  }

  var userId: String?
    get() = preferences.getString(USER_ID.first, USER_ID.second)
    set(value) = preferences.edit {
      it.putString(USER_ID.first, value)
    }
}
