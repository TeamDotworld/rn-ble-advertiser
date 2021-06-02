package com.reactnativebleadvertiser

import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import dev.dotworld.ble.Utils

class BleAdvertiserModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  companion object {
    private const val TAG = "BleAdvertiserModule"
  }

  override fun getName(): String {
    return "BleAdvertiser"
  }

  @ReactMethod
  fun setUserId(userId: String) {
    Log.i(TAG, "setUserId: $userId")
  }

  @ReactMethod
  fun resetUserId() {
    Log.i(TAG, "resetUserId")
  }

  @ReactMethod
  fun startService() {
    Log.i(TAG, "Start Service")
    Utils.startBluetoothMonitoringService(reactApplicationContext)
  }

}
