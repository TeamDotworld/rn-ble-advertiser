package dev.dotworld.ble.streetpass

import android.content.Context
import android.util.Log
import dev.dotworld.ble.bluetooth.gatt.GattServer
import dev.dotworld.ble.bluetooth.gatt.GattService


class StreetPassServer constructor(val context: Context, val serviceUUIDString: String) {

  companion object {
    private const val TAG = "StreetPassServer"
  }

  private var gattServer: GattServer? = null

  init {
    gattServer = setupGattServer(context, serviceUUIDString)
  }

  private fun setupGattServer(context: Context, serviceUUIDString: String): GattServer? {
    Log.i(TAG, "setupGattServer: Setting gatt server")
    val gattServer = GattServer(context, serviceUUIDString)
    val started = gattServer.startServer()

    if (started) {
      val service = GattService(context, serviceUUIDString)

      gattServer.addService(service)
      return gattServer
    }
    return null
  }

  fun tearDown() {
    gattServer?.stop()
  }

  fun checkServiceAvailable(): Boolean {
    return gattServer?.bluetoothGattServer?.services?.filter {
      it.uuid.toString().equals(serviceUUIDString)
    }?.isNotEmpty() ?: false
  }
}
