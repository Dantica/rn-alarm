package com.rnalarm

/**
 * Alarm properties.
 * @property alarmID
 * The unique identifier for the alarm.
 * @property hour
 * The hour at which the alarm is set, in 24 hour time.
 * @property minute
 * The minute at which the alarm is set.
 * @property second
 * The second at which the alarm is set. Defaults to 0.
 * @property name
 * Name given to the alarm.
 * @property customSound
 * Path to the custom sound to play. E.g. "/raw/alarm_sound.mp3". If left null, will play the
 * notification sound by default.
 * @property soundDuration
 * Duration the sound should play, in milliseconds. If left null, will play for the duration of the
 * sound and stop. If the value is longer than the sound duration, will loop.
 * @property repeat
 * Whether the alarm should repeat. Defaults to true.
 * @property repeatOnDays
 * String containing the days on which the alarm should repeat. Monday is 1, Sunday is 7. E.g.
 * "6,7" for weekends (commas optional), or "12345" for weekdays. Defaults to every day.
 * @property timezoneOffset
 * The device's timezone offset at the time of the alarm's creation. E.g. UTC-04:00 represents an
 * offset of -14400000 (-4 hours).
 * @property daylightSavingsOffset
 * The device's daylight savings offset at the time of the alarm's creation. Either 0 (no daylight
 * savings) or 3600000 (1 hour).
 * @property adjustWithTimezone
 * Whether the alarm time should change along with the timezone. E.g. If set to true, an
 * alarm set for 1pm at UTC-05:00 will automatically be converted to 6pm if the device's timezone
 * changes to UTC-00:00. Defaults to false.
 * @property adjustWithDaylightSavings
 * Whether the alarm time should change along with the daylight savings. E.g. If set to true, an
 * alarm set for 1pm will automatically be converted to 2pm. Defaults to false.
 * @property launchApp
 * Whether the app should be launched when the alarm goes off. Defaults to false.
 * @property showNotification
 * Whether a notification should be displayed. Defaults to true.
 * @property notificationConfig
 * The notification config. Only used if launchApp is set to false. Defaults to null, which will use
 * default NotificationConfig values.
 */
data class RnAlarm(
  var id: Int,
  var hour: Int,
  var minute: Int,
  var second: Int = 0,
  var name: String? = null,
  var customSound: String? = null,
  var soundDuration: Int? = null,
  var repeat: Boolean = true,
  var repeatOnDays: String = "1234567",
  var timezoneOffset: Int,
  var daylightSavingsOffset: Int,
  var adjustWithTimezone: Boolean = false,
  var adjustWithDaylightSavings: Boolean = false,
  var launchApp: Boolean = false,
  var showNotification: Boolean = true,
  var notificationConfig: NotificationConfig = NotificationConfig(),
)

/**
 * Alarm notification configuration.
 * @property title
 * Title text.
 * @property description
 * Description text.
 * @property showSnoozeButton
 * Whether to show the snooze button. Defaults to false.
 * @property snoozeButtonText
 * Snooze button text. Defaults to "Snooze".
 * @property snoozeTime
 * How long to snooze for in milliseconds. Defaults to 300000 (5 minutes).
 * @property showTurnOffButton
 * Whether to show the turn off for today button. Defaults to false.
 * @property turnOffButtonText
 * Turn off for today button text. Defaults to "Turn Off For Today".
 */
data class NotificationConfig(
  var title: String = "Alarm",
  var description: String = "",
  var showSnoozeButton: Boolean = false,
  var snoozeButtonText: String = "Snooze",
  var snoozeTime: Int = 300000,
  var showTurnOffButton: Boolean = false,
  var turnOffButtonText: String = "Turn Off For Today",
)
