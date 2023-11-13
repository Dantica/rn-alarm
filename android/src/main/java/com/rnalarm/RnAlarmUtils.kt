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
   */
  fun calculateNextAlarmTime(alarm: RnAlarm, turnOffForToday: Boolean = false): Long {
    val currentCalendar = Calendar.getInstance()
    if (turnOffForToday) currentCalendar.add(Calendar.DATE, 1)

    // Set the calendar for the next alarm.
    val alarmCalendar = Calendar.getInstance()
    alarmCalendar.set(Calendar.HOUR_OF_DAY, alarm.hour)
    alarmCalendar.set(Calendar.MINUTE, alarm.minute)
    alarmCalendar.set(Calendar.SECOND, alarm.second)
    alarmCalendar.set(Calendar.MILLISECOND, 0)
    if (turnOffForToday) alarmCalendar.add(Calendar.DATE, 1)

    // Get the current day of the week (1 for Monday, 7 for Sunday). Value must be adjusted, as by
    // default Android Studio makes Sunday 1 and Saturday 7.
    var currentDayOfWeekCounter = (currentCalendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1

    // Check if the alarm time has already passed for the current day.
    if (alarmCalendar.timeInMillis < currentCalendar.timeInMillis && !turnOffForToday) {
      // If the alarm time has passed for today, move to the next day.
      alarmCalendar.add(Calendar.DATE, 1)
      currentDayOfWeekCounter = (currentDayOfWeekCounter % 7) + 1 // Wrap around if needed.
    }

    // Search for the next valid day.
    if (!alarm.repeatOnDays.isNullOrEmpty()) {
      val maxCounter = 7
      var counter = 0
      while (currentDayOfWeekCounter.toString() !in alarm.repeatOnDays!! && counter < maxCounter) {
        alarmCalendar.add(Calendar.DATE, 1)
        currentDayOfWeekCounter = (currentDayOfWeekCounter % 7) + 1
        counter++
      }
    }

    return alarmCalendar.timeInMillis
  }

  fun calculateNextSnoozedAlarmTime(alarm: RnAlarm, snoozeCounter: Int): Long {
    val currentCalendar = Calendar.getInstance()

    // Set the calendar for the next alarm.
    val alarmCalendar = Calendar.getInstance()
    alarmCalendar.set(Calendar.HOUR_OF_DAY, alarm.hour)
    alarmCalendar.set(Calendar.MINUTE, alarm.minute)
    alarmCalendar.set(Calendar.SECOND, alarm.second)
    alarmCalendar.set(Calendar.MILLISECOND, 0)
    // Add on extra snooze time.
    alarmCalendar.add(Calendar.MILLISECOND, snoozeCounter * alarm.snoozeTime)

    // This shouldn't happen.
    if (alarmCalendar.timeInMillis < currentCalendar.timeInMillis) {
      Log.d("rn-alarm-debug", "Snoozing alarm, but snoozed alarm time is in the past.")
      Log.d(
        "rn-alarm-debug", "Current time: " +
          SimpleDateFormat("dd/MM/yy HH:mm:ss").format(currentCalendar.timeInMillis)
      )
      Log.d(
        "rn-alarm-debug", "Alarm time: " +
          SimpleDateFormat("dd/MM/yy HH:mm:ss").format(alarmCalendar.timeInMillis)
      )
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
        autoSnooze = alarmConfig.getBoolean("autoSnooze"),
        maxAutoSnoozeCounter = alarmConfig.getInt("maxAutoSnoozeCounter"),
        showNotif = alarmConfig.getBoolean("showNotif"),
        notifTitle = alarmConfig.getString("notifTitle")!!,
        notifDescription = alarmConfig.getString("notifDescription")!!,
        notifShowSnooze = alarmConfig.getBoolean("notifShowSnooze"),
        notifSnoozeText = alarmConfig.getString("notifSnoozeText")!!,
        notifShowTurnOff = alarmConfig.getBoolean("notifShowTurnOff"),
        notifTurnOffText = alarmConfig.getString("notifTurnOffText")!!,
        showReminderNotif = alarmConfig.getBoolean("showReminderNotif"),
        reminderVolume = alarmConfig.getDouble("reminderVolume").toFloat(),
        reminderNotifTimeBefore = alarmConfig.getInt("reminderNotifTimeBefore"),
        reminderNotifTitle = alarmConfig.getString("reminderNotifTitle")!!,
        reminderNotifDescription = alarmConfig.getString("reminderNotifDescription")!!,
        reminderNotifTurnOffText = alarmConfig.getString("reminderNotifTurnOffText")!!,
        showMissedNotif = alarmConfig.getBoolean("showMissedNotif"),
        missedNotifTitle = alarmConfig.getString("missedNotifTitle")!!,
        missedNotifDescription = alarmConfig.getString("missedNotifDescription")!!,
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
      putBoolean("autoSnooze", alarm.autoSnooze)
      putInt("maxAutoSnoozeCounter", alarm.maxAutoSnoozeCounter)
      putBoolean("showNotif", alarm.showNotif)
      putString("notifTitle", alarm.notifTitle)
      putString("notifDescription", alarm.notifDescription)
      putBoolean("notifShowSnooze", alarm.notifShowSnooze)
      putString("notifSnoozeText", alarm.notifSnoozeText)
      putBoolean("notifShowTurnOff", alarm.notifShowTurnOff)
      putString("notifTurnOffText", alarm.notifTurnOffText)
      putBoolean("showReminderNotif", alarm.showReminderNotif)
      putDouble("reminderVolume", alarm.reminderVolume.toDouble())
      putInt("reminderNotifTimeBefore", alarm.reminderNotifTimeBefore)
      putString("reminderNotifTitle", alarm.reminderNotifTitle)
      putString("reminderNotifDescription", alarm.reminderNotifDescription)
      putString("reminderNotifTurnOffText", alarm.reminderNotifTurnOffText)
      putBoolean("showMissedNotif", alarm.showMissedNotif)
      putString("missedNotifTitle", alarm.missedNotifTitle)
      putString("missedNotifDescription", alarm.missedNotifDescription)
      putBoolean("adjustWithTimezone", alarm.adjustWithTimezone)
      putBoolean("adjustWithDaylightSavings", alarm.adjustWithDaylightSavings)
      putString("extraConfigJson", alarm.extraConfigJson)
    }
  }

  /**
   * Replaces the occurrence of `$time` in a string with the current time.
   */
  fun formatStringWithTime(inputString: String, alarm: RnAlarm, snoozeCounter: Int = 0): String {
    // Add on snoozeTime, if required.
    var snoozeTime = snoozeCounter * alarm.snoozeTime

    var hours = alarm.hour + (snoozeTime / (1000 * 60 * 60)) % 24
    var minutes = alarm.minute + (snoozeTime / (1000 * 60)) % 60
    var seconds = alarm.second + (snoozeTime / 1000) % 60

    // Adjust minutes and hours if necessary
    if (seconds >= 60) {
      minutes += seconds / 60
      seconds %= 60
    }
    if (minutes >= 60) {
      hours += minutes / 60
      minutes %= 60
    }

    val formattedTime = if (alarm.militaryTime) {
      String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
      val amPm = if (hours < 12) "am" else "pm"
      val formattedHours = if (hours % 12 == 0) 12 else hours % 12
      String.format("%d:%02d:%02d %s", formattedHours, minutes, seconds, amPm)
    }

    return inputString.replace("\$time", formattedTime)
  }
}
