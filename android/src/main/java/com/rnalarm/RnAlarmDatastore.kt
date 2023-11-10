package com.rnalarm

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.facebook.react.bridge.ReactApplicationContext
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rn_saved_alarms")

class RnAlarmDatastore(private val context: Context) {
  private val dataStore: DataStore<Preferences> = context.dataStore

  suspend fun save(alarm: RnAlarm) {
    val gson = Gson()
    val alarmJson = gson.toJson(alarm)

    dataStore.edit { preferences ->
      preferences[stringPreferencesKey(alarm.id.toString())] = alarmJson
    }
  }

  suspend fun delete(alarmID: Int) {
    dataStore.edit { preferences ->
      preferences.remove(stringPreferencesKey(alarmID.toString()))
    }
  }

  suspend fun get(alarmID: Int): RnAlarm? {
    val gson = Gson()
    val alarmJson = dataStore.data.first()[stringPreferencesKey(alarmID.toString())]

    return alarmJson?.let { gson.fromJson(it, RnAlarm::class.java) }
  }

  suspend fun getAll(): List<RnAlarm> {
    val gson = Gson()
    val type = object : TypeToken<RnAlarm>() {}.type
    val preferences = dataStore.data.first()
    return preferences.asMap().mapNotNull { (_, value) ->
      if (value is String) {
        gson.fromJson<RnAlarm>(value, type)
      } else {
        null
      }
    }
  }
}
