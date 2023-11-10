package com.rnalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.facebook.react.bridge.Promise
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class RnAlarmManager(private val context: Context) {
  /**
   * Schedule an alarm. Also saves the alarm to storage.
   */
  suspend fun scheduleAlarm(
    alarm: RnAlarm, promise: Promise? = null
  ) {
    try {
      // Save to storage.
      RnAlarmDatastore(context).save(alarm)

      val intent =
        Intent(context, ScheduledAlarmBroadcastReceiver::class.java).putExtra("alarmID", alarm.id)

      val pIntent = PendingIntent.getBroadcast(
        context,
        alarm.id,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

      // Find next available time for alarm to ring.
      val timeInMillis = RnAlarmUtils.calculateNextAlarmTime(alarm)

      // Set the alarm.
      val info = AlarmManager.AlarmClockInfo(timeInMillis, pIntent)
      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
      alarmManager.setAlarmClock(info, pIntent)
      Log.d(
        "rn-alarm-debug", SimpleDateFormat("dd/MM/yy HH:mm:ss").format(
          timeInMillis
        ) + " (Scheduled alarm)"
      )


      promise?.resolve(timeInMillis.toDouble())
    } catch (e: Exception) {
      Log.d("rn-alarm-debug", "Failed to schedule alarm with ID: ${alarm.id}.")
      Log.d("rn-alarm-debug", e.stackTraceToString())
      promise?.reject(Error("Failed to schedule alarm with ID: ${alarm.id}. " + e.message))
    }
  }

  /**
   * Cancels an alarm. Also removes the alarm from storage.
   */
  suspend fun cancelAlarm(alarmID: Int, promise: Promise) {
    try {
      val pIntent = PendingIntent.getBroadcast(
        context,
        alarmID,
        Intent(context, ScheduledAlarmBroadcastReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
      alarmManager.cancel(pIntent)
      RnAlarmDatastore(context).delete(alarmID)

      promise.resolve(alarmID)
    } catch (e: Exception) {
      promise.reject(Error("Failed to cancel alarm with ID $alarmID. " + e.message))
    }
  }
}

