package dev.dotworld.ble.bluetooth.gatt

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import com.reactnativebleadvertiser.BuildConfig
import java.util.*
import kotlin.properties.Delegates


class GattService constructor(val context: Context, serviceUUIDString: String) {

  var gattService: BluetoothGattService by Delegates.notNull()

  init {
    gattService = BluetoothGattService(
      UUID.fromString(serviceUUIDString),
      BluetoothGattService.SERVICE_TYPE_PRIMARY
    )

    val characteristicV2 = BluetoothGattCharacteristic(
      UUID.fromString(BuildConfig.V2_CHARACTERISTIC_ID),
      BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE,
      BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
    )

    gattService.addCharacteristic(characteristicV2)
  }
}
