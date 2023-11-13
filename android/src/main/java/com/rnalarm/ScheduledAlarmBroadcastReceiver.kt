package com.rnalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScheduledAlarmBroadcastReceiver : BroadcastReceiver() {
  companion object {
    const val EXTRA_ALARM_ID = "rn_alarm.extra.ALARM_ID"
    const val EXTRA_SNOOZE_COUNTER = "rn_alarm.extra.SNOOZE_COUNTER"
    const val ACTION_ALARM_REMINDER = "rn_alarm.action.ALARM_REMINDER"
    const val ACTION_PLAY_ALARM = "rn_alarm.action.PLAY_ALARM"
    const val ACTION_SNOOZE_ALARM = "rn_alarm.action.SNOOZE_ALARM"
    const val ACTION_TURN_OFF_ALARM = "rn_alarm.action.TURN_OFF_ALARM"
    const val ACTION_TURN_OFF_ALARM_FOR_TODAY = "rn_alarm.action.TURN_OFF_ALARM_FOR_TODAY"
  }

  override fun onReceive(
    context: Context?,
    intent: Intent?,
  ) {
    if (context == null || intent == null) return
    val alarmID = intent.getIntExtra(EXTRA_ALARM_ID, -1)
    val snoozeCounter = intent.getIntExtra(EXTRA_SNOOZE_COUNTER, 0)

    CoroutineScope(Dispatchers.IO).launch {
      val alarm = RnAlarmDatastore(context).get(alarmID)

      if (alarm == null) {
        Log.d("rn-alarm-debug", "Alarm $alarmID could not be retrieved.")
      } else {
        when (intent.action) {
          ACTION_ALARM_REMINDER -> {
            RnAlarmNotificationManager(context).showReminderNotification(alarm)
            RnAlarmPlayer.playNotificationSound(context, alarm)
            RnAlarmScheduler(context).scheduleAlarm(alarm)
          }

          ACTION_TURN_OFF_ALARM_FOR_TODAY -> {
            RnAlarmPlayer.stop(dontReschedule = true)
            RnAlarmScheduler(context).cancelAlarm(alarmID)
            RnAlarmNotificationManager(context).removeNotification(alarm.id)
            RnAlarmScheduler(context).scheduleAlarm(
              alarm,
              isReminder = alarm.showReminderNotif,
              turnOffForToday = true
            )
          }

          ACTION_PLAY_ALARM -> {
            if (alarm.showNotif) {
              RnAlarmNotificationManager(context).showNotification(alarm, snoozeCounter)
            }
            if (alarm.launchApp) {
              val launchIntent =
                Intent(context.packageManager.getLaunchIntentForPackage(context.packageName))
              if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
              }
            }

            // Play the sound.
            RnAlarmPlayer.playAlarm(
              context,
              alarm,
              onAlarmUserStop = {
                RnAlarmNotificationManager(context).removeNotification(alarm.id)
                RnAlarmScheduler(context).scheduleAlarm(
                  alarm,
                  isReminder = alarm.showReminderNotif
                )
              },
              onAlarmUserSnooze = {
                RnAlarmNotificationManager(context).removeNotification(alarm.id)
                RnAlarmScheduler(context).scheduleAlarm(alarm, snoozeCounter = snoozeCounter + 1)
              },
              onAlarmTimeout = {
                if (alarm.autoSnooze && snoozeCounter < alarm.maxAutoSnoozeCounter) {
                  RnAlarmScheduler(context).scheduleAlarm(alarm, snoozeCounter = snoozeCounter + 1)
                } else {
                  RnAlarmScheduler(context).scheduleAlarm(
                    alarm,
                    isReminder = alarm.showReminderNotif
                  )
                }
                if (alarm.showMissedNotif) {
                  RnAlarmNotificationManager(context).showMissedNotification(alarm, snoozeCounter)
                }
              })
          }

          ACTION_SNOOZE_ALARM -> {
            RnAlarmNotificationManager(context).removeNotification(alarmID)
            RnAlarmPlayer.stop(userSnoozed = true)
          }

          ACTION_TURN_OFF_ALARM -> {
            RnAlarmNotificationManager(context).removeNotification(alarmID)
            RnAlarmPlayer.stop(userStopped = true)
          }
        }
      }
    }
  }
}
