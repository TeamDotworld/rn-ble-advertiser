package dev.dotworld.ble

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import dev.dotworld.ble.bluetooth.gatt.GattBackgroundService

object Utils {

	private val NOTIFICATION_ID = BuildConfig.SERVICE_FOREGROUND_NOTIFICATION_ID
	private val CHANNEL_ID = BuildConfig.SERVICE_FOREGROUND_CHANNEL_ID
	private val CHANNEL_NAME = BuildConfig.SERVICE_FOREGROUND_CHANNEL_NAME
	private val TAG = "Utils"

	fun startBluetoothMonitoringService(context: Context) {
		val intent = Intent(context, GattBackgroundService::class.java)

		val isRunning = context.isMyServiceRunning(GattBackgroundService::class.java)
		if (isRunning) {
			Log.d(TAG, "startBluetoothMonitoringService: Service already running")
		} else {
			Log.i("Utils", "startBluetoothMonitoringService: Starting new service")
			context.startService(intent)
			Log.i(TAG, "startBluetoothMonitoringService: New service start request sent")
		}
	}

	private fun Context.isMyServiceRunning(serviceClass: Class<*>): Boolean {
		val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
		return manager.getRunningServices(Integer.MAX_VALUE)
			.any { it.service.className == serviceClass.name }
	}

	fun stopBluetoothMonitoringService(context: Context) {
		val intent = Intent(context, GattBackgroundService::class.java)
		context.stopService(intent)
		Log.i(TAG, "stopBluetoothMonitoringService: Stopping ble service")
	}


	fun createNotification(context: Context): Notification {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val serviceChannel = NotificationChannel(
				CHANNEL_ID,
				CHANNEL_NAME,
				NotificationManager.IMPORTANCE_HIGH
			)
			val manager =
				context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
			manager.createNotificationChannel(serviceChannel)
		}

		return NotificationCompat.Builder(context, CHANNEL_ID)
			.setContentTitle(CHANNEL_NAME)
			.setContentText("Please restart the app if this service is not running")
			.setSmallIcon(R.drawable.ic_baseline_bluetooth_24)
			.build()
	}


	fun isBluetoothAvailable(): Boolean {
		val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
		return bluetoothAdapter != null &&
			bluetoothAdapter.isEnabled && bluetoothAdapter.state == BluetoothAdapter.STATE_ON
	}

}
