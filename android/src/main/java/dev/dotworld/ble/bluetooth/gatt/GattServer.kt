package dev.dotworld.ble.bluetooth.gatt

import android.bluetooth.*
import android.content.Context
import android.util.Log
import dev.dotworld.ble.Utils
import dev.dotworld.ble.protocol.BlueTrace
import java.util.*
import kotlin.properties.Delegates


class GattServer constructor(val context: Context, serviceUUIDString: String) {

  private val TAG = "GattServer"
  private var bluetoothManager: BluetoothManager by Delegates.notNull()

  private var serviceUUID: UUID by Delegates.notNull()
  var bluetoothGattServer: BluetoothGattServer? = null

  init {
    bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    this.serviceUUID = UUID.fromString(serviceUUIDString)
  }

  private val gattServerCallback = object : BluetoothGattServerCallback() {

    //this should be a table
    //in order to handle many connections from different mac addresses
    val readPayloadMap: MutableMap<String, ByteArray> = HashMap()

    override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
      when (newState) {
        BluetoothProfile.STATE_CONNECTED -> {
          Log.i(TAG, "${device?.address} Connected to local GATT server")
        }

        BluetoothProfile.STATE_DISCONNECTED -> {
          Log.i(TAG, "${device?.address} Disconnected from local GATT server.")
          readPayloadMap.remove(device?.address)
        }

        else -> {
          Log.i(TAG, "Connection status: $newState - ${device?.address}")
        }
      }
    }

    // acting as peripheral
    override fun onCharacteristicReadRequest(
      device: BluetoothDevice?,
      requestId: Int,
      offset: Int,
      characteristic: BluetoothGattCharacteristic?
    ) {

      if (device == null) {
        Log.w(TAG, "No device")
      }

      device?.let {
        Log.i(TAG, "onCharacteristicReadRequest from ${device.address}")
        if (BlueTrace.supportsCharUUID(characteristic?.uuid)) {
          characteristic?.uuid?.let { charUUID ->
            val bluetraceImplementation = BlueTrace.getImplementation(charUUID)
            val base = readPayloadMap.getOrPut(device.address) {
              bluetraceImplementation.peripheral.prepareReadRequestData(
                bluetraceImplementation.versionInt
              )
            }
            val value = base.copyOfRange(offset, base.size)
            bluetoothGattServer?.sendResponse(
              device,
              requestId,
              BluetoothGatt.GATT_SUCCESS,
              0,
              value
            )
          }
        } else {
          Log.i(TAG, "unsupported characteristic UUID from ${device.address}")
          bluetoothGattServer?.sendResponse(
            device, requestId,
            BluetoothGatt.GATT_FAILURE, 0, null
          )
        }
      }
    }

    override fun onCharacteristicWriteRequest(
      device: BluetoothDevice?,
      requestId: Int,
      characteristic: BluetoothGattCharacteristic,
      preparedWrite: Boolean,
      responseNeeded: Boolean,
      offset: Int,
      value: ByteArray?
    ) {
      super.onCharacteristicWriteRequest(
        device,
        requestId,
        characteristic,
        preparedWrite,
        responseNeeded,
        offset,
        value
      )
      Log.i(TAG, "onCharacteristicWriteRequest: ")
      value?.let {
        Log.i(TAG, "onCharacteristicWriteRequest: ${String(it)}")
        Utils.notifyUser(context)
      }
    }

    override fun onExecuteWrite(
      device: BluetoothDevice,
      requestId: Int,
      execute: Boolean
    ) {
      super.onExecuteWrite(device, requestId, execute)
      Log.i(TAG, "onExecuteWrite: ")
    }
  }

  fun startServer(): Boolean {
    bluetoothGattServer = bluetoothManager.openGattServer(context, gattServerCallback)
    bluetoothGattServer?.let {
      it.clearServices()
      return true
    }
    return false
  }

  fun addService(service: GattService) {
    bluetoothGattServer?.addService(service.gattService)
  }

  fun stop() {
    try {
      bluetoothGattServer?.clearServices()
      bluetoothGattServer?.close()
    } catch (e: Throwable) {
      Log.e(TAG, "GATT server can't be closed elegantly ${e.localizedMessage}")
    }
  }

}
