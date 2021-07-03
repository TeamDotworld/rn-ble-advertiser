package dev.dotworld.ble.protocol

open class BlueTraceProtocol(
  val versionInt: Int,
  val peripheral: PeripheralInterface
)

interface PeripheralInterface {
  fun prepareReadRequestData(protocolVersion: Int): ByteArray
}
