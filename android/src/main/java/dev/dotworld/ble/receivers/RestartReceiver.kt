package dev.dotworld.ble.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.dotworld.ble.AppPreferences
import dev.dotworld.ble.Utils

class RestartReceiver : BroadcastReceiver() {

	override fun onReceive(context: Context, intent: Intent) {
		try {
			if (intent.action == Intent.ACTION_BOOT_COMPLETED && AppPreferences.needStart) {
				Utils.startBluetoothMonitoringService(context)
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
}
