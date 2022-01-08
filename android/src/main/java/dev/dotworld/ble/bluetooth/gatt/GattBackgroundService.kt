package dev.dotworld.ble.bluetooth.gatt

import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
import dev.dotworld.ble.AppPreferences
import dev.dotworld.ble.BuildConfig
import dev.dotworld.ble.Utils
import io.sentry.Sentry
import java.util.*

class GattBackgroundService : Service() {

	/* Bluetooth API */
	private lateinit var bluetoothManager: BluetoothManager
	private var bluetoothGattServer: BluetoothGattServer? = null

	override fun onBind(intent: Intent): IBinder? {
		return null
	}

	override fun onCreate() {
		super.onCreate()
		Log.d(TAG, "onCreate: Creating Gatt Background Service")

		bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
		val bluetoothAdapter = bluetoothManager.adapter
		// We can't continue without proper Bluetooth support
		try {
			if (bluetoothAdapter != null) {
				if (!checkBluetoothSupport(bluetoothAdapter)) {
					Log.w(TAG, "onCreate: Bluetooth adapter not supported")
					stopSelf()
				}

				// Register for system Bluetooth events
				val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
				registerReceiver(bluetoothReceiver, filter)
				if (!bluetoothAdapter.isEnabled) {
					Log.d(TAG, "Bluetooth is currently disabled...enabling")
					bluetoothAdapter.enable()
				} else {
					Log.d(TAG, "Bluetooth enabled...starting services")
					startAdvertising()
					startServer()
				}
			} else {
				Log.d(TAG, "onCreate: No Bluetooth adapter detected")
			}
		} catch (e: Exception) {
			Sentry.captureException(e)
			e.printStackTrace()
		}
	}

	/**
	 * Listens for Bluetooth adapter events to enable/disable
	 * advertising and server functionality.
	 */
	private val bluetoothReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context, intent: Intent) {
			when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)) {
				BluetoothAdapter.STATE_ON -> {
					startAdvertising()
					startServer()
				}
				BluetoothAdapter.STATE_OFF -> {
					stopServer()
					stopAdvertising()
				}
			}
		}
	}

	/**
	 * Callback to receive information about the advertisement process.
	 */
	private val advertiseCallback = object : AdvertiseCallback() {
		override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
			Log.i(TAG, "LE Advertise Started.")
		}

		override fun onStartFailure(errorCode: Int) {
			Log.w(TAG, "LE Advertise Failed: $errorCode")
		}
	}


	/**
	 * Callback to handle incoming requests to the GATT server.
	 * All read/write requests for characteristics and descriptors are handled here.
	 */
	private val gattServerCallback = object : BluetoothGattServerCallback() {

		override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				Log.i(TAG, "BluetoothDevice CONNECTED: $device")
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				Log.i(TAG, "BluetoothDevice DISCONNECTED: $device")
			}
		}

		override fun onCharacteristicReadRequest(
			device: BluetoothDevice, requestId: Int, offset: Int,
			characteristic: BluetoothGattCharacteristic
		) {
			try {
				when (characteristic.uuid) {
					UUID.fromString(BuildConfig.V1_CHARACTERISTIC_ID) -> {
						Log.i(TAG, "onCharacteristicReadRequest: Reading data")
						val base = AppPreferences.userId?.toByteArray() ?: "na".toByteArray()
						val value = base.copyOfRange(offset, base.size)

						bluetoothGattServer?.sendResponse(
							device,
							requestId,
							BluetoothGatt.GATT_SUCCESS,
							0,
							value
						)
					}
					else -> {
						// Invalid characteristic
						Log.w(TAG, "Invalid Characteristic Read: " + characteristic.uuid)
						bluetoothGattServer?.sendResponse(
							device,
							requestId,
							BluetoothGatt.GATT_FAILURE,
							0,
							null
						)
					}
				}
			} catch (e: Exception) {
				Sentry.captureException(e)
				e.printStackTrace()
			}
		}
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		val notification = Utils.createNotification(this);
		startForeground(1, notification)
		return START_NOT_STICKY
	}

	/**
	 * Verify the level of Bluetooth support provided by the hardware.
	 * @param bluetoothAdapter System [BluetoothAdapter].
	 * @return true if Bluetooth is properly supported, false otherwise.
	 */
	private fun checkBluetoothSupport(bluetoothAdapter: BluetoothAdapter?): Boolean {

		if (bluetoothAdapter == null) {
			Log.w(TAG, "Bluetooth is not supported")
			return false
		}

		if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Log.w(TAG, "Bluetooth LE is not supported")
			return false
		}

		return true
	}

	/**
	 * Begin advertising over Bluetooth that this device is connectable
	 * and supports the Current Time Service.
	 */
	private fun startAdvertising() {
		val bluetoothLeAdvertiser: BluetoothLeAdvertiser? =
			bluetoothManager.adapter.bluetoothLeAdvertiser

		bluetoothLeAdvertiser?.let {
			val settings = AdvertiseSettings.Builder()
				.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
				.setConnectable(true)
				.setTimeout(0)
				.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
				.build()

			val data = AdvertiseData.Builder()
				.setIncludeDeviceName(false)
				.setIncludeTxPowerLevel(false)
				.addServiceUuid(
					ParcelUuid(UUID.fromString(BuildConfig.SERVICE_ID)),
				)
				.build()

			it.startAdvertising(settings, data, advertiseCallback)
		} ?: Log.w(TAG, "Failed to create advertiser")
	}

	/**
	 * Stop Bluetooth advertisements.
	 */
	private fun stopAdvertising() {
		val bluetoothLeAdvertiser: BluetoothLeAdvertiser? =
			bluetoothManager.adapter.bluetoothLeAdvertiser
		bluetoothLeAdvertiser?.let {
			it.stopAdvertising(advertiseCallback)
		} ?: Log.w(TAG, "Failed to create advertiser")
	}

	/**
	 * Initialize the GATT server instance with the services/characteristics
	 * from the Time Profile.
	 */
	private fun startServer() {
		bluetoothGattServer = bluetoothManager.openGattServer(this, gattServerCallback)
		bluetoothGattServer?.addService(createCustomService())
	}

	/**
	 * Shut down the GATT server.
	 */
	private fun stopServer() {
		bluetoothGattServer?.close()
	}

	private fun createCustomService(): BluetoothGattService {
		val gattService = BluetoothGattService(
			UUID.fromString(BuildConfig.SERVICE_ID),
			BluetoothGattService.SERVICE_TYPE_PRIMARY
		)

		val characteristicV2 = BluetoothGattCharacteristic(
			UUID.fromString(BuildConfig.V1_CHARACTERISTIC_ID),
			BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE,
			BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
		)

		gattService.addCharacteristic(characteristicV2)

		return gattService
	}

	override fun onDestroy() {
		super.onDestroy()

		val bluetoothAdapter = bluetoothManager.adapter
		if (bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
			stopServer()
			stopAdvertising()
		}
		unregisterReceiver(bluetoothReceiver)
	}

	companion object {
		private const val TAG = "GattBackgroundService"
		private const val CHANNEL_ID = BuildConfig.SERVICE_FOREGROUND_CHANNEL_ID
		private const val CHANNEL_SERVICE = BuildConfig.SERVICE_FOREGROUND_CHANNEL_NAME

	}
}
