package com.rnalarm

import android.content.Context
import android.content.Intent

class RnAlarmController(private val context: Context, private val alarm: RnAlarm) {
  /**
   * Play the alarm reminder.
   */
  fun playAlarmReminder() {
    if (!RnAlarmPlayer.isPlaying()) {
      RnAlarmPlayer.playReminderSound(context, alarm)
      RnAlarmScheduler(context).scheduleAlarm(alarm)
      if (alarm.showReminderNotif) {
        RnAlarmNotificationManager(context).showReminderNotification(alarm)
      }
    }
  }

  /**
   * Play the alarm.
   */
  fun playAlarm() {
    if (!RnAlarmPlayer.isPlaying()) {
      // Play the sound, and set up actions when the sound track ends.
      RnAlarmPlayer.playAlarm(
        context,
        alarm,
        onAlarmUserStop = { turnOffAlarm() },
        onAlarmUserSnooze = { snoozeAlarm() },
        onAlarmTimeout = {
          if (alarm.snoozeCount < alarm.maxAutoSnoozeCount) snoozeAlarm()
          else {
            turnOffAlarm()
            if (alarm.showMissedNotif)
              RnAlarmNotificationManager(context).showMissedNotification(alarm)
          }
        })

      // Show notification, if required.
      if (alarm.showNotif) {
        RnAlarmNotificationManager(context).showAlarmNotification(alarm)
      }

      // Launch app, if required.
      if (alarm.launchApp) {
        val launchIntent =
          Intent(context.packageManager.getLaunchIntentForPackage(context.packageName))
        if (launchIntent != null) {
          launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          context.startActivity(launchIntent)
        }
      }
    }
  }

  fun snoozeAlarm(userSnoozed: Boolean = false) {
    if (RnAlarmPlayer.isPlaying()) {
      RnAlarmPlayer.stop(userSnoozed = userSnoozed)
    } else {
      alarm.snoozeCount++
      RnAlarmDatastore(context).saveInBackground(alarm)
      RnAlarmScheduler(context).scheduleAlarm(alarm)
      if (alarm.showSnoozeNotif) {
        RnAlarmNotificationManager(context).showSnoozedNotification(alarm)
      }
    }
  }

  fun turnOffAlarm(userStopped: Boolean = false) {
    if (RnAlarmPlayer.isPlaying()) {
      RnAlarmPlayer.stop(userStopped = userStopped)
    } else {
      alarm.snoozeCount = 0
      RnAlarmDatastore(context).saveInBackground(alarm)
      RnAlarmScheduler(context).cancelAlarm(alarm.id)
      RnAlarmNotificationManager(context).removeNotification(alarm.id)
      RnAlarmScheduler(context).scheduleAlarm(
        alarm,
        isReminder = alarm.showReminderNotif,
        turnOffForToday = true
      )
    }
  }
}
