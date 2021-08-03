package dev.dotworld.ble.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import dev.dotworld.ble.BuildConfig
import dev.dotworld.ble.Utils
import dev.dotworld.ble.bluetooth.BLEAdvertiser
import dev.dotworld.ble.notifications.NotificationTemplates
import dev.dotworld.ble.streetpass.StreetPassServer
import dev.dotworld.sample.bletracker.services.CommandHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.lang.ref.WeakReference
import kotlin.coroutines.CoroutineContext


class BluetoothMonitoringService : Service(), CoroutineScope {

	private var mNotificationManager: NotificationManager? = null

	private var streetPassServer: StreetPassServer? = null
	private var advertiser: BLEAdvertiser? = null

	private lateinit var commandHandler: CommandHandler
	private lateinit var serviceUUID: String

	private val bluetoothStatusReceiver = BluetoothStatusReceiver()

	private var job = Job()
	override val coroutineContext: CoroutineContext
		get() = Dispatchers.Main + job

	override fun onBind(intent: Intent?): IBinder? {
		return null
	}

	override fun onCreate() {
		Log.i(TAG, "setup: Setting Bluetooth monitoring service")
		commandHandler = CommandHandler(WeakReference(this))
		serviceUUID = BuildConfig.SERVICE_ID

		Log.i(TAG, "onCrceate: Service UUID is $serviceUUID")
		setupNotifications()

		unregisterReceivers()
		registerReceivers()

	}

	private fun registerReceivers() {
		val bluetoothStatusReceivedFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
		registerReceiver(bluetoothStatusReceiver, bluetoothStatusReceivedFilter)

		Log.i(TAG, "Receivers registered")
	}

	private fun unregisterReceivers() {
		try {
			unregisterReceiver(bluetoothStatusReceiver)
		} catch (e: Throwable) {
			Log.w(TAG, "bluetoothStatusReceiver is not registered?")
		}
	}


	private fun setupNotifications() {
		mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			Log.i(TAG, "setupNotifications: Creating notification channel for android O")
			val name = CHANNEL_SERVICE
			val mChannel =
				NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW)
			mChannel.enableLights(false)
			mChannel.enableVibration(true)
			mChannel.vibrationPattern = longArrayOf(0L)
			mChannel.setSound(null, null)
			mChannel.setShowBadge(false)

			mNotificationManager!!.createNotificationChannel(mChannel)
		}
	}


	private fun isBluetoothEnabled(): Boolean {
		var btOn = false
		try {
			val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
				val bluetoothManager =
					getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
				bluetoothManager.adapter
			}

			bluetoothAdapter?.let {
				btOn = it.isEnabled
			}
		} catch (e: Exception) {
			Log.e(TAG, "isBluetoothEnabled: Error enabling bluetooth", e)
		}
		return btOn
	}

	private var notificationShown: NOTIFICATION_STATE? = null

	enum class NOTIFICATION_STATE {
		RUNNING,
		LACKING_THINGS
	}

	private fun notifyRunning(override: Boolean = false) {
		if (notificationShown != NOTIFICATION_STATE.RUNNING || override) {
			val notif =
				NotificationTemplates.getRunningNotification(this.applicationContext, CHANNEL_ID)
			startForeground(BuildConfig.SERVICE_FOREGROUND_NOTIFICATION_ID, notif)
			notificationShown = NOTIFICATION_STATE.LACKING_THINGS
		}
	}

	private fun notifyLackingThings(override: Boolean = false) {
		if (notificationShown != NOTIFICATION_STATE.LACKING_THINGS || override) {
			val notif =
				NotificationTemplates.lackingThingsNotification(this.applicationContext, CHANNEL_ID)
			startForeground(BuildConfig.SERVICE_FOREGROUND_NOTIFICATION_ID, notif)
			notificationShown = NOTIFICATION_STATE.LACKING_THINGS
		}
	}


	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		Log.i(TAG, "onStartCommand: Starting on command")

		Log.i(TAG, "onStartCommand: UUID $serviceUUID")

		try {
			intent?.let {
				val cmd = intent.getIntExtra(COMMAND_KEY, Command.INVALID.index)
				runService(Command.findByValue(cmd))
				Log.i(TAG, "onStartCommand: Started with intent")
				return START_STICKY
			}

			if (intent == null) {
				Log.i(TAG, "onStartCommand: Using command handler")
				commandHandler.startBluetoothMonitoringService()
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}

		return START_STICKY
	}

	fun runService(cmd: Command?) {

		if (!isBluetoothEnabled()) {
			Log.i(
				TAG,
				"Bluetooth: ${isBluetoothEnabled()}"
			)
			notifyLackingThings()
			return
		}

		notifyRunning()


		when (cmd) {
			Command.ACTION_START -> {
				Log.i(TAG, "runService: Command run initiated")
				setupService()
				Utils.scheduleNextHealthCheck(this.applicationContext, healthCheckInterval)
				setupAdvertisingCycles()
			}
			Command.ACTION_SELF_CHECK -> {
				Utils.scheduleNextHealthCheck(this.applicationContext, healthCheckInterval)
				actionHealthCheck()
			}
			Command.ACTION_ADVERTISE -> {
				scheduleAdvertisement()
				Log.i(TAG, "runService: running actionAdvertise")
				actionAdvertise()
			}
			else -> Log.i(TAG, "runService: Unknown command")
		}
	}

	private fun scheduleAdvertisement() {
		Log.i(TAG, "scheduleAdvertisement: scheduling advertise")
		if (!infiniteAdvertising) {
			Log.i(TAG, "scheduleAdvertisement: running command for next advertise schedule")
			commandHandler.scheduleNextAdvertise(advertisingDuration + advertisingGap)
		}
	}

	private fun actionHealthCheck() {
		performHealthCheck()
		Utils.scheduleRepeatingPurge(this.applicationContext, purgeInterval)
	}

	private fun performHealthCheck() {
		if (!isBluetoothEnabled()) {
			Log.i(TAG, "Bluetooth not enabled")
			notifyLackingThings(true)
			return
		}


		notifyRunning(true)
		setupService()

		if (!infiniteAdvertising) {
			if (!commandHandler.hasAdvertiseScheduled()) {
				Log.w(TAG, "Missing Advertise Schedule - rectifying")
				setupAdvertisingCycles()
				commandHandler.scheduleNextAdvertise(100)
			} else {
				Log.w(
					TAG,
					"Advertise Schedule present. Should be advertising?:  ${
						advertiser?.shouldBeAdvertising
							?: false
					}. Is Advertising?: ${advertiser?.isAdvertising ?: false}"
				)
			}
		} else {
			Log.w(TAG, "Should be operating under infinite advertise mode")
		}
	}

	private fun setupAdvertisingCycles() {
		commandHandler.scheduleNextAdvertise(0)
	}

	override fun onDestroy() {
		super.onDestroy()
		stopService()
	}

	private fun teardown() {
		streetPassServer?.tearDown()
		streetPassServer = null

		if (this::commandHandler.isInitialized) {
			commandHandler.removeCallbacksAndMessages(null)
		}
	}

	private fun stopService() {
		Log.i(TAG, "stopService: Stopping service")
		teardown()

		unregisterReceivers()

		job.cancel()
	}

	private fun setupService() {
		streetPassServer =
			streetPassServer ?: StreetPassServer(this.applicationContext, serviceUUID)
		setupAdvertiser()
	}

	private fun actionAdvertise() {
		setupAdvertiser()
		Log.i(TAG, "actionAdvertise: Running action advertise")
		if (isBluetoothEnabled()) {
			Log.i(TAG, "actionAdvertise: Start advertising")
			advertiser?.startAdvertising(advertisingDuration)
		} else {
			Log.w(TAG, "Unable to start advertising, bluetooth is off")
		}
	}

	private fun setupAdvertiser() {
		Log.i(TAG, "setupAdvertiser: Setting advertiser")
		advertiser = advertiser ?: BLEAdvertiser(serviceUUID)
	}

	enum class Command(val index: Int, val string: String) {
		INVALID(-1, "INVALID"),
		ACTION_START(0, "START"),
		ACTION_ADVERTISE(3, "ADVERTISE"),
		ACTION_SELF_CHECK(4, "SELF_CHECK"),
		ACTION_PURGE(6, "PURGE");

		companion object {
			private val types = values().associateBy { it.index }
			fun findByValue(value: Int) = types[value]
		}
	}

	inner class BluetoothStatusReceiver : BroadcastReceiver() {

		override fun onReceive(context: Context?, intent: Intent?) {
			intent?.let {
				val action = intent.action
				if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
					when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
						BluetoothAdapter.STATE_TURNING_OFF -> {
							Log.d(TAG, "BluetoothAdapter.STATE_TURNING_OFF")
							notifyLackingThings()
							teardown()
						}
						BluetoothAdapter.STATE_OFF -> {
							Log.d(TAG, "BluetoothAdapter.STATE_OFF")
						}
						BluetoothAdapter.STATE_TURNING_ON -> {
							Log.d(TAG, "BluetoothAdapter.STATE_TURNING_ON")
						}
						BluetoothAdapter.STATE_ON -> {
							Log.d(TAG, "BluetoothAdapter.STATE_ON")
							Utils.startBluetoothMonitoringService(this@BluetoothMonitoringService.applicationContext)
						}
					}
				}
			}
		}
	}

	companion object {
		private const val CHANNEL_ID = BuildConfig.SERVICE_FOREGROUND_CHANNEL_ID
		const val CHANNEL_SERVICE = BuildConfig.SERVICE_FOREGROUND_CHANNEL_NAME

		const val advertisingDuration: Long = BuildConfig.ADVERTISING_DURATION
		const val advertisingGap: Long = BuildConfig.ADVERTISING_INTERVAL
		const val healthCheckInterval: Long = BuildConfig.HEALTH_CHECK_INTERVAL
		const val purgeInterval: Long = BuildConfig.PURGE_INTERVAL

		const val PENDING_ACTIVITY = 5
		const val PENDING_HEALTH_CHECK_CODE = 9
		const val PENDING_PURGE_CODE = 12

		const val COMMAND_KEY = "${BuildConfig.LIBRARY_PACKAGE_NAME}_CMD"
		private const val TAG = "BTMService"

		const val infiniteAdvertising = false
	}

}
