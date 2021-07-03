package dev.dotworld.ble.protocol.v2

import android.os.Build
import android.util.Log
import com.google.gson.Gson
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
  override fun prepareReadRequestData(protocolVersion: Int): ByteArray {
    return Gson().toJson(V2ReadRequestPayload(
      v = protocolVersion,
      id = AppPreferences.userId ?: "ERROR",
      o = "DW",
      peripheral = PeripheralDevice(Build.MODEL, "SELF")
    )).toByteArray()
  }
}
