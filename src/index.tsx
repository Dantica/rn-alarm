import { NativeModules, Platform } from 'react-native';

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
// Alarm Scheduling
// ------------------------------------------------------------------------------

/**
 * Schedule an alarm.
 * @param alarmConfig Alarm configuration values.
 * @returns A promise which resolves the unix time the alarm has been set for.
 */
export async function scheduleAlarm(alarmConfig: TRnAlarm): Promise<number> {
  return RnAlarm.scheduleAlarm(alarmConfig);
}

/**
 * Cancel an alarm.
 * @param alarmID ID of the alarm to cancel.
 * @returns A promise that resolves to the alarm ID if successful.
 */
export async function cancelAlarm(alarmID: number): Promise<number> {
  return RnAlarm.cancelAlarm(alarmID);
}

// ------------------------------------------------------------------------------
// Access Alarms
// ------------------------------------------------------------------------------

/**
 * Get currently playing alarm (null if one is not playing).
 */
export async function getCurrentlyPlayingAlarm(): Promise<TRnAlarm | null> {
  return RnAlarm.getCurrentlyPlayingAlarm();
}

/**
 * Get alarm with ID (null if the ID does not exist).
 */
export async function getAlarm(alarmID: number): Promise<TRnAlarm | null> {
  return RnAlarm.getAlarm(alarmID);
}

/**
 * Get the time an alarm is due to set off in milliseconds (null if the ID does not exist).
 */
export async function getAlarmTime(alarmID: number): Promise<number | null> {
  return RnAlarm.getAlarmTime(alarmID);
}

/**
 * Get all alarms.
 */
export async function getAllAlarms(): Promise<TRnAlarm[]> {
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
// Types
// ------------------------------------------------------------------------------

/**
 * Alarm properties.
 */
export type TRnAlarm = {
  /** The unique identifier for the alarm. */
  id: number;
  /** The hour at which the alarm is set, in 24-hour time. */
  hour: number;
  /** The minute at which the alarm is set. */
  minute: number;
  /** The second at which the alarm is set. */
  second: number;
  /** Name given to the alarm. */
  name?: string;
  /**
   * Path to the custom sound to play. E.g. "/raw/alarm_sound.mp3". If left
   * null, will play the notification sound by default.
   */
  customSound?: string;
  /**
   * Duration the sound should play, in milliseconds. If left null, will play
   * for the duration of the sound and stop. If the value is longer than the
   * sound duration, will loop.
   */
  soundDuration?: string;
  /** Whether the alarm should automatically reschedule. */
  repeat?: boolean;
  /**
   * String containing days on which the alarm should repeat. E.g. "1234567"
   * for every day (default), or "67" for weekends, etc.
   */
  repeatOnDays?: string;
  /**
   * Whether the alarm time should change along with the timezone. E.g. If set
   * to true, an alarm set for 1pm at UTC-05:00 will automatically be converted
   * to 6pm if the device's timezone changes to UTC-00:00. Default is false.
   */
  adjustWithTimezone?: boolean;
  /**
   * Whether the alarm time should change along with the daylight savings. E.g.
   * If true, an alarm set for 1pm will automatically be converted to 2pm.
   * Default is false.
   */
  adjustWithDaylightSavings?: boolean;
  /**
   * Whether the app should be launched when the alarm goes off. Default is
   * false.
   */
  launchApp?: boolean;
  /** Whether a notification should be displayed. Default is true. */
  showNotification?: boolean;
  /**
   * The notification config. Only used if showNotification is set to true.
   * Uses default values if not specified.
   */
  notificationConfig?: TNotificationConfig;
};

/**
 * Alarm notification configuration.
 */
type TNotificationConfig = {
  /** Title text. Defaults to "Alarm". */
  title?: string;
  /** Description text. Defaults to "". */
  description?: string;
  /** Whether to show the snooze button. Defaults to false. */
  showSnoozeButton?: boolean;
  /** Snooze button text. Defaults to "Snooze". */
  snoozeButtonText?: string;
  /** Snooze duration in milliseconds. Defaults to 300000 (5 minutes). */
  snoozeTime?: number;
  /** Whether to show the turn off for today button. Defaults to false. */
  showTurnOffButton?: boolean;
  /** Turn off for today button text. Defaults to "Turn Off For Today". */
  turnOffButtonText?: string;
};

export type TPermissionConfig = {
  title?: string;
  message?: string;
  cancelText?: string;
  openSettingsText?: string;
};
