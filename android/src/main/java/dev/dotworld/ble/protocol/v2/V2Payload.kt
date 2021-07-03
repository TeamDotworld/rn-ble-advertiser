package dev.dotworld.ble.protocol.v2

import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.dotworld.ble.streetpass.CentralDevice
import dev.dotworld.ble.streetpass.PeripheralDevice
import kotlinx.parcelize.Parcelize
import java.util.*

//acting as peripheral
data class V2ReadRequestPayload(
  val v: Int,
  val id: String,
  val o: String,
  val peripheral: PeripheralDevice,
) {
  val mp = peripheral.modelP
}
