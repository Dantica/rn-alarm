package com.rnalarm

import android.Manifest
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RnAlarmModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {
  private val context = reactApplicationContext

  companion object {
    const val NAME = "RnAlarm"
  }

  override fun getName(): String {
    return NAME
  }

  /**
   * Schedule an alarm.
   * @param config
   * The alarm configuration properties to set.
   * @param promise
   * A promise which resolves to the unix time the alarm is next scheduled for, or null if the alarm
   * has been disabled.
   */
  @ReactMethod
  fun setAlarm(
    config: ReadableMap,
    promise: Promise,
  ) {
    val alarm = RnAlarmUtils.getRnAlarmFromMap(config, promise)

    if (alarm != null) {
      try {
        CoroutineScope(Dispatchers.IO).launch {
          RnAlarmDatastore(context).save(alarm)

          if (alarm.enabled) {
            RnAlarmScheduler(context).scheduleAlarm(alarm, isReminder = alarm.showReminderNotif)
            promise.resolve(RnAlarmUtils.calculateNextAlarmTime(alarm).toDouble())
          } else {
            RnAlarmScheduler(context).cancelAlarm(alarm.id)
            promise.resolve(null)
          }
        }
      } catch (e: Exception) {
        Log.d("rn-alarm-debug", "Failed to set alarm. ${e.message}")
        Log.d("rn-alarm-debug", e.stackTraceToString())
        promise.reject(Error("Failed to set alarm. ${e.message}"))
      }
    }
  }

  /**
   * Turn off an alarm.
   * @param alarmID
   * Alarm ID to turn off.
   * @param turnOffForToday
   * If true, guarantees the alarm is set for at least the following day.
   * @param promise
   * A promise which resolves to the unix time the alarm is next scheduled for.
   */
  @ReactMethod
  fun turnOffAlarm(
    alarmID: Int,
    turnOffForToday: Boolean,
    promise: Promise
  ) {
    CoroutineScope(Dispatchers.IO).launch {
      val alarm =
        RnAlarmDatastore(context).get(alarmID)
      if (alarm == null) {
        promise.reject(Error("Alarm with ID $alarmID not found."))
      } else {
        RnAlarmController(context, alarm).turnOffAlarm(
          userStopped = true,
          turnOffForToday = turnOffForToday
        )
        promise.resolve(
          RnAlarmUtils.calculateNextAlarmTime(alarm, turnOffForToday = turnOffForToday).toDouble()
        )
      }
    }
  }

  /**
   * Snooze an alarm.
   * @param alarmID
   * ID of the alarm to snooze.
   * @param promise
   * A promise which resolves to the unix time the alarm is next scheduled for.
   */
  @ReactMethod
  fun snoozeAlarm(
    alarmID: Int,
    promise: Promise
  ) {
    CoroutineScope(Dispatchers.IO).launch {
      val alarm =
        RnAlarmDatastore(context).get(alarmID)

      if (alarm == null)
        promise.reject(Error("Alarm with ID $alarmID not found."))
      else if (!RnAlarmPlayer.isPlaying())
        promise.reject(Error("Attempting to snooze an alarm that is not currently playing."))
      else
        RnAlarmController(context, alarm).snoozeAlarm(userSnoozed = true)
    }
  }

  /**
   * Delete an alarm.
   * @param alarmID
   * ID of the alarm to delete.
   * @param promise
   * A promise that resolves to null.
   */
  @ReactMethod
  fun deleteAlarm(
    alarmID: Int,
    promise: Promise,
  ) {
    RnAlarmScheduler(context).cancelAlarm(alarmID)
    CoroutineScope(Dispatchers.IO).launch {
      RnAlarmDatastore(context).delete(alarmID)
      promise.resolve(null)
    }
  }

  /**
   * Gets the currently playing alarm.
   * @param promise
   * A promise that resolves to the currently playing alarm, or null if one is not playing.
   */
  @ReactMethod
  fun getCurrentlyPlayingAlarm(promise: Promise) {
    val currentlyPlayingAlarmID = RnAlarmPlayer.currentPlayingAlarmID
    if (currentlyPlayingAlarmID == null) {
      promise.resolve(null)
    } else {
      CoroutineScope(Dispatchers.IO).launch {
        val currentlyPlayingAlarm =
          RnAlarmDatastore(context).get(currentlyPlayingAlarmID)
        if (currentlyPlayingAlarm != null) {
          promise.resolve(RnAlarmUtils.getMapFromRnAlarm(currentlyPlayingAlarm))
        } else {
          promise.reject(Error("Could not retrieve currently playing alarm."))
          Log.d("rn-alarm-debug", "Could not retrieve currently playing alarm.")
        }
      }
    }
  }

  /**
   * Get alarm with ID, or null if the ID does not exist.
   */
  @ReactMethod
  fun getAlarm(alarmID: Int, promise: Promise) {
    CoroutineScope(Dispatchers.IO).launch {
      val alarm = RnAlarmDatastore(reactApplicationContext).get(alarmID)
      if (alarm == null) promise.resolve(null)
      else promise.resolve(RnAlarmUtils.getMapFromRnAlarm(alarm))
    }
  }

  /**
   * Get the time an alarm is due to set off in milliseconds (null if the ID does not exist).
   */
  @ReactMethod
  fun getNextAlarmTime(alarmID: Int, promise: Promise) {
    CoroutineScope(Dispatchers.IO).launch {
      val alarm = RnAlarmDatastore(reactApplicationContext).get(alarmID)
      if (alarm == null) promise.resolve(null)
      else {
        val unixTime =
          if (alarm.snoozeCount > 0) RnAlarmUtils.calculateNextSnoozedAlarmTime(alarm)
          else RnAlarmUtils.calculateNextAlarmTime(alarm)
        promise.resolve(unixTime.toDouble())
      }
    }
  }

  /**
   * Get all alarms.
   */
  @ReactMethod
  fun getAllAlarms(promise: Promise) {
    CoroutineScope(Dispatchers.IO).launch {
      val listArray = Arguments.createArray()
      val allAlarms = RnAlarmDatastore(reactApplicationContext).getAll()
      allAlarms.forEach { alarm ->
        listArray.pushMap(RnAlarmUtils.getMapFromRnAlarm(alarm))
      }

      promise.resolve(listArray)
    }
  }

  /**
   * Set up notifications channel, required for API 26 onwards.
   */
  @ReactMethod
  fun setupAndroidNotifChannel() {
    RnAlarmPermissions(reactApplicationContext).setupAndroidNotifChannel()
  }

  /**
   * Checks if alarm permission has been granted. (This is currently not a
   * permission that can be disabled, but is left here for completeness in
   * case the permission is required in the future.)
   */
  @ReactMethod
  fun hasAlarmPermission(promise: Promise) {
    val alarmPermissions = RnAlarmPermissions(reactApplicationContext)
    promise.resolve(alarmPermissions.hasPermission(Manifest.permission.SCHEDULE_EXACT_ALARM))
  }

  /**
   * Checks if notification permission has been granted.
   */
  @ReactMethod
  fun hasNotificationPermission(promise: Promise) {
    promise.resolve(
      RnAlarmPermissions(reactApplicationContext).hasPermission(Manifest.permission.POST_NOTIFICATIONS)
    )
  }

  /**
   * Checks if permission to launch the app from background has been granted.
   */
  @ReactMethod
  fun hasLaunchAppPermission(promise: Promise) {
    val alarmPermissions = RnAlarmPermissions(reactApplicationContext)
    promise.resolve(alarmPermissions.hasPermission(Manifest.permission.SYSTEM_ALERT_WINDOW))
  }

  /**
   * Requests the alarm permission be granted. (This is currently not a
   * permission that can be disabled, but is left here for completeness in
   * case the permission is required in the future.)
   */
  @ReactMethod
  fun requestAlarmPermission(
    config: ReadableMap,
    promise: Promise,
  ) {
    RnAlarmPermissions(reactApplicationContext).requestPermission(
      Manifest.permission.SCHEDULE_EXACT_ALARM, config, promise
    )
  }

  /**
   * Requests the notification permission be granted.
   */
  @ReactMethod
  fun requestNotificationPermission(
    config: ReadableMap,
    promise: Promise,
  ) {
    RnAlarmPermissions(reactApplicationContext).requestPermission(
      Manifest.permission.POST_NOTIFICATIONS, config, promise
    )
  }

  /**
   * Requests permission to launch the app from the background.
   */
  @ReactMethod
  fun requestLaunchAppPermission(
    config: ReadableMap,
    promise: Promise,
  ) {
    RnAlarmPermissions(reactApplicationContext).requestPermission(
      Manifest.permission.SYSTEM_ALERT_WINDOW, config, promise
    )
  }
}
