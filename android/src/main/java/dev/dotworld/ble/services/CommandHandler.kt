package dev.dotworld.sample.bletracker.services

import android.os.Handler
import android.os.Message
import dev.dotworld.ble.services.BluetoothMonitoringService
import java.lang.ref.WeakReference


class CommandHandler(private val service: WeakReference<BluetoothMonitoringService>) : Handler() {
    override fun handleMessage(msg: Message) {
        msg.let {
            val cmd = msg.what
            service.get()?.runService(BluetoothMonitoringService.Command.findByValue(cmd))
        }
    }

    fun sendCommandMsg(cmd: BluetoothMonitoringService.Command, delay: Long) {
        val msg = Message.obtain(this, cmd.index)
        sendMessageDelayed(msg, delay)
    }

    fun sendCommandMsg(cmd: BluetoothMonitoringService.Command) {
        val msg = obtainMessage(cmd.index)
        msg.arg1 = cmd.index
        sendMessage(msg)
    }

    fun startBluetoothMonitoringService() {
        sendCommandMsg(BluetoothMonitoringService.Command.ACTION_START)
    }

    fun scheduleNextAdvertise(timeInMillis: Long) {
        cancelNextAdvertise()
        sendCommandMsg(BluetoothMonitoringService.Command.ACTION_ADVERTISE, timeInMillis)
    }

    fun cancelNextAdvertise() {
        removeMessages(BluetoothMonitoringService.Command.ACTION_ADVERTISE.index)
    }

    fun hasAdvertiseScheduled(): Boolean {
        return hasMessages(BluetoothMonitoringService.Command.ACTION_ADVERTISE.index)
    }
}
