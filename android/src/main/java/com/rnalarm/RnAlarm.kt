package com.rnalarm

/**
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
 * String containing the days on which the alarm should repeat, e.g. "12345"
 * for weekdays, "67" for weekends. (If given null or an empty string, will
 * only play once and then become disabled.)
 * @property soundPath
 * Path to the sound file, e.g. "alarm.mp3" in the resource "raw" folder
 * would be "/raw/alarm" where ".mp3" is omitted. If given an empty string or
 * null, will play the default notification sound.
 * @property soundDuration
 * Duration the sound should play, in milliseconds. If null, will play for
 * the duration of the sound. If longer than sound duration, will loop until
 * the duration has elapsed.
 * @property soundVolume
 * Sound volume between 0 and 100.
 * @property launchApp
 * Whether the app should be launched when the alarm goes off - requires launch
 * app permission, which is usually automatically granted.
 * @property militaryTime
 * Whether to show 24 hour time when using $time inside a notification title
 * or description.
 * @property snoozeTime
 * Alarm snooze duration, in milliseconds. Snoozing the alarm can happen in
 * different ways, such as if the user presses snooze on a notification, or
 * if autoSnooze is enabled and soundDuration elapses without the alarm being
 * turned off. Note: the snooze time will be added onto the initial alarm
 * time, meaning the value must be longer than the duration of the alarm.
 * Otherwise, the snoozed alarm will play immediately. E.g. an alarm set for
 * 7:00 with a snooze time of 300000 (5 minutes) and soundDuration of 120000
 * (2 minutes) will stop ringing at 7:02, but the snoozed alarm will still
 * always ring at 7:05.
 * @property maxAutoSnoozeCount
 * The maximum number of times an alarm will auto snooze before being missed.
 * @property snoozeCount
 * The number of times the alarm has been snoozed.
 * @property showNotif
 * Whether to show a notification when the alarm goes off.
 * @property notifTitle
 * Notification title text. Using `$time` will show the alarm's time, e.g.
 * "Alarm at $time".
 * @property notifMsg
 * Notification description text. Using `$time` will show the alarm's time,
 * e.g. "Alarm at $time".
 * @property notifSnoozeBtn
 * Whether to show a snooze button in the alarm notification.
 * @property notifSnoozeBtnTxt
 * Text for notifSnoozeBtn.
 * @property notifTurnOffBtn
 * Whether to show the turn off button in the notification.
 * @property notifTurnOffBtnTxt
 * Text for notifTurnOffBtn.
 * @property showReminderNotif
 * Whether to show a reminder notification alerting that an alarm is upcoming
 * Also gives the users the ability to turn the alarm off for the day.
 * @property reminderTimeBefore
 * How long before the alarm a reminder notification should be shown, in
 * milliseconds.
 * @property reminderSoundPath
 * Path to a custom sound file to play during the reminder notification, e.g.
 * "reminder.mp3" in the resource "raw" folder would be "/raw/reminder" where
 * ".mp3" is omitted. If left null, will play the device's standard
 * notification sound.
 * @property reminderSoundVolume
 * Reminder sound volume between 0 and 100.
 * @property reminderNotifTitle
 * Reminder notification title text. Using `$time` will show the alarm's
 * time, e.g. "Alarm at `$time`".
 * @property reminderNotifMsg
 * Reminder notification description text. Using `$time` will show alarm's
 * time, e.g. "Alarm at `$time`".
 * @property reminderNotifTurnOffBtnTxt
 * Text for the reminder notification's turn off button, which will turn the
 * alarm off for the day.
 * @property showSnoozeNotif
 * Whether to show a notification once an alarm has been snoozed, which can
 * happen either if the user presses snooze, or if the alarm runs out and is
 * auto snoozed.
 * @property snoozeNotifTitle
 * Snooze notification title text. Using `$time` will show the alarm's
 * snoozed time, e.g. "Alarm at `$time`".
 * @property snoozeNotifMsg
 * Snooze notification description text. Using `$time` will show the alarm's
 * snoozed time.
 * @property snoozeNotifTurnOffBtnTxt
 * Text for the snooze notification turn off button, which will turn the
 * alarm off for the day (default: "Turn Off").
 * @property showMissedNotif
 * Whether to show a notification when the alarm hasn't been turned off in
 * time (and auto snooze has run out).
 * @property missedNotifTitle
 * Missed notification title text. `$time` will show the alarm's time, e.g.
 * "Missed alarm at $time".
 * @property missedNotifMsg
 * Missed notification description text. `$time` will show the alarm's time,
 * e.g. "Missed alarm at $time".
 * @property adjustWithTimezone
 * Whether alarm time should change along with the timezone. If true, an
 * alarm set for 1pm at UTC-05:00 will automatically be converted to 6pm if
 * the device's timezone changes to UTC+00:00.
 * @property adjustWithDaylightSavings
 * Whether alarm time should change along with the daylight savings. If true,
 * an alarm set for 1pm will automatically be converted to 2pm once daylight
 * savings begins, and back to 1pm when it ends.
 * @property timezoneOffset
 * The device's timezone offset at the time of the alarm's creation, e.g.
 * UTC-04:00 represents an offset of -14400000 (-4 hours).
 * @property daylightSavingsOffset
 * The device's daylight savings offset at the time of the alarm's creation.
 * Either 0 (no daylight savings) or 3600000 (1 hour).
 * @property extraConfigJson
 * An extra string given to the user in case they which to store extra data.
 * Can be used to store an object as JSON.
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
  var maxAutoSnoozeCount: Int,
  var snoozeCount: Int,
  var showNotif: Boolean,
  var notifTitle: String,
  var notifMsg: String,
  var notifSnoozeBtn: Boolean,
  var notifSnoozeBtnTxt: String,
  var notifTurnOffBtn: Boolean,
  var notifTurnOffBtnTxt: String,
  var showReminderNotif: Boolean,
  var reminderTimeBefore: Int,
  var reminderSoundPath: String?,
  var reminderSoundVolume: Float,
  var reminderNotifTitle: String,
  var reminderNotifMsg: String,
  var reminderNotifTurnOffBtnTxt: String,
  var showSnoozeNotif: Boolean,
  var snoozeNotifTitle: String,
  var snoozeNotifMsg: String,
  var snoozeNotifTurnOffBtnTxt: String,
  var showMissedNotif: Boolean,
  var missedNotifTitle: String,
  var missedNotifMsg: String,
  var adjustWithTimezone: Boolean,
  var adjustWithDaylightSavings: Boolean,
  var timezoneOffset: Int,
  var daylightSavingsOffset: Int,
  var extraConfigJson: String?,
)
