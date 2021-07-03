package dev.dotworld.ble.protocol.v2

import android.os.Build
import android.util.Log
import dev.dotworld.ble.AppPreferences
import dev.dotworld.ble.protocol.BlueTraceProtocol
import dev.dotworld.ble.protocol.PeripheralInterface
import dev.dotworld.ble.streetpass.CentralDevice
import dev.dotworld.ble.streetpass.ConnectionRecord
import dev.dotworld.ble.streetpass.PeripheralDevice


class BlueTraceV2 : BlueTraceProtocol(
  versionInt = 2,
  peripheral = V2Peripheral(),
)

class V2Peripheral : PeripheralInterface {

  companion object {
    private const val TAG = "V2Peripheral"
  }

  override fun prepareReadRequestData(protocolVersion: Int): ByteArray {
    return V2ReadRequestPayload(
      v = protocolVersion,
      id = AppPreferences.userId ?: "ERROR",
      o = "DW",
      peripheral = PeripheralDevice(Build.MODEL, "SELF")
    ).getPayload()
  }

  override fun processWriteRequestDataReceived(
    dataReceived: ByteArray,
    centralAddress: String
  ): ConnectionRecord? {
    try {
      val dataWritten =
        V2WriteRequestPayload.fromPayload(
          dataReceived
        )

      return ConnectionRecord(
        version = dataWritten.v,
        msg = dataWritten.id,
        org = dataWritten.o,
        peripheral = PeripheralDevice(Build.MODEL, "SELF"),
        central = CentralDevice(dataWritten.mc, centralAddress),
        rssi = dataWritten.rs,
        txPower = null
      )
    } catch (e: Throwable) {
      Log.e(TAG, "Failed to deserialize write payload ${e.message}")
    }
    return null
  }
}
