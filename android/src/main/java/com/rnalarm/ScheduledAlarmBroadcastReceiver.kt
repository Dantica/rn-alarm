package com.rnalarm

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class ScheduledAlarmBroadcastReceiver : BroadcastReceiver() {
  override fun onReceive(
    context: Context?,
    intent: Intent?,
  ) {
    if (context == null || intent == null) return
    val alarmID = intent.getIntExtra("alarmID", -1)

    CoroutineScope(Dispatchers.IO).launch {
      val alarm = RnAlarmDatastore(context).get(alarmID)

      if (alarm == null) {
        Log.d("rn-alarm-debug", "Alarm $alarmID could not be retrieved.")
      } else {
        if (alarm.showNotification) {
          showNotification(context, alarm)
        }
        if (alarm.launchApp) {
          val launchIntent =
            Intent(context.packageManager.getLaunchIntentForPackage(context.packageName))
          if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
          }
          Log.d("rn-alarm-debug", "Attempting to launch app.")
        }

        // Play the sound.
        RnAlarmPlayer.playAlarm(context, alarmID, alarm.customSound)
        Log.d(
          "rn-alarm-debug", SimpleDateFormat("dd/MM/yy HH:mm:ss").format(Date()) + " Playing alarm."
        )

        // Reschedule alarm if required.
        if (RnAlarmUtils.alarmShouldRepeat(alarm)) {
          RnAlarmManager(context).scheduleAlarm(alarm)
        }
      }
    }
  }

  private fun showNotification(context: Context, alarm: RnAlarm) {
    // Set up intent to return to activity if notification is touched
    val launchIntent = Intent(context.packageManager.getLaunchIntentForPackage(context.packageName))
    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val contentPendingIntent = PendingIntent.getActivity(
      context,
      alarm.id,
      launchIntent,
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    // Create a notification using NotificationCompat.Builder
    val notificationBuilder =
      NotificationCompat.Builder(context, RnAlarmPermissions.Companion.NOTIFICATION_CHANNEL.ID)
        .setContentTitle(alarm.notificationConfig.title)
        .setContentText(alarm.notificationConfig.description)
        .setSmallIcon(context.applicationInfo.icon).setContentIntent(contentPendingIntent)
        .setAutoCancel(true).setPriority(Notification.PRIORITY_MAX)

    // Create a notification manager and display the notification.
    val notificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(alarm.id, notificationBuilder.build())
  }
}
