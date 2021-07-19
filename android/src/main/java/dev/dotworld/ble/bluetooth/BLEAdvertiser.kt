package dev.dotworld.ble.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import dev.dotworld.ble.BuildConfig
import dev.dotworld.ble.services.BluetoothMonitoringService
import java.util.*


class BLEAdvertiser constructor(private val serviceUUID: String) {

	private var advertiser: BluetoothLeAdvertiser? =
		BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser
	private var charLength = 3

	companion object {
		private const val TAG = "BLEAdvertiser"
	}

	private var callback: AdvertiseCallback = object : AdvertiseCallback() {
		override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
			super.onStartSuccess(settingsInEffect)
			Log.i(TAG, "Advertising onStartSuccess")
			Log.i(TAG, settingsInEffect.toString())
			isAdvertising = true
		}

		override fun onStartFailure(errorCode: Int) {
			super.onStartFailure(errorCode)

			Log.i(TAG, "onStartFailure: Advertise failed")
			val reason: String

			when (errorCode) {
				ADVERTISE_FAILED_ALREADY_STARTED -> {
					reason = "ADVERTISE_FAILED_ALREADY_STARTED"
					isAdvertising = true
				}
				ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> {
					reason = "ADVERTISE_FAILED_FEATURE_UNSUPPORTED"
					isAdvertising = false
				}
				ADVERTISE_FAILED_INTERNAL_ERROR -> {
					reason = "ADVERTISE_FAILED_INTERNAL_ERROR"
					isAdvertising = false
				}
				ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> {
					reason = "ADVERTISE_FAILED_TOO_MANY_ADVERTISERS"
					isAdvertising = false
				}
				ADVERTISE_FAILED_DATA_TOO_LARGE -> {
					reason = "ADVERTISE_FAILED_DATA_TOO_LARGE"
					isAdvertising = false
					charLength--
				}

				else -> {
					reason = "UNDOCUMENTED"
				}
			}

			Log.d(TAG, "Advertising onStartFailure: $errorCode - $reason")
		}
	}
	private val pUuid = ParcelUuid(UUID.fromString(serviceUUID))

	val settings: AdvertiseSettings = AdvertiseSettings.Builder()
		.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
		.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
		.setConnectable(true)
		.setTimeout(0)
		.build()

	var data: AdvertiseData? = null

	var handler = Handler(Looper.getMainLooper())

	var stopRunnable: Runnable = Runnable {
		Log.i(TAG, "Advertising stopping as scheduled.")
		stopAdvertising()
	}

	var isAdvertising = false
	var shouldBeAdvertising = false

	//reference
	//https://code.tutsplus.com/tutorials/how-to-advertise-android-as-a-bluetooth-le-peripheral--cms-25426
	private fun startAdvertisingLegacy(timeoutInMillis: Long) {

		val randomUUID = UUID.randomUUID().toString()
		val finalString = randomUUID.substring(randomUUID.length - charLength, randomUUID.length)
		Log.d(TAG, "Unique string: $finalString")
		val serviceDataByteArray = finalString.toByteArray()

		data = AdvertiseData.Builder()
			.setIncludeDeviceName(false)
			.setIncludeTxPowerLevel(true)
			.addServiceUuid(pUuid)
			.addManufacturerData(BuildConfig.MANUFACTURER_ID, serviceDataByteArray)
			.build()

		try {
			Log.d(TAG, "Start advertising")
			advertiser = advertiser ?: BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser
			advertiser?.startAdvertising(settings, data, callback)
				?: Log.i(TAG, "startAdvertisingLegacy: Advertiser is null")
		} catch (e: Throwable) {
			Log.e(TAG, "Failed to start advertising legacy: ${e.message}")
		}

		if (!BluetoothMonitoringService.infiniteAdvertising) {
			handler.removeCallbacksAndMessages(stopRunnable)
			handler.postDelayed(stopRunnable, timeoutInMillis)
		}
	}

	fun startAdvertising(timeoutInMillis: Long) {
		startAdvertisingLegacy(timeoutInMillis)
		shouldBeAdvertising = true
	}

	private fun stopAdvertising() {
		try {
			Log.d(TAG, "stop advertising")
			advertiser?.stopAdvertising(callback)
		} catch (e: Throwable) {
			Log.e(TAG, "Failed to stop advertising: ${e.message}")
		}
		shouldBeAdvertising = false
		handler.removeCallbacksAndMessages(null)
	}
}
