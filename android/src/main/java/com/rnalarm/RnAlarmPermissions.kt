package com.rnalarm

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.modules.core.PermissionAwareActivity
import com.facebook.react.modules.core.PermissionListener


class RnAlarmPermissions(private val context: ReactApplicationContext) {
  companion object {
    object NOTIFICATION_CHANNEL {
      const val ID = "rn_alarm_channel"
      const val NAME = "Alarms"
      const val DESC = "Notifications that display during alarms."
    }
  }

  /**
   * Set up notifications channel, required for API 26 onwards.
   */
  @ReactMethod
  fun setupAndroidNotifChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val alarmNotificationChannel = NotificationChannel(
        NOTIFICATION_CHANNEL.ID,
        NOTIFICATION_CHANNEL.NAME,
        NotificationManager.IMPORTANCE_HIGH,
      )
      alarmNotificationChannel.description = NOTIFICATION_CHANNEL.DESC

      // Remove sounds as I play them manually with MediaPlayer.
      alarmNotificationChannel.setSound(null, null)

      val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(alarmNotificationChannel)
    }
  }


  /**
   * Checks if a given permission has been granted.
   * @param permission E.g. Manifest.permission.POST_NOTIFICATIONS
   */
  fun hasPermission(permission: String): Boolean {
    return when (permission) {
      // Handle notification carefully as prior to API 33 it wasn't technically a permission.
      Manifest.permission.POST_NOTIFICATIONS -> {
        var notificationsEnabled = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          val permissionStatus =
            ContextCompat.checkSelfPermission(context, permission)
          notificationsEnabled = (permissionStatus == PackageManager.PERMISSION_GRANTED)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
          val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
          notificationsEnabled = notificationManager.areNotificationsEnabled()
        } else {
          return true
        }

        // Also need to check if alarm channel has been turned off.
        return notificationsEnabled && isNotificationChannelEnabled()
      }

      Manifest.permission.SYSTEM_ALERT_WINDOW -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          return Settings.canDrawOverlays(context)
        }
        return true
      }

      // Default case
      else -> {
        val permissionStatus =
          ContextCompat.checkSelfPermission(context, permission)
        return (permissionStatus == PackageManager.PERMISSION_GRANTED)
      }
    }
  }

  /**
   * Requests a permission be granted.
   * @param permission E.g. Manifest.permission.POST_NOTIFICATIONS
   */
  fun requestPermission(
    permission: String,
    config: ReadableMap,
    promise: Promise,
  ) {
    val title = config.getString("title") ?: "Permission Required"
    val message =
      config.getString("message")
        ?: ("You must grant the app access to " + getReadablePermission(permission) + '.')
    val cancelText = config.getString("cancelText") ?: "Cancel"
    val openSettingsText = config.getString("openSettingsText") ?: "Open Settings"

    if (hasPermission(permission)) {
      promise.resolve(true)
      return
    }

    // Permission not granted, request it
    val thisRequestCode = 123 // You can use any unique request code here
    val permissionListener =
      object : PermissionListener {
        override fun onRequestPermissionsResult(
          requestCode: Int,
          permissions: Array<out String>,
          grantResults: IntArray,
        ): Boolean {
          if (requestCode == thisRequestCode && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED && isNotificationChannelEnabled()
          ) {
            // Permission granted
            promise.resolve(true)
            return true
          } else {
            // Permission denied
            val openIntent: Intent
            when (permission) {
              Manifest.permission.POST_NOTIFICATIONS -> {
                if (isNotificationChannelEnabled()) {
                  openIntent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                } else {
                  openIntent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                  openIntent.putExtra(
                    Settings.EXTRA_CHANNEL_ID,
                    NOTIFICATION_CHANNEL.ID
                  )
                }
              }

              Manifest.permission.SYSTEM_ALERT_WINDOW -> {
                openIntent = Intent(
                  Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                  Uri.parse("package:" + context.packageName)
                )
              }

              else -> {
                openIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
              }
            }

            showOpenSettingsDialog(title, message, cancelText, openSettingsText, openIntent)

            promise.resolve(false)
            return false
          }
        }
      }

    val activity = context.currentActivity as PermissionAwareActivity
    activity.requestPermissions(arrayOf(permission), thisRequestCode, permissionListener)
  }

  /**
   * Checks whether notifications for the specific channel are enabled.
   */
  private fun isNotificationChannelEnabled(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
    if (TextUtils.isEmpty(NOTIFICATION_CHANNEL.ID)) return false

    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channel = manager.getNotificationChannel(NOTIFICATION_CHANNEL.ID)
    return (channel.importance != NotificationManager.IMPORTANCE_NONE) && NotificationManagerCompat.from(
      context
    ).areNotificationsEnabled()
  }

  /**
   * Gets readable version of a permission.
   */
  private fun getReadablePermission(permission: String): String? {
    // Prior to API 33, notifications weren't really considered permissions
    if (permission == Manifest.permission.POST_NOTIFICATIONS && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
      return "show notifications"
    }

    if (permission == Manifest.permission.SYSTEM_ALERT_WINDOW) {
      return "launch from the background"
    }

    val packageManager = context.packageManager
    try {
      val permissionInfo = packageManager.getPermissionInfo(permission, 0)
      return permissionInfo.loadLabel(packageManager).toString()
    } catch (e: PackageManager.NameNotFoundException) {
      e.printStackTrace()
    }
    return null
  }

  /**
   * Show a dialog to open settings to grant permission access.
   */
  private fun showOpenSettingsDialog(
    title: String,
    message: String,
    cancelText: String,
    openSettingsText: String,
    openIntent: Intent,
  ) {
    openIntent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    val builder = AlertDialog.Builder(context.currentActivity)
    builder.setTitle(title)
    builder.setMessage(message)
    builder.setNegativeButton(cancelText) { dialog, _ ->
      dialog.dismiss()
    }
    builder.setPositiveButton(openSettingsText) { _, _ ->
      context.startActivity(openIntent)
    }
    builder.create().show()
  }
}
