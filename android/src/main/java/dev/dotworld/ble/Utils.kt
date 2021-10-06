package dev.dotworld.ble

import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.core.app.NotificationCompat
import dev.dotworld.ble.services.BluetoothMonitoringService
import dev.dotworld.ble.services.Scheduler
import java.io.ByteArrayOutputStream
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher


object Utils {

	private const val TAG = "Utils"
	private val CHANNEL_ID = UUID.randomUUID().toString()
	private const val NOTIFY_ID = 12121

	const val transformation = "RSA"
	const val ENCRYPT_MAX_SIZE = 117

	fun startBluetoothMonitoringService(context: Context) {
		val intent = Intent(context, BluetoothMonitoringService::class.java)
		intent.putExtra(
			BluetoothMonitoringService.COMMAND_KEY,
			BluetoothMonitoringService.Command.ACTION_START.index
		)
		Log.i("Utils", "startBluetoothMonitoringService: Starting new service")
		context.startService(intent)
		Log.i(TAG, "startBluetoothMonitoringService: New service start request sent")
	}

	fun stopBluetoothMonitoringService(context: Context) {
		val intent = Intent(context, BluetoothMonitoringService::class.java)
		context.stopService(intent)
		Log.i(TAG, "stopBluetoothMonitoringService: Stopping ble service")
	}


	fun isBluetoothAvailable(): Boolean {
		val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
		return bluetoothAdapter != null &&
			bluetoothAdapter.isEnabled && bluetoothAdapter.state == BluetoothAdapter.STATE_ON
	}

	fun scheduleRepeatingPurge(context: Context, intervalMillis: Long) {
		val nextIntent = Intent(context, BluetoothMonitoringService::class.java)
		nextIntent.putExtra(
			BluetoothMonitoringService.COMMAND_KEY,
			BluetoothMonitoringService.Command.ACTION_PURGE.index
		)

		Scheduler.scheduleRepeatingServiceIntent(
			BluetoothMonitoringService.PENDING_PURGE_CODE,
			context,
			nextIntent,
			intervalMillis
		)
	}

	fun scheduleNextHealthCheck(context: Context, timeInMillis: Long) {
		//cancels any outstanding check schedules.
		cancelNextHealthCheck(context)

		val nextIntent = Intent(context, BluetoothMonitoringService::class.java)
		nextIntent.putExtra(
			BluetoothMonitoringService.COMMAND_KEY,
			BluetoothMonitoringService.Command.ACTION_SELF_CHECK.index
		)
		//runs every XXX milliseconds - every minute?
		Scheduler.scheduleServiceIntent(
			BluetoothMonitoringService.PENDING_HEALTH_CHECK_CODE,
			context,
			nextIntent,
			timeInMillis
		)
	}

	fun cancelNextHealthCheck(context: Context) {
		val nextIntent = Intent(context, BluetoothMonitoringService::class.java)
		nextIntent.putExtra(
			BluetoothMonitoringService.COMMAND_KEY,
			BluetoothMonitoringService.Command.ACTION_SELF_CHECK.index
		)
		Scheduler.cancelServiceIntent(
			BluetoothMonitoringService.PENDING_HEALTH_CHECK_CODE,
			context,
			nextIntent
		)
	}

	fun notifyUser(context: Context) {
		try {
			Log.i(TAG, "notifyUser: Creating notification")

			val mBuilder =
				NotificationCompat.Builder(context.applicationContext, "notify_001")
					.setSmallIcon(R.drawable.bluetooth)
					.setContentTitle("Authentication Notification")
					.setContentText("You are being authenticated by BLE Tracker")
					.setPriority(NotificationCompat.PRIORITY_HIGH)

			val mNotificationManager =
				context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				val channelId = CHANNEL_ID
				val channel = NotificationChannel(
					channelId,
					"BLE Tracker Notification",
					NotificationManager.IMPORTANCE_HIGH
				)
				mNotificationManager.createNotificationChannel(channel)
				mBuilder.setChannelId(channelId)
			}

			mNotificationManager.notify(0, mBuilder.build())
			Log.i(TAG, "notifyUser: Created notification")

		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	fun encryptByPublicKey(str: String, publicKey: String): String {
		val keyFactory = KeyFactory.getInstance("RSA")
		val publicKeyReverse =
			keyFactory.generatePublic(
				X509EncodedKeySpec(Base64.decode(publicKey, Base64.NO_WRAP))
			)

		val byteArray = str.toByteArray()
		val cipher = Cipher.getInstance(transformation)
		cipher.init(Cipher.ENCRYPT_MODE, publicKeyReverse)

		var temp: ByteArray? = null
		var offset = 0

		val outputStream = ByteArrayOutputStream()

		while (byteArray.size - offset > 0) {
			if (byteArray.size - offset >= ENCRYPT_MAX_SIZE) {
				temp = cipher.doFinal(byteArray, offset, ENCRYPT_MAX_SIZE)
				offset += ENCRYPT_MAX_SIZE
			} else {
				temp = cipher.doFinal(byteArray, offset, (byteArray.size - offset))
				offset = byteArray.size
			}
			outputStream.write(temp)
		}

		outputStream.close()
		return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
	}


}
