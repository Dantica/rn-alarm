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

  companion object {
    const val NAME = "RnAlarm"
  }

  override fun getName(): String {
    return NAME
  }

  /**
   * Schedule an alarm.
   * @param alarmConfig Map containing RnAlarm fields. `id`, `hour`, and `minute` are mandatory.
   */
  @ReactMethod
  fun scheduleAlarm(
    alarmConfig: ReadableMap,
    promise: Promise,
  ) {
    val alarm = RnAlarmUtils.getRnAlarmFromMap(alarmConfig, promise)

    if (alarm != null) {
      CoroutineScope(Dispatchers.IO).launch {
        RnAlarmManager(reactApplicationContext).scheduleAlarm(alarm, promise)
      }
    }
  }

  /**
   * Cancel an alarm.
   */
  @ReactMethod
  fun cancelAlarm(
    alarmID: Int,
    promise: Promise,
  ) {
    CoroutineScope(Dispatchers.IO).launch {
      RnAlarmManager(reactApplicationContext).cancelAlarm(alarmID, promise)
    }
  }

  /**
   * Get the currently playing alarm.
   */
  @ReactMethod
  fun getCurrentlyPlayingAlarm(promise: Promise) {
    val currentlyPlayingAlarmID = RnAlarmPlayer.currentlyPlayingAlarmID()
    if (currentlyPlayingAlarmID == null) {
      promise.resolve(null)
    } else {
      CoroutineScope(Dispatchers.IO).launch {
        val currentlyPlayingAlarm =
          RnAlarmDatastore(reactApplicationContext).get(currentlyPlayingAlarmID)
        if (currentlyPlayingAlarm == null) {
          Log.d("rn-alarm-debug", "AlarmID exists, but the corresponding alarm does not.")
          promise.resolve(null)
        } else {
          promise.resolve(RnAlarmUtils.getMapFromRnAlarm(currentlyPlayingAlarm))
        }
      }
    }
  }

  /**
   * Get alarm with ID (null if the ID does not exist).
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
  fun getAlarmTime(alarmID: Int, promise: Promise) {
    CoroutineScope(Dispatchers.IO).launch {
      val alarm = RnAlarmDatastore(reactApplicationContext).get(alarmID)
      if (alarm == null) promise.resolve(null)
      else promise.resolve(RnAlarmUtils.calculateNextAlarmTime(alarm).toDouble())
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
