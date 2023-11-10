package com.rnalarm

import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import java.util.Calendar
import java.util.TimeZone

object RnAlarmUtils {
  /**
   * Determines the next time the alarm should ring, in milliseconds.
   */
  fun calculateNextAlarmTime(alarm: RnAlarm): Long {
    val currentCalendar = Calendar.getInstance()

    // Set the calendar for the next alarm.
    val alarmCalendar = Calendar.getInstance()
    alarmCalendar.set(Calendar.HOUR_OF_DAY, alarm.hour)
    alarmCalendar.set(Calendar.MINUTE, alarm.minute)
    alarmCalendar.set(Calendar.SECOND, alarm.second)
    alarmCalendar.set(Calendar.MILLISECOND, 0)

    // Get the current day of the week (1 for Monday, 7 for Sunday). Value must be adjust, as by
    // default Android Studio makes Sunday 1 and Saturday 7.
    var currentDayOfWeekCounter = (currentCalendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1

    // Check if the alarm time has already passed for the current day.
    if (alarmCalendar.timeInMillis < currentCalendar.timeInMillis) {
      // If the alarm time has passed for today, move to the next day.
      alarmCalendar.add(Calendar.DATE, 1)
      currentDayOfWeekCounter = (currentDayOfWeekCounter % 7) + 1 // Wrap around if needed.
    }

    // Search for the next valid day if repeat is set to true.
    if (RnAlarmUtils.alarmShouldRepeat(alarm)) {
      val maxCounter = 7
      var counter = 0
      while (currentDayOfWeekCounter.toString() !in alarm.repeatOnDays && counter < maxCounter) {
        alarmCalendar.add(Calendar.DATE, 1)
        currentDayOfWeekCounter = (currentDayOfWeekCounter % 7) + 1
        counter++
      }
    }

    return alarmCalendar.timeInMillis
  }

  /**
   * Convert a ReadableMap alarm received from React Native into RnAlarm format.
   */
  fun getRnAlarmFromMap(alarmConfig: ReadableMap, promise: Promise? = null): RnAlarm? {
    if (!alarmConfig.hasKey("id") || !alarmConfig.hasKey("hour") || !alarmConfig.hasKey("minute")) {
      Log.d("rn-alarm-debug", "React Native app did not provide alarm id, hour, and minute.")
      promise?.reject(Error("Did not provide id, hour, and minute."))
      return null
    }

    // Retrieve mandatory fields.
    val id = alarmConfig.getInt("id")
    val hour = alarmConfig.getInt("hour")
    val minute = alarmConfig.getInt("minute")

    // Calculate offsets.
    val timeZone: TimeZone = TimeZone.getDefault()
    val calendar: Calendar = Calendar.getInstance(timeZone)
    val timezoneOffset: Int = timeZone.getOffset(calendar.getTimeInMillis())
    val daylightSavingsOffset: Int = timeZone.getOffset(calendar.getTimeInMillis())

    // Create basic alarm.
    val alarm = RnAlarm(
      id = id,
      hour = hour,
      minute = minute,
      timezoneOffset = timezoneOffset,
      daylightSavingsOffset = daylightSavingsOffset,
    )

    // Check for all other optional parameters.
    val iterator = alarmConfig.keySetIterator()
    while (iterator.hasNextKey()) {
      when (val key = iterator.nextKey()) {
        "id" -> {} // Already saved above
        "hour" -> {}
        "minute" -> {}
        "second" -> alarm.second = alarmConfig.getInt(key)
        "name" -> alarm.name = alarmConfig.getString(key)
        "customSound" -> alarm.customSound = alarmConfig.getString(key)
        "repeat" -> alarm.repeat = alarmConfig.getBoolean(key)
        "repeatOnDays" -> alarm.repeatOnDays = alarmConfig.getString(key)!!
        "adjustWithTimezone" -> alarm.adjustWithTimezone = alarmConfig.getBoolean(key)
        "adjustWithDaylightSavings" -> alarm.adjustWithDaylightSavings = alarmConfig.getBoolean(key)
        "launchApp" -> alarm.launchApp = alarmConfig.getBoolean(key)
        "showNotification" -> alarm.showNotification = alarmConfig.getBoolean(key)
        "notificationConfig" -> {} // Handled below
        else -> {
          promise?.reject(Error("Invalid alarm config parameter provided: $key"))
          return null
        }
      }
    }

    if (alarmConfig.hasKey("notificationConfig")) {
      val notifConfig = NotificationConfig()
      val notifMap = alarmConfig.getMap("notificationConfig")!!
      val iterator2 = notifMap.keySetIterator()
      while (iterator2.hasNextKey()) {
        when (val key2 = iterator2.nextKey()) {
          "title" -> notifConfig.title = notifMap.getString(key2)!!
          "description" -> notifConfig.description = notifMap.getString(key2)!!
          "showSnoozeButton" -> notifConfig.showSnoozeButton = notifMap.getBoolean(key2)
          "snoozeButtonText" -> notifConfig.snoozeButtonText = notifMap.getString(key2)!!
          "snoozeTime" -> notifConfig.snoozeTime = notifMap.getInt(key2)
          "showTurnOffButton" -> notifConfig.showTurnOffButton = notifMap.getBoolean(key2)
          "turnOffButtonText" -> notifConfig.turnOffButtonText = notifMap.getString(key2)!!
          else -> {
            promise?.reject(Error("Invalid alarm notification config parameter provided: $key2"))
            return null
          }
        }
      }
      alarm.notificationConfig = notifConfig
    }

    return alarm
  }

  /**
   * Convert a RnAlarm into a ReadableMap that can be sent to React Native.
   */
  fun getMapFromRnAlarm(alarm: RnAlarm): WritableMap {
    val map = Arguments.createMap()

    map.putInt("id", alarm.id)
    map.putInt("hour", alarm.hour)
    map.putInt("minute", alarm.minute)
    map.putInt("second", alarm.second)
    if (alarm.name != null) map.putString("customSound", alarm.name)
    if (alarm.customSound != null) map.putString("customSound", alarm.customSound)
    if (alarm.soundDuration != null) map.putInt("customSound", alarm.soundDuration!!)
    map.putBoolean("repeat", alarm.repeat)
    map.putString("repeatOnDays", alarm.repeatOnDays)
    // Purposefully omit `timezoneOffset` and `daylightSavingsOffset` as that is only used for
    // calculations on the native side.
    map.putBoolean("adjustWithTimezone", alarm.adjustWithTimezone)
    map.putBoolean("adjustWithDaylightSavings", alarm.adjustWithDaylightSavings)
    map.putBoolean("launchApp", alarm.launchApp)
    map.putBoolean("showNotification", alarm.showNotification)

    val notifConfigMap = Arguments.createMap()
    val notifConfig = alarm.notificationConfig
    notifConfigMap.putString("title", notifConfig.title)
    notifConfigMap.putString("description", notifConfig.description)
    notifConfigMap.putBoolean("showSnoozeButton", notifConfig.showSnoozeButton)
    notifConfigMap.putString("snoozeButtonText", notifConfig.snoozeButtonText)
    notifConfigMap.putInt("snoozeTime", notifConfig.snoozeTime)
    notifConfigMap.putBoolean("showTurnOffButton", notifConfig.showTurnOffButton)
    notifConfigMap.putString("turnOffButtonText", notifConfig.turnOffButtonText)
    map.putMap("notificationConfig", notifConfigMap)

    return map
  }

  /**
   * Determines whether an alarm should repeat based on values given by repeat and repeatOnDays.
   */
  fun alarmShouldRepeat(alarm: RnAlarm): Boolean {
    return alarm.repeat && !alarm.repeatOnDays.isEmpty()
  }
}
