package com.reactnativebleadvertiser

import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import dev.dotworld.ble.AppPreferences
import dev.dotworld.ble.Utils

class BleAdvertiserModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  companion object {
    private const val TAG = "BleAdvertiserModule"
  }

  init {
    AppPreferences.init(reactContext.applicationContext)
  }

  override fun getName(): String {
    return "BleAdvertiser"
  }

  @ReactMethod
  fun setUserId(userId: String) {
    Log.i(TAG, "setUserId: $userId")
    AppPreferences.userId = userId
  }

  @ReactMethod
  fun resetUserId() {
    Log.i(TAG, "resetUserId")
    AppPreferences.userId = null
  }

  @ReactMethod
  fun startService() {
    Log.i(TAG, "Start Service")
    if (AppPreferences.userId != null) {
      Utils.startBluetoothMonitoringService(reactApplicationContext)
    }
  }

}
