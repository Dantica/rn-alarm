package com.rnalarm

import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableType
import com.facebook.react.bridge.WritableMap
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

object RnAlarmUtils {
  /**
   * Determines the next time the alarm should ring, in milliseconds.
   * @param turnOffForToday
   * Whether the next alarm time should skip today.
   */
  fun calculateNextAlarmTime(alarm: RnAlarm, turnOffForToday: Boolean = false): Long {
    // Set the calendar for the alarm time today.
    val alarmCalendar = Calendar.getInstance()
    alarmCalendar.set(Calendar.HOUR_OF_DAY, alarm.hour)
    alarmCalendar.set(Calendar.MINUTE, alarm.minute)
    alarmCalendar.set(Calendar.SECOND, alarm.second)
    alarmCalendar.set(Calendar.MILLISECOND, 0)

    if (alarmCalendar.timeInMillis < Calendar.getInstance().timeInMillis) {
      // If the alarm has already passed, skip today.
      alarmCalendar.add(Calendar.DATE, 1)
    } else if (turnOffForToday) {
      // If the alarm is upcoming, but turnOffForToday today is set, then still skip today.
      alarmCalendar.add(Calendar.DATE, 1)
    }

    // Search for the next valid day, if repeat is on.
    if (!alarm.repeatOnDays.isNullOrEmpty()) {
      // Get the alarm's current day of the week (1 for Monday, 7 for Sunday). Must adjust as by
      // default Android Studio makes Sunday 1 and Saturday 7.
      var alarmDayOfWeek = (alarmCalendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1

      val maxCount = 7
      var count = 0
      while (alarmDayOfWeek.toString() !in alarm.repeatOnDays!! && count < maxCount) {
        alarmCalendar.add(Calendar.DATE, 1)
        alarmDayOfWeek = (alarmDayOfWeek % 7) + 1
        count++
      }
    }

    return alarmCalendar.timeInMillis
  }

  fun calculateNextSnoozedAlarmTime(alarm: RnAlarm): Long {
    val currentCalendar = Calendar.getInstance()

    // Set the calendar for the next alarm.
    val alarmCalendar = Calendar.getInstance()
    alarmCalendar.set(Calendar.HOUR_OF_DAY, alarm.hour)
    alarmCalendar.set(Calendar.MINUTE, alarm.minute)
    alarmCalendar.set(Calendar.SECOND, alarm.second)
    alarmCalendar.set(Calendar.MILLISECOND, 0)
    // Add on extra snooze time.
    alarmCalendar.add(Calendar.MILLISECOND, alarm.snoozeCount * alarm.snoozeTime)

    // This shouldn't happen.
    if (alarmCalendar.timeInMillis < currentCalendar.timeInMillis) {
      Log.d("rn-alarm-debug", "Snoozing alarm, but snoozed alarm time is in the past.")
    }

    return alarmCalendar.timeInMillis
  }

  /**
   * Convert a ReadableMap alarm received from React Native into RnAlarm format.
   */
  fun getRnAlarmFromMap(alarmConfig: ReadableMap, promise: Promise? = null): RnAlarm? {
    try {
      // Calculate offsets.
      val timeZone: TimeZone = TimeZone.getDefault()
      val calendar: Calendar = Calendar.getInstance(timeZone)
      val timezoneOffset: Int = timeZone.getOffset(calendar.getTimeInMillis())
      val daylightSavingsOffset: Int = timeZone.getOffset(calendar.getTimeInMillis())

      return RnAlarm(
        id = alarmConfig.getInt("id"),
        hour = alarmConfig.getInt("hour"),
        minute = alarmConfig.getInt("minute"),
        second = alarmConfig.getInt("second"),
        enabled = alarmConfig.getBoolean("enabled"),
        name = alarmConfig.getString("name"),
        repeatOnDays = alarmConfig.getString("repeatOnDays"),
        soundPath = alarmConfig.getString("soundPath"),
        soundDuration = if (alarmConfig.getDynamic("soundDuration").type == ReadableType.Number) alarmConfig.getInt(
          "soundDuration"
        ) else null,
        soundVolume = alarmConfig.getDouble("soundVolume").toFloat(),
        launchApp = alarmConfig.getBoolean("launchApp"),
        militaryTime = alarmConfig.getBoolean("militaryTime"),
        snoozeTime = alarmConfig.getInt("snoozeTime"),
        maxAutoSnoozeCount = alarmConfig.getInt("maxAutoSnoozeCount"),
        snoozeCount = alarmConfig.getInt("snoozeCount"),
        showNotif = alarmConfig.getBoolean("showNotif"),
        notifTitle = alarmConfig.getString("notifTitle")!!,
        notifMsg = alarmConfig.getString("notifMsg")!!,
        notifSnoozeBtn = alarmConfig.getBoolean("notifSnoozeBtn"),
        notifSnoozeBtnTxt = alarmConfig.getString("notifSnoozeBtnTxt")!!,
        notifTurnOffBtn = alarmConfig.getBoolean("notifTurnOffBtn"),
        notifTurnOffBtnTxt = alarmConfig.getString("notifTurnOffBtnTxt")!!,
        showReminderNotif = alarmConfig.getBoolean("showReminderNotif"),
        reminderSoundPath = alarmConfig.getString("reminderSoundPath"),
        reminderSoundVolume = alarmConfig.getDouble("reminderSoundVolume").toFloat(),
        reminderTimeBefore = alarmConfig.getInt("reminderTimeBefore"),
        reminderNotifTitle = alarmConfig.getString("reminderNotifTitle")!!,
        reminderNotifMsg = alarmConfig.getString("reminderNotifMsg")!!,
        reminderNotifTurnOffBtnTxt = alarmConfig.getString("reminderNotifTurnOffBtnTxt")!!,
        showSnoozeNotif = alarmConfig.getBoolean("showSnoozeNotif"),
        snoozeNotifTitle = alarmConfig.getString("snoozeNotifTitle")!!,
        snoozeNotifMsg = alarmConfig.getString("snoozeNotifMsg")!!,
        snoozeNotifTurnOffBtnTxt = alarmConfig.getString("snoozeNotifTurnOffBtnTxt")!!,
        showMissedNotif = alarmConfig.getBoolean("showMissedNotif"),
        missedNotifTitle = alarmConfig.getString("missedNotifTitle")!!,
        missedNotifMsg = alarmConfig.getString("missedNotifMsg")!!,
        adjustWithTimezone = alarmConfig.getBoolean("adjustWithTimezone"),
        adjustWithDaylightSavings = alarmConfig.getBoolean("adjustWithDaylightSavings"),
        timezoneOffset = timezoneOffset,
        daylightSavingsOffset = daylightSavingsOffset,
        extraConfigJson = alarmConfig.getString("extraConfigJson")
      )
    } catch (e: Exception) {
      promise?.reject(Error("Error parsing alarm configuration. " + e.message))
      Log.d("rn-alarm-debug", "Error parsing alarm configuration. " + e.message)
      Log.d("rn-alarm-debug", e.stackTraceToString())
      return null
    }
  }

  /**
   * Convert a RnAlarm into a ReadableMap that can be sent to React Native.
   */
  fun getMapFromRnAlarm(alarm: RnAlarm): WritableMap {
    return Arguments.createMap().apply {
      putInt("id", alarm.id)
      putInt("hour", alarm.hour)
      putInt("minute", alarm.minute)
      putInt("second", alarm.second)
      putBoolean("enabled", alarm.enabled)
      putString("name", alarm.name)
      putString("repeatOnDays", alarm.repeatOnDays)
      putString("soundPath", alarm.soundPath)
      if (alarm.soundDuration != null) putInt("soundDuration", alarm.soundDuration!!)
      else putString("soundDuration", null)
      putDouble("soundVolume", alarm.soundVolume.toDouble())
      putBoolean("launchApp", alarm.launchApp)
      putInt("snoozeTime", alarm.snoozeTime)
      putBoolean("militaryTime", alarm.militaryTime)
      putInt("maxAutoSnoozeCount", alarm.maxAutoSnoozeCount)
      putInt("snoozeCount", alarm.snoozeCount)
      putBoolean("showNotif", alarm.showNotif)
      putString("notifTitle", alarm.notifTitle)
      putString("notifMsg", alarm.notifMsg)
      putBoolean("notifSnoozeBtn", alarm.notifSnoozeBtn)
      putString("notifSnoozeBtnTxt", alarm.notifSnoozeBtnTxt)
      putBoolean("notifTurnOffBtn", alarm.notifTurnOffBtn)
      putString("notifTurnOffBtnTxt", alarm.notifTurnOffBtnTxt)
      putBoolean("showReminderNotif", alarm.showReminderNotif)
      putDouble("reminderSoundVolume", alarm.reminderSoundVolume.toDouble())
      putInt("reminderTimeBefore", alarm.reminderTimeBefore)
      putString("reminderNotifTitle", alarm.reminderNotifTitle)
      putString("reminderNotifMsg", alarm.reminderNotifMsg)
      putString("reminderNotifTurnOffBtnTxt", alarm.reminderNotifTurnOffBtnTxt)
      putBoolean("showSnoozeNotif", alarm.showSnoozeNotif)
      putString("snoozeNotifTitle", alarm.snoozeNotifTitle)
      putString("snoozeNotifMsg", alarm.snoozeNotifMsg)
      putString("snoozeNotifTurnOffBtnTxt", alarm.snoozeNotifTurnOffBtnTxt)
      putBoolean("showMissedNotif", alarm.showMissedNotif)
      putString("missedNotifTitle", alarm.missedNotifTitle)
      putString("missedNotifDescription", alarm.missedNotifMsg)
      putBoolean("adjustWithTimezone", alarm.adjustWithTimezone)
      putBoolean("adjustWithDaylightSavings", alarm.adjustWithDaylightSavings)
      putString("extraConfigJson", alarm.extraConfigJson)
    }
  }

  /**
   * Replaces the occurrence of `$time` in a string with the current time.
   */
  fun formatStringWithTime(inputString: String, alarm: RnAlarm): String {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, alarm.hour)
    calendar.set(Calendar.MINUTE, alarm.minute)
    calendar.set(Calendar.SECOND, alarm.second)
    calendar.set(Calendar.MILLISECOND, 0)
    calendar.add(Calendar.MILLISECOND, alarm.snoozeCount * alarm.snoozeTime)

    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    val second = calendar.get(Calendar.SECOND)

    val formattedTime = if (alarm.militaryTime) {
      if (second == 0)
        String.format("%d:%02d", hour, minute)
      else
        String.format("%d:%02d:%02d", hour, minute, second)
    } else {
      val amPm = if (hour < 12) "am" else "pm"
      val formattedHours = if (hour % 12 == 0) 12 else hour % 12
      if (second == 0)
        String.format("%d:%02d %s", formattedHours, minute, amPm)
      else
        String.format("%d:%02d:%02d %s", formattedHours, minute, second, amPm)
    }

    return inputString.replace("\$time", formattedTime)
  }
}
