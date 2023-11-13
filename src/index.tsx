import { useEffect, useState } from 'react';
import { NativeEventEmitter, NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'rn-alarm' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const RnAlarm = NativeModules.RnAlarm
  ? NativeModules.RnAlarm
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

// ------------------------------------------------------------------------------
// Setting Alarms
// ------------------------------------------------------------------------------

/**
 * @param {number} config.id
 * Unique alarm ID. This will override an existing alarm with the same ID.
 * @param {number} config.hour
 * The hour for which the alarm is set, in 24-hour time.
 * @param {number} config.minute
 * The minute for which the alarm is set.
 * @param {number} config.second
 * The second for which the alarm is set (default: 0).
 * @param {boolean} config.enabled
 * If true, will schedule the alarm (default: true).
 * @param {string | null} config.name
 * Name given to the alarm (default: null).
 * @param {string | null} config.repeatOnDays
 * String containing the days on which the alarm should repeat, e.g. "12345"
 * for weekdays, "67" for weekends (default: "1234567"). (If given an empty
 * string or null, will only play once and then become disabled.)
 * @param {string | null} config.soundPath
 * Path to the sound file, e.g. 'alarm_sound.mp3' in the resource 'raw' folder
 * would be '/raw/alarm_sound' where '.mp3' is omitted (default: null). If
 * left null, will play the notification sound.
 * @param {number | null} config.soundDuration
 * Duration the sound should play, in milliseconds (default: null). If null,
 * will play for the duration of the sound. If longer than sound duration, will
 * loop until the duration has elapsed.
 * @param {number} config.soundVolume
 * Sound volume between 0 and 100 (default: 50).
 * @param {boolean} config.launchApp
 * Whether the app should be launched when the alarm goes off - requires launch
 * app permission, which is usually automatically granted (default: false).
 * @param {boolean} config.militaryTime
 * Whether to show 24 hour time when using `$time` inside a notification title
 * or description (default: true).
 * @param {number} config.snoozeTime
 * Time in milliseconds the alarm should snooze by (default: 300000). Snoozing
 * the alarm can happen in different ways, such as if the user presses snooze
 * on the notification (and notifShowSnooze is enabled), or if soundDuration
 * elapses without the user turning the alarm off (and autoSnooze is enabled).
 * @param {boolean} config.autoSnooze
 * Whether the alarm should automatically snooze when soundDuration elapses
 * without the user turning off the alarm (default: true).
 * @param {number} config.maxAutoSnoozeCounter
 * The maximum number of times an alarm will auto snooze (default: 3).
 * @param {boolean} config.showNotif
 * Whether to show a notification when the alarm goes off (default: true).
 * @param {string} config.notifTitle
 * Notification title text (default: "Alarm"). Using `$time` will show current
 * time, e.g. "Alarm at `$time`".
 * @param {string} config.notifDescription
 * Notification description text (default: "Alarm for $time"). Using `$time`
 * will show current time.
 * @param {boolean} config.notifShowSnooze
 * Whether to show the snooze button in the notification (default: true).
 * @param {string} config.notifSnoozeText
 * Text for the notification snooze button (default: "Snooze").
 * @param {boolean} config.notifShowTurnOff
 * Whether to show the 'turn off for today' button in the notification
 * (default: true).
 * @param {string} config.notifTurnOffText
 * Text for the notification 'turn off for today' button (default: "Turn Off
 * For Today").
 * @param {boolean} config.showReminderNotif
 * Whether to show a reminder notification alerting that an alarm is upcoming
 * (default: false). Gives the users the ability to turn the alarm off for the
 * day.
 * @param {number} config.reminderVolume
 * Reminder sound volume between 0 and 100 (default: config.soundVolume).
 * @param {number} config.reminderNotifTimeBefore
 * How long before the alarm to show the reminder notification, in milliseconds
 * (default: 300000).
 * @param {string} config.reminderNotifTitle
 * Text for the reminder notification title (default: 'Upcoming Alarm'). Using
 * `$time` will show current time, e.g. "Alarm at `$time`".
 * @param {string} config.reminderNotifDescription
 * Text for the reminder notification description (default: 'Alarm set for
 * $time'). Using `$time` will show the time the alarm is set to ring.
 * @param {string} config.reminderNotifTurnOffText
 * Text for the reminder notification turn off button, which will turn the
 * alarm off for the day (default: 'Turn Off For Today').
 * @param {boolean} config.showMissedNotif
 * Whether to show a notification when the alarm hasn't been turned off in time
 * (default: false).
 * @param {string} config.missedNotifTitle
 * Text for the missed notification title (default: 'Missed Alarm'). `$time`
 * will show the current time.
 * @param {string} config.missedNotifDescription
 * Text for the missed notification description (default: 'You missed your
 * alarm at $time'). `$time` will show the current time.
 * @param {boolean} config.adjustWithTimezone
 * Whether alarm time should change along with the timezone (default: false).
 * If set to true, an alarm set for 1pm at UTC-05:00 will automatically be
 * converted to 6pm if the device's timezone changes to UTC-00:00.
 * @param {boolean} config.adjustWithDaylightSavings
 * Whether alarm time should change along with the daylight savings (default:
 * false).
 * If set to true, an alarm set for 1pm will automatically be converted to 2pm
 * once daylight savings begins, and back to 1pm when it ends.
 * @param {string} config.extraConfigJson
 * An extra string given to the user in case they which to store extra data as
 * JSON (default: null).
 * @returns {Promise}
 * Promise that resolves to the alarm and all of its configured properties.
 */
export async function setAlarm(config: TAlarmConfig): Promise<TAlarm> {
  const { id, hour, minute } = config; // Mandatory fields.

  const alarm: TAlarm = {
    id,
    hour,
    minute,
    second: config.second ?? 0,
    enabled: config.enabled ?? true,
    name: config.name ?? null,
    repeatOnDays:
      config.repeatOnDays === undefined ? '1234567' : config.repeatOnDays,
    soundPath: config.soundPath ?? null,
    soundDuration: config.soundDuration ?? null,
    soundVolume: config.soundVolume ?? 50,
    launchApp: config.launchApp ?? false,
    militaryTime: config.militaryTime ?? true,
    snoozeTime: config.snoozeTime ?? 300000,
    autoSnooze: config.autoSnooze ?? true,
    maxAutoSnoozeCounter: config.maxAutoSnoozeCounter ?? 3,
    showNotif: config.showNotif ?? true,
    notifTitle: config.notifTitle ?? 'Alarm',
    notifDescription: config.notifDescription ?? 'Alarm for $time',
    notifShowSnooze: config.notifShowSnooze ?? true,
    notifSnoozeText: config.notifSnoozeText ?? 'Snooze',
    notifShowTurnOff: config.notifShowTurnOff ?? true,
    notifTurnOffText: config.notifTurnOffText ?? 'Turn Off',
    showReminderNotif: config.showReminderNotif ?? false,
    reminderVolume: config.reminderVolume ?? config.soundVolume ?? 50,
    reminderNotifTimeBefore: config.reminderNotifTimeBefore ?? 300000,
    reminderNotifTitle: config.reminderNotifTitle ?? 'Upcoming Alarm',
    reminderNotifDescription:
      config.reminderNotifDescription ?? 'Alarm set for $time',
    reminderNotifTurnOffText:
      config.reminderNotifTurnOffText ?? 'Turn Off For Today',
    showMissedNotif: config.showMissedNotif ?? false,
    missedNotifTitle: config.missedNotifTitle ?? 'Missed Alarm',
    missedNotifDescription:
      config.missedNotifDescription ?? 'You missed your alarm at $time',
    adjustWithTimezone: config.adjustWithTimezone ?? false,
    adjustWithDaylightSavings: config.adjustWithDaylightSavings ?? false,
    extraConfigJson: null,
  };
  return RnAlarm.setAlarm(alarm);
}

/**
 * Updates one or multiple alarm properties. Must supply an ID of the alarm to
 * update. If the ID does not exist, returns null.
 */
export async function updateAlarm(
  updatedAlarm: TAlarmUpdateConfig
): Promise<TAlarm | null> {
  const alarm = await getAlarm(updatedAlarm.id);
  if (alarm === null) return null;
  return RnAlarm.setAlarm({ ...alarm, ...updatedAlarm });
}

/**
 * Stops an alarm if it ringing, otherwise does nothing.
 */
export async function turnOffAlarm(alarmID: number): Promise<void> {
  return RnAlarm.turnOffAlarm(alarmID);
}

/**
 * Snooze an alarm, which will reschedule it `snoozeTime` milliseconds into the
 * future, where `snoozeTime` is an alarm property set previously.
 */
export async function snoozeAlarm(alarmID: number): Promise<void> {
  return RnAlarm.snoozeAlarm(alarmID);
}

/**
 * Turn off an alarm due to ring today, rescheduling it to the next available
 * day present in `repeatOnDays`.
 */
export async function turnOffAlarmForToday(alarmID: number): Promise<void> {
  return RnAlarm.turnOffAlarmForToday(alarmID);
}

/**
 * Permanently delete an alarm.
 */
export async function deleteAlarm(alarmID: number): Promise<null> {
  return RnAlarm.deleteAlarm(alarmID);
}

// ------------------------------------------------------------------------------
// Accessing Alarms
// ------------------------------------------------------------------------------

/**
 * Get currently playing alarm (null if one is not playing).
 */
export async function getCurrentlyPlayingAlarm(): Promise<TAlarm | null> {
  return RnAlarm.getCurrentlyPlayingAlarm();
}

/**
 * Get an alarm, or null it the ID does not exist.
 */
export async function getAlarm(alarmID: number): Promise<TAlarm | null> {
  return RnAlarm.getAlarm(alarmID);
}

/**
 * Get the unix time in milliseconds an alarm is next due to set off, or null
 * if the ID does not exist.
 */
export async function getAlarmTime(alarmID: number): Promise<number | null> {
  return RnAlarm.getAlarmTime(alarmID);
}

/**
 * Get the readable time an alarm is next due to set off, or null if the ID
 * does not exist.
 */
export async function getReadableAlarmTime(
  alarmID: number
): Promise<string | null> {
  const alarmTime = await RnAlarm.getAlarmTime(alarmID);
  if (alarmTime === null) return null;

  const date = new Date(alarmTime);
  const day = String(date.getDate()).padStart(2, '0');
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const year = String(date.getFullYear()).slice(2);
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');

  return `${day}/${month}/${year} ${hours}:${minutes}:${seconds}`;
}

/**
 * Get all alarms.
 */
export async function getAllAlarms(): Promise<TAlarm[]> {
  return RnAlarm.getAllAlarms();
}

// ------------------------------------------------------------------------------
// Permissions
// ------------------------------------------------------------------------------

/**
 * Set up notifications channel, required for Android API 26 onwards.
 */
export function setupAndroidNotifChannel() {
  RnAlarm.setupAndroidNotifChannel();
}

/**
 * Checks if alarm permission has been granted. (This is currently not a
 * permission that can be disabled, but is left here for completeness in
 * case the permission is required in the future.)
 */
export function hasAlarmPermission(): Promise<boolean> {
  return RnAlarm.hasAlarmPermission();
}

/**
 * Checks if notification permission has been granted.
 */
export function hasNotificationPermission(): Promise<boolean> {
  return RnAlarm.hasNotificationPermission();
}

/**
 * Checks if permission to launch the app from background has been granted.
 */
export function hasLaunchAppPermission(): Promise<boolean> {
  return RnAlarm.hasLaunchAppPermission();
}

/**
 * Requests the alarm permission be granted. (This is currently not a
 * permission that can be disabled, but is left here for completeness in
 * case the permission is required in the future.)
 */
export function requestAlarmPermission(
  config: TPermissionConfig = {}
): Promise<boolean> {
  return RnAlarm.requestAlarmPermission(config);
}

/**
 * Requests the notification permission be granted.
 */
export function requestNotificationPermission(
  config: TPermissionConfig = {}
): Promise<boolean> {
  return RnAlarm.requestNotificationPermission(config);
}

/**
 * Requests permission to launch the app from the background.
 */
export function requestLaunchAppPermission(
  config: TPermissionConfig = {}
): Promise<boolean> {
  return RnAlarm.requestLaunchAppPermission(config);
}

// ------------------------------------------------------------------------------
// Event Listeners
// ------------------------------------------------------------------------------

export function useCurrentlyPlayingAlarm() {
  const [alarm, setAlarm] = useState<TAlarm | null>(null);

  useEffect(() => {
    async function fetchAlarm() {
      setAlarm(await getCurrentlyPlayingAlarm());
    }
    fetchAlarm();

    const eventEmitter = new NativeEventEmitter(NativeModules.ToastExample);
    const alarmPlayingListener = eventEmitter.addListener(
      'AlarmPlaying',
      (event) => {
        setAlarm(event);
      }
    );

    return () => alarmPlayingListener.remove();
  }, []);

  return alarm;
}

// ------------------------------------------------------------------------------
// Types
// ------------------------------------------------------------------------------

export type TAlarm = {
  id: number;
  hour: number;
  minute: number;
  second: number;
  enabled: boolean;
  name: string | null;
  repeatOnDays: string | null;
  soundPath: string | null;
  soundDuration: number | null;
  soundVolume: number;
  launchApp: boolean;
  militaryTime: boolean;
  snoozeTime: number;
  autoSnooze: boolean;
  maxAutoSnoozeCounter: number;
  showNotif: boolean;
  notifTitle: string;
  notifDescription: string;
  notifShowSnooze: boolean;
  notifSnoozeText: string;
  notifShowTurnOff: boolean;
  notifTurnOffText: string;
  showReminderNotif: boolean;
  reminderVolume: number;
  reminderNotifTimeBefore: number;
  reminderNotifTitle: string;
  reminderNotifDescription: string;
  reminderNotifTurnOffText: string;
  showMissedNotif: boolean;
  missedNotifTitle: string;
  missedNotifDescription: string;
  extraConfigJson: string | null;
  adjustWithTimezone: boolean;
  adjustWithDaylightSavings: boolean;
};

type TAlarmConfig = Pick<TAlarm, 'id' | 'hour' | 'minute'> & Partial<TAlarm>;

type TAlarmUpdateConfig = Pick<TAlarm, 'id'> & Partial<TAlarm>;

/**
 * Config options when requesting permissions.
 */
type TPermissionConfig = {
  title?: string;
  message?: string;
  cancelText?: string;
  openSettingsText?: string;
};
