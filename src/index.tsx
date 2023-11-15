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

// ----------------------------------------------------------------------------
// Setting Alarms
// ----------------------------------------------------------------------------

/**
 * Set an alarm with certain configuration parameters.
 * @param config
 * Alarm configuration parameters.
 * @param config.id
 * Required. Will override an existing alarm with the same ID.
 * @param config.hour
 * Required.
 * @param config.minute
 * Required.
 * @param config.second
 * Optional. Default: 0.
 * @param config.enabled
 * Optional. Default: true.
 * @param config.name
 * Optional. Default: null.
 * @param config.repeatOnDays
 * Optional. Default: "1234567".
 * @param config.soundPath
 * Optional. Default: null.
 * @param config.soundDuration
 * Optional. Default: null.
 * @param config.soundVolume
 * Optional. Default: 50.
 * @param config.launchApp
 * Optional. Default: true.
 * @param config.militaryTime
 * Optional. Default: true.
 * @param config.snoozeTime
 * Optional. Default: 300000.
 * @param config.maxAutoSnoozeCount
 * Optional. Default: 0.
 * @param config.snoozeCount
 * Optional. Default: 0.
 * @param config.showNotif
 * Optional. Default: true.
 * @param config.notifTitle
 * Optional. Default: "Alarm".
 * @param config.notifMsg
 * Optional. Default: "Alarm for $time".
 * @param config.notifSnoozeBtn
 * Optional. Default: true.
 * @param config.notifSnoozeBtnTxt
 * Optional. Default: "Snooze".
 * @param config.notifTurnOffBtn
 * Optional. Default: true.
 * @param config.notifTurnOffBtnTxt
 * Optional. Default: "Turn Off".
 * @param config.showReminderNotif
 * Optional. Default: true.
 * @param config.reminderTimeBefore
 * Optional. Default: 300000.
 * @param config.reminderSoundPath
 * Optional. Default: null.
 * @param config.reminderSoundVolume
 * Optional. Default: config.soundVolume.
 * @param config.reminderNotifTitle
 * Optional. Default: "Upcoming Alarm".
 * @param config.reminderNotifMsg
 * Optional. Default: "Alarm set for $time".
 * @param config.reminderNotifTurnOffBtnTxt
 * Optional. Default: "Turn Off For Today".
 * @param config.showSnoozeNotif
 * Optional. Default: true.
 * @param config.snoozeNotifTitle
 * Optional. Default: "Alarm Snoozed".
 * @param config.snoozeNotifMsg
 * Optional. Default: "Next alarm set for $time".
 * @param config.snoozeNotifTurnOffBtnTxt
 * Optional. Default: "Turn Off".
 * @param config.showMissedNotif
 * Optional. Default: true.
 * @param config.missedNotifTitle
 * Optional. Default: "Missed Alarm".
 * @param config.missedNotifMsg
 * Optional. Default: "Alarm missed at $time".
 * @param config.adjustWithTimezone
 * Optional. Default: false.
 * @param config.adjustWithDaylightSavings
 * Optional. Default: false.
 * @param config.extraConfigJson
 * Optional. Default: null.
 * @returns
 * A promise that resolves to the unix time the alarm is next scheduled for, or
 * null if the alarm has been disabled.
 */
export async function setAlarm(config: TAlarmConfig): Promise<number | null> {
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
    launchApp: config.launchApp ?? true,
    militaryTime: config.militaryTime ?? true,
    snoozeTime: config.snoozeTime ?? 300000,
    maxAutoSnoozeCount: config.maxAutoSnoozeCount ?? 0,
    snoozeCount: config.snoozeCount ?? 0,
    showNotif: config.showNotif ?? true,
    notifTitle: config.notifTitle ?? 'Alarm',
    notifMsg: config.notifMsg ?? 'Alarm for $time',
    notifSnoozeBtn: config.notifSnoozeBtn ?? true,
    notifSnoozeBtnTxt: config.notifSnoozeBtnTxt ?? 'Snooze',
    notifTurnOffBtn: config.notifTurnOffBtn ?? true,
    notifTurnOffBtnTxt: config.notifTurnOffBtnTxt ?? 'Turn Off',
    showReminderNotif: config.showReminderNotif ?? true,
    reminderSoundPath: config.reminderSoundPath ?? null,
    reminderSoundVolume: config.reminderSoundVolume ?? config.soundVolume ?? 50,
    reminderTimeBefore: config.reminderTimeBefore ?? 300000,
    reminderNotifTitle: config.reminderNotifTitle ?? 'Upcoming Alarm',
    reminderNotifMsg: config.reminderNotifMsg ?? 'Alarm set for $time',
    reminderNotifTurnOffBtnTxt:
      config.reminderNotifTurnOffBtnTxt ?? 'Turn Off For Today',
    showSnoozeNotif: config.showSnoozeNotif ?? true,
    snoozeNotifTitle: config.snoozeNotifTitle ?? 'Alarm Snoozed',
    snoozeNotifMsg: config.snoozeNotifMsg ?? 'Next alarm set for $time',
    snoozeNotifTurnOffBtnTxt: config.snoozeNotifTurnOffBtnTxt ?? 'Turn Off',
    showMissedNotif: config.showMissedNotif ?? true,
    missedNotifTitle: config.missedNotifTitle ?? 'Missed Alarm',
    missedNotifMsg: config.missedNotifMsg ?? 'Alarm missed at $time',
    adjustWithTimezone: config.adjustWithTimezone ?? false,
    adjustWithDaylightSavings: config.adjustWithDaylightSavings ?? false,
    extraConfigJson: null,
  };
  return RnAlarm.setAlarm(alarm);
}

/**
 * Updates one or multiple alarm properties.
 * @param updatedAlarmConfig
 * Alarm configuration parameters to update.
 * @param updatedAlarmConfig.id
 * Must be provided.
 * @returns
 * A promise that resolves to the unix time the alarm is next scheduled for, or
 * null if the alarm has been disabled.
 */
export async function updateAlarm(
  updatedAlarmConfig: TAlarmUpdateConfig
): Promise<number | null> {
  const alarm = await getAlarm(updatedAlarmConfig.id);
  if (alarm === null) return null;
  return RnAlarm.setAlarm({ ...alarm, ...updatedAlarmConfig });
}

/**
 * Turn off an alarm. If the alarm is not playing, it will instead turn the
 * alarm off for the day and reschedule to the next available day.
 * @param alarmID
 * ID of the alarm to turn off.
 * @returns
 * A promise which resolves to the unix time the alarm is next scheduled for.
 */
export async function turnOffAlarm(alarmID: number): Promise<number> {
  return RnAlarm.turnOffAlarm(alarmID);
}

/**
 * Snooze an alarm, which will reschedule it `snoozeTime` milliseconds into the
 * future, where `snoozeTime` is an alarm property set previously.
 * @param alarmID
 * ID of the alarm to snooze.
 * @return
 * A promise which resolves to the unix time the alarm is next scheduled for.
 */
export async function snoozeAlarm(alarmID: number): Promise<number> {
  return RnAlarm.snoozeAlarm(alarmID);
}

/**
 * Permanently delete an alarm.
 * @param alarmID
 * ID of the alarm to delete.
 */
export async function deleteAlarm(alarmID: number): Promise<null> {
  return RnAlarm.deleteAlarm(alarmID);
}

// ----------------------------------------------------------------------------
// Accessing Alarms
// ----------------------------------------------------------------------------

/**
 * Get the currently playing alarm.
 * @returns
 * A promise that resolves to the currently playing alarm, or null if one is
 * not playing.
 */
export async function getCurrentlyPlayingAlarm(): Promise<TAlarm | null> {
  return RnAlarm.getCurrentlyPlayingAlarm();
}

/**
 * Get an alarm, or null it the ID does not exist.
 * @param alarmID
 * The ID of the alarm to retrieve.
 * @returns
 * A promise that resolves to the alarm corresponding to the given ID, or null
 * if an alarm with the ID does not exist.
 */
export async function getAlarm(alarmID: number): Promise<TAlarm | null> {
  return RnAlarm.getAlarm(alarmID);
}

/**
 * Get the unix time in milliseconds an alarm is next due to ring.
 * @param alarmID
 * The ID of the alarm to retrieve the alarm time from.
 * @returns
 * A promise that resolves to the unix time the alarm is next set to ring at,
 * or null if an alarm with the ID does not exist.
 */
export async function getAlarmTime(alarmID: number): Promise<number | null> {
  return RnAlarm.getAlarmTime(alarmID);
}

/**
 * Get all alarms.
 */
export async function getAllAlarms(): Promise<TAlarm[]> {
  return RnAlarm.getAllAlarms();
}

/**
 * A hook which returns the currently playing alarm, or null if one is not
 * playing.
 */
export function useCurrentlyPlayingAlarm() {
  const [currentlyPlayingAlarm, setCurrentlyPlayingAlarm] =
    useState<TAlarm | null>(null);

  useEffect(() => {
    async function fetchAlarm() {
      setCurrentlyPlayingAlarm(await getCurrentlyPlayingAlarm());
    }
    fetchAlarm();

    const eventEmitter = new NativeEventEmitter(NativeModules.ToastExample);
    const alarmPlayingListener = eventEmitter.addListener(
      'AlarmPlaying',
      (event) => {
        setCurrentlyPlayingAlarm(event);
      }
    );

    return () => alarmPlayingListener.remove();
  }, []);

  return currentlyPlayingAlarm;
}

/**
 * A helper function that converts unix time into a readable form.
 */
export function getReadableAlarmTime(unixTime: number): string {
  const date = new Date(unixTime);
  const day = String(date.getDate()).padStart(2, '0');
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const year = String(date.getFullYear()).slice(2);
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');

  return `${day}/${month}/${year} ${hours}:${minutes}:${seconds}`;
}

// ----------------------------------------------------------------------------
// Permissions
// ----------------------------------------------------------------------------

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
  config: TAlarmPermissionConfig = {}
): Promise<boolean> {
  return RnAlarm.requestAlarmPermission(config);
}

/**
 * Requests the notification permission be granted.
 */
export function requestNotificationPermission(
  config: TAlarmPermissionConfig = {}
): Promise<boolean> {
  return RnAlarm.requestNotificationPermission(config);
}

/**
 * Requests permission to launch the app from the background.
 */
export function requestLaunchAppPermission(
  config: TAlarmPermissionConfig = {}
): Promise<boolean> {
  return RnAlarm.requestLaunchAppPermission(config);
}

// ----------------------------------------------------------------------------
// Types
// ----------------------------------------------------------------------------

/**
 * Config options when requesting permissions.
 */
export type TAlarmPermissionConfig = {
  title?: string;
  message?: string;
  cancelText?: string;
  openSettingsText?: string;
};

type Expand<T> = T extends infer O ? { [K in keyof O]: O[K] } : never;
// type ExpandRecursively<T> = T extends object
//   ? T extends infer O
//     ? { [K in keyof O]: ExpandRecursively<O[K]> }
//     : never
//   : T;

export type TAlarmConfig = Expand<
  Pick<TAlarm, 'id' | 'hour' | 'minute'> & Partial<TAlarm>
>;

export type TAlarmUpdateConfig = Expand<Pick<TAlarm, 'id'> & Partial<TAlarm>>;

export type TAlarm = {
  /**
   * Unique alarm ID. This will override an existing alarm with the same ID.
   */
  id: number;
  /**
   * The hour for which the alarm is set, in 24-hour time.
   */
  hour: number;
  /**
   * The minute for which the alarm is set.
   */
  minute: number;
  /**
   * The second for which the alarm is set.
   */
  second: number;
  /**
   * If true, will schedule the alarm.
   */
  enabled: boolean;
  /**
   * Name given to the alarm.
   */
  name: string | null;
  /**
   * String containing the days on which the alarm should repeat, e.g. "12345"
   * for weekdays, "67" for weekends. (If given null or an empty string, will
   * only play once and then become disabled.)
   */
  repeatOnDays: string | null;
  /**
   * Path to the sound file, e.g. "alarm.mp3" in the resource "raw" folder
   * would be "/raw/alarm" where ".mp3" is omitted. If given an empty string or
   * null, will play the default notification sound.
   */
  soundPath: string | null;
  /**
   * Duration the sound should play, in milliseconds. If null, will play for
   * the duration of the sound. If longer than sound duration, will loop until
   * the duration has elapsed.
   */
  soundDuration: number | null;
  /**
   * Sound volume between 0 and 100.
   */
  soundVolume: number;
  /**
   * Whether the app should be launched when the alarm goes off - requires launch
   * app permission, which is usually automatically granted.
   */
  launchApp: boolean;
  /**
   * Whether to show 24 hour time when using $time inside a notification title
   * or description.
   */
  militaryTime: boolean;
  /**
   * Alarm snooze duration, in milliseconds. Snoozing the alarm can happen in
   * different ways, such as if the user presses snooze on a notification, or
   * if autoSnooze is enabled and soundDuration elapses without the alarm being
   * turned off. Note: the snooze time will be added onto the initial alarm
   * time, meaning the value must be longer than the duration of the alarm.
   * Otherwise, the snoozed alarm will play immediately. E.g. an alarm set for
   * 7:00 with a snooze time of 300000 (5 minutes) and soundDuration of 120000
   * (2 minutes) will stop ringing at 7:02, but the snoozed alarm will still
   * always ring at 7:05.
   */
  snoozeTime: number;
  /**
   * The maximum number of times an alarm will auto snooze before being missed.
   */
  maxAutoSnoozeCount: number;
  /**
   * The number of times an alarm has been snoozed.
   */
  snoozeCount: number;
  /**
   * Whether to show a notification when the alarm goes off.
   */
  showNotif: boolean;
  /**
   * Notification title text. Using `$time` will show the alarm's time, e.g.
   * "Alarm at $time".
   */
  notifTitle: string;
  /**
   * Notification description text. Using `$time` will show the alarm's time,
   * e.g. "Alarm at $time".
   */
  notifMsg: string;
  /**
   * Whether to show a snooze button in the alarm notification.
   */
  notifSnoozeBtn: boolean;
  /**
   * Text for notifSnoozeBtn.
   */
  notifSnoozeBtnTxt: string;
  /**
   * Whether to show the turn off button in the notification.
   */
  notifTurnOffBtn: boolean;
  /**
   * Text for notifTurnOffBtn.
   */
  notifTurnOffBtnTxt: string;
  /**
   * Whether to show a reminder notification alerting that an alarm is upcoming
   * Also gives the users the ability to turn the alarm off for the day.
   */
  showReminderNotif: boolean;
  /**
   * How long before the alarm a reminder notification should be shown, in
   * milliseconds.
   */
  reminderTimeBefore: number;
  /**
   * Path to a custom sound file to play during the reminder notification, e.g.
   * "reminder.mp3" in the resource "raw" folder would be "/raw/reminder" where
   * ".mp3" is omitted. If left null, will play the device's standard
   * notification sound.
   */
  reminderSoundPath: string | null;
  /**
   * Reminder sound volume between 0 and 100.
   */
  reminderSoundVolume: number;
  /**
   * Reminder notification title text. Using `$time` will show the alarm's
   * time, e.g. "Alarm at `$time`".
   */
  reminderNotifTitle: string;
  /**
   * Reminder notification description text. Using `$time` will show alarm's
   * time, e.g. "Alarm at `$time`".
   */
  reminderNotifMsg: string;
  /**
   * Text for the reminder notification's turn off button, which will turn the
   * alarm off for the day.
   */
  reminderNotifTurnOffBtnTxt: string;
  /**
   * Whether to show a notification once an alarm has been snoozed, which can
   * happen either if the user presses snooze, or if the alarm runs out and is
   * auto snoozed.
   */
  showSnoozeNotif: boolean;
  /**
   * Snooze notification title text. Using `$time` will show the alarm's
   * snoozed time, e.g. "Alarm at `$time`".
   */
  snoozeNotifTitle: string;
  /**
   * Snooze notification description text. Using `$time` will show the alarm's
   * snoozed time.
   */
  snoozeNotifMsg: string;
  /**
   * Text for the snooze notification turn off button, which will turn the
   * alarm off for the day (default: "Turn Off").
   */
  snoozeNotifTurnOffBtnTxt: string;
  /**
   * Whether to show a notification when the alarm hasn't been turned off in
   * time (and auto snooze has run out).
   */
  showMissedNotif: boolean;
  /**
   * Missed notification title text. `$time` will show the alarm's time, e.g.
   * "Missed alarm at $time".
   */
  missedNotifTitle: string;
  /**
   * Missed notification description text. `$time` will show the alarm's time,
   * e.g. "Missed alarm at $time".
   */
  missedNotifMsg: string;
  /**
   * Whether alarm time should change along with the timezone. If true, an
   * alarm set for 1pm at UTC-05:00 will automatically be converted to 6pm if
   * the device's timezone changes to UTC+00:00.
   */
  adjustWithTimezone: boolean;
  /**
   * Whether alarm time should change along with the daylight savings. If true,
   * an alarm set for 1pm will automatically be converted to 2pm once daylight
   * savings begins, and back to 1pm when it ends.
   */
  adjustWithDaylightSavings: boolean;
  /**
   * An extra string given to the user in case they which to store extra data.
   * Can be used to store an object as JSON.
   */
  extraConfigJson: string | null;
};
