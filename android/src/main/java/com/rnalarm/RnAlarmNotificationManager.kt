package com.rnalarm

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class RnAlarmNotificationManager(private val context: Context) {
  fun showNotification(alarm: RnAlarm, snoozeCounter: Int = 0) {
    val notificationBuilder =
      NotificationCompat.Builder(context, RnAlarmPermissions.Companion.NOTIFICATION_CHANNEL.ID)
        .setContentTitle(RnAlarmUtils.formatStringWithTime(alarm.notifTitle, alarm, snoozeCounter))
        .setContentText(
          RnAlarmUtils.formatStringWithTime(
            alarm.notifDescription,
            alarm,
            snoozeCounter
          )
        )
        .setAutoCancel(true).setPriority(Notification.PRIORITY_MAX)

    // Set up intent to return to activity if notification is touched.
    val launchIntent = Intent(context.packageManager.getLaunchIntentForPackage(context.packageName))
    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val contentPendingIntent = PendingIntent.getActivity(
      context,
      alarm.id,
      launchIntent,
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    notificationBuilder.setContentIntent(contentPendingIntent)

    // Add snooze button, if required.
    if (alarm.notifShowSnooze) {
      val snoozeIntent = Intent(context, ScheduledAlarmBroadcastReceiver::class.java)
        .setAction(ScheduledAlarmBroadcastReceiver.ACTION_SNOOZE_ALARM)
        .putExtra(ScheduledAlarmBroadcastReceiver.EXTRA_ALARM_ID, alarm.id)

      val snoozePendingIntent = PendingIntent.getBroadcast(
        context,
        alarm.id,
        snoozeIntent,
        PendingIntent.FLAG_IMMUTABLE
      )
      notificationBuilder.addAction(0, alarm.notifSnoozeText, snoozePendingIntent)
    }

    // Add turn off for today button, if required.
    if (alarm.notifShowTurnOff) {
      val turnOffIntent = Intent(context, ScheduledAlarmBroadcastReceiver::class.java)
        .setAction(ScheduledAlarmBroadcastReceiver.ACTION_TURN_OFF_ALARM)
        .putExtra(ScheduledAlarmBroadcastReceiver.EXTRA_ALARM_ID, alarm.id)
      val turnOffPendingIntent = PendingIntent.getBroadcast(
        context,
        alarm.id,
        turnOffIntent,
        PendingIntent.FLAG_IMMUTABLE
      )
      notificationBuilder.addAction(
        0,
        alarm.notifTurnOffText,
        turnOffPendingIntent
      )
    }

    // Set icon. TODO: allow user to supply custom icon
    val packageManager = context.packageManager
    val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
    notificationBuilder.setSmallIcon(applicationInfo.icon)

    // Create a notification manager and display the notification.
    val notificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(alarm.id, notificationBuilder.build())
  }

  /**
   * Show reminder notification.
   */
  fun showReminderNotification(alarm: RnAlarm) {
    val notificationBuilder =
      NotificationCompat.Builder(context, RnAlarmPermissions.Companion.NOTIFICATION_CHANNEL.ID)
        .setContentTitle(RnAlarmUtils.formatStringWithTime(alarm.reminderNotifTitle, alarm))
        .setContentText(RnAlarmUtils.formatStringWithTime(alarm.reminderNotifDescription, alarm))
        .setPriority(Notification.PRIORITY_MAX)

    // Set up intent to return to activity if notification is touched.
    val launchIntent = Intent(context.packageManager.getLaunchIntentForPackage(context.packageName))
    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val contentPendingIntent = PendingIntent.getActivity(
      context,
      alarm.id,
      launchIntent,
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    notificationBuilder.setContentIntent(contentPendingIntent)

    // Add turn off for today button.
    val turnOffForTodayIntent = Intent(context, ScheduledAlarmBroadcastReceiver::class.java)
      .setAction(ScheduledAlarmBroadcastReceiver.ACTION_TURN_OFF_ALARM_FOR_TODAY)
      .putExtra(ScheduledAlarmBroadcastReceiver.EXTRA_ALARM_ID, alarm.id)
    val turnOffForTodayPendingIntent = PendingIntent.getBroadcast(
      context,
      alarm.id,
      turnOffForTodayIntent,
      PendingIntent.FLAG_IMMUTABLE
    )
    notificationBuilder.addAction(0, alarm.reminderNotifTurnOffText, turnOffForTodayPendingIntent)

    // Set icon. TODO: allow user to supply custom icon
    val packageManager = context.packageManager
    val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
    notificationBuilder.setSmallIcon(applicationInfo.icon)

    // Create a notification manager and display the notification.
    val notificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(alarm.id, notificationBuilder.build())
  }

  /**
   * Show missed alarm notification.
   */
  fun showMissedNotification(alarm: RnAlarm, snoozeCounter: Int = 0) {
    val notificationBuilder =
      NotificationCompat.Builder(context, RnAlarmPermissions.Companion.NOTIFICATION_CHANNEL.ID)
        .setContentTitle(
          RnAlarmUtils.formatStringWithTime(
            alarm.missedNotifTitle,
            alarm,
            snoozeCounter
          )
        )
        .setContentText(
          RnAlarmUtils.formatStringWithTime(
            alarm.missedNotifDescription,
            alarm,
            snoozeCounter
          )
        )
        .setAutoCancel(true).setPriority(Notification.PRIORITY_MAX)

    // Set up intent to return to activity if notification is touched.
    val launchIntent = Intent(context.packageManager.getLaunchIntentForPackage(context.packageName))
    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val contentPendingIntent = PendingIntent.getActivity(
      context,
      alarm.id,
      launchIntent,
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    notificationBuilder.setContentIntent(contentPendingIntent)

    // Set icon. TODO: allow user to supply custom icon
    val packageManager = context.packageManager
    val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
    notificationBuilder.setSmallIcon(applicationInfo.icon)

    // Create a notification manager and display the notification.
    val notificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(alarm.id, notificationBuilder.build())
  }

  /**
   * Remove a notification.
   */
  fun removeNotification(alarmID: Int) {
    val notificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(alarmID)
  }
}
