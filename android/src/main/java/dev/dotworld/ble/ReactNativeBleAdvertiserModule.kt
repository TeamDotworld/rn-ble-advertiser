package dev.dotworld.ble

import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import io.sentry.Sentry
import io.sentry.protocol.User


class ReactNativeBleAdvertiserModule(reactContext: ReactApplicationContext) :
	ReactContextBaseJavaModule(reactContext) {

	companion object {
		private const val TAG = "RNBleAdvertiserModule"
	}

	init {
		AppPreferences.init(reactContext.applicationContext)
	}

	override fun getName(): String {
		return "ReactNativeBleAdvertiser"
	}

	@ReactMethod
	fun initializeBle() {
		Sentry.captureMessage("Initializing BLE")
		Log.i(TAG, "initialize: initializeBle Called")
	}

	@ReactMethod
	fun setData(data: String) {
		AppPreferences.userId = data
		try {
			val user = User()
			user.id = data
			Sentry.setUser(user)
			Sentry.captureMessage("Set Data")
		} catch (e: Exception) { }
		Log.i(TAG, "setData $data in App prefs as '${AppPreferences.userId}'")
	}

	@ReactMethod
	fun startBroadcast() {
		Log.i(TAG, "Start Service")
		try {
			AppPreferences.needStart = true
			Utils.startBluetoothMonitoringService(reactApplicationContext)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	@ReactMethod
	fun stopBroadcast() {
		Log.i(TAG, "stopBroadcast")
		try {
			AppPreferences.needStart = false
			Utils.stopBluetoothMonitoringService(reactApplicationContext)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
}
