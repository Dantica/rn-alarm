package com.rnalarm

import android.content.ContentResolver
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import kotlin.math.ln

/**
 * Responsible for running alarm sounds.
 */
object RnAlarmPlayer {
  private var mediaPlayer: MediaPlayer? = MediaPlayer()
  private var reminderMediaPlayer: MediaPlayer? = MediaPlayer()
  private var userStopped: Boolean = false
  private var userSnoozed: Boolean = false
  var currentPlayingAlarmID: Int? = null

  /**
   * Play an alarm sound.
   * @param alarm Alarm to play.
   * @param onAlarmUserStop Action to perform if the user stops the alarm.
   * @param onAlarmUserSnooze Action to perform if the user snoozed the alarm.
   * @param onAlarmTimeout Action to perform if the alarm times out.
   */
  fun playAlarm(
    context: Context,
    alarm: RnAlarm,
    onAlarmUserStop: () -> Unit,
    onAlarmUserSnooze: () -> Unit,
    onAlarmTimeout: () -> Unit
  ) {
    if (mediaPlayer != null && mediaPlayer!!.isPlaying) return

    val defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    var alarmSoundUri = defaultRingtoneUri
    if (alarm.soundPath !== null) {
      alarmSoundUri = Uri.parse(
        ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.packageName + alarm.soundPath
      )
    }

    try {
      // Set volume to maximum to allow user's volume to be set directly in the media player.
      val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
      val originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
      audioManager.setStreamVolume(
        AudioManager.STREAM_ALARM, audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0
      )

      mediaPlayer = MediaPlayer()

      // Set volume to value specified by alarm.
      val logVolume = changeVolumeScale(alarm.soundVolume)
      mediaPlayer!!.setVolume(logVolume, logVolume) // For both left and right ear channels.

      val audioAttributes =
        AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
          .setUsage(AudioAttributes.USAGE_ALARM).build()
      mediaPlayer!!.setAudioAttributes(audioAttributes)
      mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_ALARM)

      // Set source and prepare.
      mediaPlayer!!.setDataSource(context, alarmSoundUri)
      mediaPlayer!!.prepare()

      // Set to stop the track after the specified duration.
      val duration = alarm.soundDuration ?: mediaPlayer!!.duration
      val handlerThread = HandlerThread("StopTrackAfterDurationHandlerThread")
      handlerThread.start()
      val handler = Handler(handlerThread.looper)
      handler.postDelayed({
        stop()
        handlerThread.quitSafely()
      }, duration.toLong())
      mediaPlayer!!.isLooping = true

      // Prepare to send alarm playing events
      val rnAlarmEvent = RnAlarmEvent(context)

      // Set listener when alarm stops. This could either be when the time runs out (above), or if
      // the user actively stops the alarm.
      mediaPlayer!!.setOnCompletionListener { mp ->
        mp.release()
        mediaPlayer = null
        handlerThread.quitSafely()
        currentPlayingAlarmID = null
        // Reset to original volume afterwards.
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, originalVolume, 0)

        if (userStopped) {
          onAlarmUserStop()
          userStopped = false
        } else if (userSnoozed) {
          onAlarmUserSnooze()
          userSnoozed = false
        } else {
          onAlarmTimeout()
        }

        rnAlarmEvent.sendAlarmPlayingEvent(null)
      }

      mediaPlayer!!.start()
      currentPlayingAlarmID = alarm.id
      rnAlarmEvent.sendAlarmPlayingEvent(alarm)
      Log.d("rn-alarm-debug", "Playing alarm...")
    } catch (e: Exception) {
      Log.d("rn-alarm-debug", "Failed to play alarm sound. " + e.stackTraceToString())
    }
  }

  /**
   * Stops the alarm from playing.
   * @param userStopped Whether the user stopped the alarm manually.
   * @param userSnoozed Whether the user has snoozed the alarm.
   */
  fun stop(userStopped: Boolean = false, userSnoozed: Boolean = false) {
    if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
      this.userStopped = userStopped
      this.userSnoozed = userSnoozed
      mediaPlayer!!.isLooping = false
      mediaPlayer!!.seekTo(mediaPlayer!!.duration)
    }
    if (reminderMediaPlayer != null && reminderMediaPlayer!!.isPlaying) {
      reminderMediaPlayer!!.seekTo(reminderMediaPlayer!!.duration)
    }
  }

  /**
   * Checks whether the alarm sound is playing.
   */
  fun isPlaying(): Boolean {
    return (mediaPlayer != null && mediaPlayer!!.isPlaying)
  }

  /**
   * Play a basic notification sound.
   */
  fun playReminderSound(context: Context, alarm: RnAlarm) {
    // Set volume to maximum to allow user's volume to be set directly in the media player.
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
    audioManager.setStreamVolume(
      AudioManager.STREAM_ALARM, audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0
    )
    // TODO: check and set alarm.reminderSoundPath

    reminderMediaPlayer = MediaPlayer()
    // Set volume to value specified by alarm.
    val logVolume = changeVolumeScale(alarm.reminderSoundVolume)
    reminderMediaPlayer!!.setVolume(
      logVolume,
      logVolume
    ) // For both left and right ear channels.

    val audioAttributes =
      AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ALARM).build()
    reminderMediaPlayer!!.setAudioAttributes(audioAttributes)
    reminderMediaPlayer!!.setAudioStreamType(AudioManager.STREAM_ALARM)

    val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    reminderMediaPlayer!!.setDataSource(context, notificationSoundUri)

    reminderMediaPlayer!!.prepare()
    reminderMediaPlayer!!.setOnCompletionListener { mp ->
      mp.release()
      // Reset to original volume afterwards.
      audioManager.setStreamVolume(AudioManager.STREAM_ALARM, originalVolume, 0)

      reminderMediaPlayer = null
      currentPlayingAlarmID = null
    }
    reminderMediaPlayer!!.start()
    currentPlayingAlarmID = alarm.id
  }

  /**
   * Checks whether the reminder sound is playing.
   */
  fun isReminderPlaying(): Boolean {
    return (reminderMediaPlayer != null && reminderMediaPlayer!!.isPlaying)
  }

  /**
   * Makes the volume log(log()) scaled
   * @param volume Volume between 0.0 and 100.0
   * @return The log(log()) scaled volume between 0.0 and 1.0
   */
  private fun changeVolumeScale(volume: Float): Float {
    val maxVolume = 100
    // Log the volume scale, converting 0 to 100 volume into a double between 0 and 1
    val firstLog = 1 - ln((1 + maxVolume - volume).toDouble()) / ln((1 + maxVolume).toDouble())

    // Log the scale again, converting 0 to 100 logged volume into a double between 0 and 1
    val secondLog = 1 - ln(1 + maxVolume - firstLog * maxVolume) / ln((1 + maxVolume).toDouble())
    return secondLog.toFloat()
  }
}
