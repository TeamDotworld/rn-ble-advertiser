package dev.dotworld.ble.protocol

import dev.dotworld.ble.streetpass.ConnectionRecord

open class BlueTraceProtocol(
  val versionInt: Int,
  val peripheral: PeripheralInterface
)

interface PeripheralInterface {
  fun prepareReadRequestData(protocolVersion: Int): ByteArray

  fun processWriteRequestDataReceived(
    dataWritten: ByteArray,
    centralAddress: String
  ): ConnectionRecord?
}
