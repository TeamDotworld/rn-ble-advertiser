package dev.dotworld.ble.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dev.dotworld.ble.AppPreferences
import dev.dotworld.ble.Utils

class RestartReceiver : BroadcastReceiver() {

	companion object {
		private const val TAG = "RestartReceiver"
	}

	override fun onReceive(context: Context, intent: Intent) {
		try {
			if (intent.action == Intent.ACTION_BOOT_COMPLETED && AppPreferences.needStart) {
				Log.d(TAG, "onReceive: Starting ble service")
				Utils.startBluetoothMonitoringService(context)
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
}
