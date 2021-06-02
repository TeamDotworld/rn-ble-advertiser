package dev.dotworld.ble.notifications

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.reactnativebleadvertiser.R

class NotificationTemplates {

  companion object {
    fun getRunningNotification(context: Context, channel: String): Notification {
      val builder = NotificationCompat.Builder(context, channel)
        .setContentTitle(context.getText(R.string.service_ok_title))
        .setContentText(context.getText(R.string.service_ok_body))
        .setOngoing(true)
        .setPriority(Notification.PRIORITY_LOW)
        .setSmallIcon(R.drawable.ic_notification_service)
        .setTicker(context.getText(R.string.service_ok_body))
        .setStyle(
          NotificationCompat.BigTextStyle().bigText(context.getText(R.string.service_ok_body))
        )
        .setWhen(System.currentTimeMillis())
        .setSound(null)
        .setVibrate(null)
        .setColor(ContextCompat.getColor(context, R.color.notification_tint))

      return builder.build()
    }

  }
}
