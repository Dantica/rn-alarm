import React from 'react';
import { Text, TouchableNativeFeedback, View } from 'react-native';
import {
  deleteAlarm,
  getAlarm,
  getAlarmTime,
  getAllAlarms,
  getCurrentlyPlayingAlarm,
  setAlarm,
  snoozeAlarm,
  turnOffAlarm,
  turnOffAlarmForToday,
  updateAlarm,
  useCurrentlyPlayingAlarm,
} from 'rn-alarm';
import { styles } from './App';

const ALARM_ID = 1;

async function scheduleAlarmInTenSeconds(id: number) {
  const currentTime = new Date(Date.now());
  const currentHour = currentTime.getHours();
  const currentMinute = currentTime.getMinutes();
  const currentSecond = currentTime.getSeconds();
  await setAlarm({
    id,
    hour: currentHour,
    minute: currentMinute,
    second: currentSecond + 10,
    showReminderNotif: true,
    reminderNotifTimeBefore: 10000,
    // launchApp: true,
    soundPath: '/raw/alarm_tone',
    soundDuration: 5000,
    snoozeTime: 15000,
    showNotif: true,
    // militaryTime: false,
    showMissedNotif: true,
    soundVolume: 10,
  });
  displayReadableTime(await getAlarmTime(id));
}

async function updateAlarmVolume(id: number) {
  await updateAlarm({ id, soundVolume: 50, reminderVolume: 50 });
  displayReadableTime(await getAlarmTime(id));
}

export function AlarmScreen(): JSX.Element {
  const alarmPlaying = useCurrentlyPlayingAlarm();
  console.log('Currently playing alarm: ', alarmPlaying);

  return (
    <>
      <TouchableNativeFeedback
        onPress={() => scheduleAlarmInTenSeconds(ALARM_ID)}
      >
        <View style={styles.box}>
          <Text>Alarm in 10 seconds</Text>
        </View>
      </TouchableNativeFeedback>
      <TouchableNativeFeedback onPress={() => updateAlarmVolume(ALARM_ID)}>
        <View style={styles.box}>
          <Text>Update alarm to new track</Text>
        </View>
      </TouchableNativeFeedback>

      <View style={styles.spacer} />

      <TouchableNativeFeedback
        onPress={async () => console.log(await getCurrentlyPlayingAlarm())}
      >
        <View style={styles.box}>
          <Text>Get Currently Playing Alarm</Text>
        </View>
      </TouchableNativeFeedback>
      <TouchableNativeFeedback onPress={() => turnOffAlarm(ALARM_ID)}>
        <View style={styles.box}>
          <Text>Stop Alarm</Text>
        </View>
      </TouchableNativeFeedback>
      <TouchableNativeFeedback onPress={() => snoozeAlarm(ALARM_ID)}>
        <View style={styles.box}>
          <Text>Snooze Alarm</Text>
        </View>
      </TouchableNativeFeedback>
      <TouchableNativeFeedback onPress={() => turnOffAlarmForToday(ALARM_ID)}>
        <View style={styles.box}>
          <Text>Turn Alarm Off For Today</Text>
        </View>
      </TouchableNativeFeedback>

      <View style={styles.spacer} />

      <TouchableNativeFeedback
        onPress={async () => console.log(await getAlarm(ALARM_ID))}
      >
        <View style={styles.box}>
          <Text>Get Current Alarm</Text>
        </View>
      </TouchableNativeFeedback>
      <TouchableNativeFeedback
        onPress={async () => displayReadableTime(await getAlarmTime(ALARM_ID))}
      >
        <View style={styles.box}>
          <Text>Get Current Alarm Time</Text>
        </View>
      </TouchableNativeFeedback>
      <TouchableNativeFeedback
        onPress={async () => console.log(await getAllAlarms())}
      >
        <View style={styles.box}>
          <Text>Get All Alarms</Text>
        </View>
      </TouchableNativeFeedback>

      <View style={styles.spacer} />

      <TouchableNativeFeedback onPress={() => deleteAlarm(ALARM_ID)}>
        <View style={styles.box}>
          <Text>Delete Current Alarm</Text>
        </View>
      </TouchableNativeFeedback>
    </>
  );
}

function displayReadableTime(millis: number | null) {
  if (!millis) {
    console.log(null);
    return;
  }

  const date = new Date(millis);
  const day = String(date.getDate()).padStart(2, '0');
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const year = String(date.getFullYear()).slice(2);
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');

  console.log(`${day}/${month}/${year} ${hours}:${minutes}:${seconds}`);
}
