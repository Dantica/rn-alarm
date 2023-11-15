package com.rnalarm

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat.Builder

class RnAlarmNotificationManager(private val context: Context) {
  /**
   * Show the notification that displays as the alarm rings.
   */
  fun showAlarmNotification(alarm: RnAlarm) {
    val notificationBuilder =
      createNotificationBuilder(
        alarm,
        RnAlarmUtils.formatStringWithTime(alarm.notifTitle, alarm),
        RnAlarmUtils.formatStringWithTime(alarm.notifMsg, alarm),
      )

    if (alarm.notifSnoozeBtn) {
      addButton(
        alarm.id,
        btnAction = ScheduledAlarmBroadcastReceiver.ACTION_SNOOZE_ALARM,
        btnTxt = alarm.notifSnoozeBtnTxt,
        notificationBuilder
      )
    }

    if (alarm.notifTurnOffBtn) {
      addButton(
        alarm.id,
        btnAction = ScheduledAlarmBroadcastReceiver.ACTION_TURN_OFF_ALARM,
        btnTxt = alarm.notifTurnOffBtnTxt,
        notificationBuilder
      )
    }

    notify(alarm.id, notificationBuilder)
  }

  /**
   * Show the notification that plays before an upcoming alarm.
   */
  fun showReminderNotification(alarm: RnAlarm) {
    val notificationBuilder =
      createNotificationBuilder(
        alarm,
        RnAlarmUtils.formatStringWithTime(alarm.reminderNotifTitle, alarm),
        RnAlarmUtils.formatStringWithTime(alarm.reminderNotifMsg, alarm),
      )

    addButton(
      alarm.id,
      btnAction = ScheduledAlarmBroadcastReceiver.ACTION_TURN_OFF_ALARM,
      btnTxt = alarm.reminderNotifTurnOffBtnTxt,
      notificationBuilder
    )

    notify(alarm.id, notificationBuilder)
  }

  /**
   * Show the notification when an alarm is snoozed.
   */
  fun showSnoozedNotification(alarm: RnAlarm) {
    // Increment snoozeCount so that $time in the title and msg will display the next snooze time.
    val notificationBuilder =
      createNotificationBuilder(
        alarm,
        RnAlarmUtils.formatStringWithTime(alarm.snoozeNotifTitle, alarm),
        RnAlarmUtils.formatStringWithTime(alarm.snoozeNotifMsg, alarm),
      )

    addButton(
      alarm.id,
      btnAction = ScheduledAlarmBroadcastReceiver.ACTION_TURN_OFF_ALARM,
      btnTxt = alarm.snoozeNotifTurnOffBtnTxt,
      notificationBuilder
    )

    notify(alarm.id, notificationBuilder)
  }


  /**
   * Show the notification when an alarm is missed.
   */
  fun showMissedNotification(alarm: RnAlarm) {
    val notificationBuilder =
      createNotificationBuilder(
        alarm,
        RnAlarmUtils.formatStringWithTime(alarm.missedNotifTitle, alarm),
        RnAlarmUtils.formatStringWithTime(alarm.missedNotifMsg, alarm),
      )
    notificationBuilder.setAutoCancel(true)

    notify(alarm.id, notificationBuilder)
  }

  /**
   * Helper function that creates the base for the notification.
   */
  private fun createNotificationBuilder(
    alarm: RnAlarm,
    title: String,
    msg: String
  ): Builder {
    val notificationBuilder =
      Builder(context, RnAlarmPermissions.Companion.NOTIFICATION_CHANNEL.ID)
        .setContentTitle(RnAlarmUtils.formatStringWithTime(title, alarm))
        .setContentText(RnAlarmUtils.formatStringWithTime(msg, alarm))
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

    // Set icon. TODO: allow user to supply custom icon
    val packageManager = context.packageManager
    val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
    notificationBuilder.setSmallIcon(applicationInfo.icon)

    return notificationBuilder
  }

  /**
   * Helper function that adds a button to the notification builder.
   */
  private fun addButton(
    alarmID: Int,
    btnAction: String,
    btnTxt: String,
    notificationBuilder: Builder
  ) {
    val intent = Intent(context, ScheduledAlarmBroadcastReceiver::class.java)
      .setAction(btnAction)
      .putExtra(ScheduledAlarmBroadcastReceiver.EXTRA_ALARM_ID, alarmID)

    val pIntent = PendingIntent.getBroadcast(
      context,
      alarmID,
      intent,
      PendingIntent.FLAG_IMMUTABLE
    )
    notificationBuilder.addAction(
      0,
      btnTxt,
      pIntent
    )
  }

  /**
   * Helper function to display the notification.
   */
  private fun notify(alarmID: Int, notificationBuilder: Builder) {
    // Create a notification manager and display the notification.
    val notificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(alarmID)
    notificationManager.notify(alarmID, notificationBuilder.build())
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

