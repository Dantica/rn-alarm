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
    const val ACTION_ALARM_REMINDER = "rn_alarm.action.ALARM_REMINDER"
    const val ACTION_PLAY_ALARM = "rn_alarm.action.PLAY_ALARM"
    const val ACTION_SNOOZE_ALARM = "rn_alarm.action.SNOOZE_ALARM"
    const val ACTION_TURN_OFF_ALARM = "rn_alarm.action.TURN_OFF_ALARM"
  }

  override fun onReceive(
    context: Context?,
    intent: Intent?,
  ) {
    if (context == null || intent == null) return
    val alarmID = intent.getIntExtra(EXTRA_ALARM_ID, -1)

    CoroutineScope(Dispatchers.IO).launch {
      val alarm = RnAlarmDatastore(context).get(alarmID)

      if (alarm == null) {
        Log.d("rn-alarm-debug", "Alarm $alarmID could not be retrieved.")
      } else {
        when (intent.action) {
          ACTION_ALARM_REMINDER -> {
            RnAlarmController(context, alarm).playAlarmReminder()
          }

          ACTION_PLAY_ALARM -> {
            RnAlarmController(context, alarm).playAlarm()
          }

          ACTION_SNOOZE_ALARM -> {
            RnAlarmController(context, alarm).snoozeAlarm(userSnoozed = true)
          }

          ACTION_TURN_OFF_ALARM -> {
            RnAlarmController(context, alarm).turnOffAlarm(userStopped = true)
          }
        }
      }
    }
  }
}
