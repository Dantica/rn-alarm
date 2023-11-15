package com.rnalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

class RnAlarmScheduler(private val context: Context) {
  /**
   * Schedule an alarm.
   */
  fun scheduleAlarm(
    alarm: RnAlarm,
    isReminder: Boolean = false,
    turnOffForToday: Boolean = false,
  ) {
    val intent =
      Intent(context, ScheduledAlarmBroadcastReceiver::class.java)
        .putExtra(ScheduledAlarmBroadcastReceiver.EXTRA_ALARM_ID, alarm.id)

    intent.action =
      if (isReminder) ScheduledAlarmBroadcastReceiver.ACTION_ALARM_REMINDER
      else ScheduledAlarmBroadcastReceiver.ACTION_PLAY_ALARM

    val pIntent = PendingIntent.getBroadcast(
      context,
      alarm.id,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    // Find next available time for alarm to ring.
    var timeInMillis =
      if (alarm.snoozeCount > 0) RnAlarmUtils.calculateNextSnoozedAlarmTime(alarm)
      else RnAlarmUtils.calculateNextAlarmTime(alarm, turnOffForToday)
    if (isReminder) timeInMillis -= alarm.reminderTimeBefore

    // Set the alarm.
    val info = AlarmManager.AlarmClockInfo(timeInMillis, pIntent)
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.setAlarmClock(info, pIntent)

    Log.d(
      "rn-alarm-debug", "Scheduled alarm for ${
        SimpleDateFormat("dd/MM/yy HH:mm:ss").format(
          timeInMillis
        )
      } (isReminder: $isReminder, turnOffForToday: $turnOffForToday)"
    )
  }

  /**
   * Cancel an alarm.
   */
  fun cancelAlarm(alarmID: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Cancel both types of alarms
    listOf(
      ScheduledAlarmBroadcastReceiver.ACTION_ALARM_REMINDER,
      ScheduledAlarmBroadcastReceiver.ACTION_PLAY_ALARM
    ).forEach { action ->
      val intent =
        Intent(context, ScheduledAlarmBroadcastReceiver::class.java)
          .setAction(action)
      val pIntent = PendingIntent.getBroadcast(
        context,
        alarmID,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )
      alarmManager.cancel(pIntent)
    }
  }
}

