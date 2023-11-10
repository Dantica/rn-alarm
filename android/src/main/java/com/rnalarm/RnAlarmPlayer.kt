package com.rnalarm

import android.content.ContentResolver
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import kotlin.math.ln


/**
 * Responsible for running alarm sounds.
 */
object RnAlarmPlayer {
  private var mediaPlayer: MediaPlayer = MediaPlayer()
  private var currentPlayingAlarmID: Int? = null
  private var userVolume: Float = 50f

  /**
   * Play an alarm sound
   * @param sound Sound to play
   */
  fun playAlarm(context: Context, alarmID: Int, customSound: String?) {
    mediaPlayer = MediaPlayer()

    val defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    var alarmSoundUri = defaultRingtoneUri
    if (customSound !== null) {
      alarmSoundUri = Uri.parse(
        ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.packageName + customSound
      )
    }
    try {
      mediaPlayer.setDataSource(context, alarmSoundUri)
      mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION)
      mediaPlayer.prepare()
      mediaPlayer.setOnCompletionListener { mp ->
        mp.release()
        currentPlayingAlarmID = null
      }
      mediaPlayer.start()
      currentPlayingAlarmID = alarmID
    } catch (e: Exception) {
      Log.d("rn-alarm-debug", "Failed to play alarm sound")
      Log.d("rn-alarm-debug", e.stackTraceToString())
    }
  }

  fun currentlyPlayingAlarmID(): Int? {
    return currentPlayingAlarmID
  }

  /**
   * Stops the alarm from playing
   */
  fun stop() {
    if (isPlaying()) mediaPlayer.stop()
  }

  /**
   * Checks whether the alarm is playing
   */
  fun isPlaying(): Boolean {
    return mediaPlayer.isPlaying
  }

  /**
   * Set the alarm volume
   * @param volume Volume level, between 0 and 100 inclusive
   */
  fun setVolume(volume: Float) {
    val logVolume = changeVolumeScale(volume)
    // Two logVolumes for both left and right ear channels
    mediaPlayer.setVolume(logVolume, logVolume)
    userVolume = volume
  }

  //  companion object {
  private const val GONG_TRACK = "/raw/fzn_gong"
  private const val CUSTOM_NOTIFICATION_TRACK = "/raw/pristine"

  /**
   * Makes the volume log(log()) scaled
   * @param volume Volume between 0.0 and 100.0
   * @return The log(log()) scaled volume between 0.0 and 1.0
   */
  private fun changeVolumeScale(volume: Float): Float {
    val MAX_VOLUME = 100
    // Log the volume scale, converting 0 to 100 volume into a double between 0 and 1
    val firstLog = 1 - ln((1 + MAX_VOLUME - volume).toDouble()) / ln((1 + MAX_VOLUME).toDouble())

    // Log the scale again, converting 0 to 100 logged volume into a double between 0 and 1
    val secondLog =
      1 - ln(1 + MAX_VOLUME - firstLog * MAX_VOLUME) / ln((1 + MAX_VOLUME).toDouble())
    return secondLog.toFloat()
//    }
  }
}
