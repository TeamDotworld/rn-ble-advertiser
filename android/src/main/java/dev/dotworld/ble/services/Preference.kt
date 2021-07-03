package dev.dotworld.ble.services

import android.content.Context

object Preference {
  private const val PREF_ID = "Tracer_pref"
  private const val LAST_PURGE_TIME = "LAST_PURGE_TIME"

  fun getLastPurgeTime(context: Context): Long {
    return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
      .getLong(LAST_PURGE_TIME, 0)
  }
}
