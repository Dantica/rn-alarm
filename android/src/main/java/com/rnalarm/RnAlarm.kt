package com.rnalarm

/**
 * Alarm properties. Using `$time` in titles/descriptions will g
 * @property id
 * Unique alarm ID. This will override an existing alarm with the same ID.
 * @property hour
 * The hour for which the alarm is set, in 24-hour time.
 * @property minute
 * The minute for which the alarm is set.
 * @property second
 * The second for which the alarm is set.
 * @property enabled
 * If true, will schedule the alarm.
 * @property name
 * Name given to the alarm.
 * @property repeatOnDays
 * String containing the days on which the alarm should repeat, e.g. "12345" for weekdays, "67" for
 * weekends. (If given an empty string or null, will only play once and then become disabled.)
 * @property soundPath
 * Path to the sound file, e.g. 'alarm_sound.mp3' in the resource 'raw' folder would be
 * '/raw/alarm_sound' where '.mp3' is omitted. If left null, will play the notification sound.
 * @property soundDuration
 * Duration the sound should play, in milliseconds. If null, will play for the duration of the
 * sound. If longer than sound duration, will loop until the duration has elapsed.
 * @property soundVolume
 * Sound volume between 0 and 100.
 * @property launchApp
 * Whether the app should be launched when the alarm goes off - requires launch app permission,
 * which is usually automatically granted.
 * @property militaryTime
 * Whether to show 24 hour time when using `$time` inside a notification title/description.
 * @property snoozeTime
 * Time in milliseconds the alarm should snooze by. Snoozing the alarm can happen in different ways,
 * such as if the user presses snooze on the notification (and notifShowSnooze is enabled), or if
 * soundDuration elapses without the user turning the alarm off (and autoSnooze is enabled).
 * @property autoSnooze
 * Whether the alarm should automatically snooze when soundDuration elapses without the user turning
 * off the alarm.
 * @property maxAutoSnoozeCounter
 * The maximum number of times an alarm will auto snooze.
 * @property showNotif
 * Whether to show a notification when the alarm goes off.
 * @property notifTitle
 * Notification title text. `$time` will show current time, e.g. "Alarm at `$time`".
 * @property notifDescription
 * Notification description text. `$time` will show current time, e.g. "Alarm at `$time`".
 * @property notifShowSnooze
 * Whether to show the snooze button in the notification.
 * @property notifSnoozeText
 * Text for the notification snooze button.
 * @property notifShowTurnOff
 * Whether to show the 'turn off for today' button in the notification.
 * @property notifTurnOffText
 * Text for the notification 'turn off for today' button.
 * @property showReminderNotif
 * Whether to show a reminder notification alerting that an alarm is upcoming. Gives the users the
 * ability to turn the alarm off for the day.
 * @property reminderVolume
 * Reminder sound volume between 0 and 100.
 * @property reminderNotifTimeBefore
 * How long before the alarm to show the reminder notification, in milliseconds.
 * @property reminderNotifTitle
 * Text for the reminder notification title. `$time` will show current time, e.g. "Alarm at `$time`".
 * @property reminderNotifDescription
 * Text for the reminder notification description. `$time` will show current time, e.g. "Alarm at
 * `$time`".
 * @property reminderNotifTurnOffText
 * Text for the reminder notification turn off button, which will turn the alarm off for the day.
 * @property showMissedNotif
 * Whether to show a notification when the alarm hasn't been turned off in time.
 * @property missedNotifTitle
 * Text for the missed notification title. `$time` will show current time, e.g. "Alarm at `$time`".
 * @property missedNotifDescription
 * Text for the missed notification description. `$time` will show current time, e.g. "Alarm at
 * `$time`".
 * @property adjustWithTimezone
 * Whether alarm time should change along with the timezone. If true, an alarm set for 1 pm at
 * UTC-05:00 will automatically be converted to 6 pm if the device's timezone changes to UTC-00:00.
 * @property adjustWithDaylightSavings
 * Whether alarm time should change along with the daylight savings. If set to true, an alarm set
 * for 1 pm will automatically be converted to 2 pm once daylight savings begins, and back to 1 pm
 * when it ends.
 * @property timezoneOffset
 * The device's timezone offset at the time of the alarm's creation. E.g. UTC-04:00 represents an
 * offset of -14400000 (-4 hours).
 * @property daylightSavingsOffset
 * The device's daylight savings offset at the time of the alarm's creation. Either 0 (no daylight
 * savings) or 3600000 (1 hour).
 * @property extraConfigJson
 * An extra string given to the user in case they which to store extra data as JSON.
 */
data class RnAlarm(
  var id: Int,
  var hour: Int,
  var minute: Int,
  var second: Int,
  var enabled: Boolean,
  var name: String?,
  var repeatOnDays: String?,
  var soundPath: String?,
  var soundDuration: Int?,
  var soundVolume: Float,
  var launchApp: Boolean,
  var militaryTime: Boolean,
  var snoozeTime: Int,
  var autoSnooze: Boolean,
  var maxAutoSnoozeCounter: Int,
  var showNotif: Boolean,
  var notifTitle: String,
  var notifDescription: String,
  var notifShowSnooze: Boolean,
  var notifSnoozeText: String,
  var notifShowTurnOff: Boolean,
  var notifTurnOffText: String,
  var showReminderNotif: Boolean,
  var reminderVolume: Float,
  var reminderNotifTimeBefore: Int,
  var reminderNotifTitle: String,
  var reminderNotifDescription: String,
  var reminderNotifTurnOffText: String,
  var showMissedNotif: Boolean,
  var missedNotifTitle: String,
  var missedNotifDescription: String,
  var extraConfigJson: String?,
  var adjustWithTimezone: Boolean,
  var adjustWithDaylightSavings: Boolean,
  var timezoneOffset: Int,
  var daylightSavingsOffset: Int,
)
