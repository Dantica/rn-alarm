package com.rnalarm

import android.content.Context
import com.facebook.react.ReactApplication
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule

class RnAlarmEvent(private val context: Context) {
  private fun sendEvent(eventName: String, params: WritableMap?) {
    val rnApp = context.applicationContext as ReactApplication
    val reactContext = rnApp.reactNativeHost.reactInstanceManager.currentReactContext

    reactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      ?.emit(eventName, params)
  }

  fun sendAlarmPlayingEvent(alarm: RnAlarm?) {
    sendEvent(
      "AlarmPlaying",
      if (alarm == null) null
      else RnAlarmUtils.getMapFromRnAlarm(alarm)
    )
  }
}
