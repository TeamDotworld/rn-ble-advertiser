package dev.dotworld.ble

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {
	private const val NAME = "ble_interlock"
	private const val MODE = Context.MODE_PRIVATE
	private lateinit var preferences: SharedPreferences

	private val USER_ID = Pair("user_id", null)
	private val NEED_START = Pair("need_start", false)

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

	var needStart: Boolean
		get() = preferences.getBoolean(NEED_START.first, NEED_START.second)
		set(value) = preferences.edit {
			it.putBoolean(USER_ID.first, value)
		}
}
