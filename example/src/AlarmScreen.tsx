import React from 'react';
import { Text, TouchableNativeFeedback, View } from 'react-native';
import {
  deleteAlarm,
  getAlarm,
  getAlarmTime,
  getAllAlarms,
  getCurrentlyPlayingAlarm,
  getReadableAlarmTime,
  setAlarm,
  snoozeAlarm,
  turnOffAlarm,
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
  const unixTime = await setAlarm({
    id,
    hour: currentHour,
    minute: currentMinute,
    second: currentSecond + 15,
    // militaryTime: false,
    showReminderNotif: true,
    reminderTimeBefore: 10000,
    soundPath: '/raw/alarm_tone',
    soundDuration: 3000,
    snoozeTime: 15000,
    showNotif: true,
    showMissedNotif: true,
    soundVolume: 10,
    maxAutoSnoozeCount: 3,
  });
  displayReadableUnixTime(unixTime);
}

async function updateAlarmVolume(id: number) {
  await updateAlarm({
    id,
    soundVolume: 50,
    reminderSoundVolume: 50,
  });
}

export function AlarmScreen(): JSX.Element {
  const alarmPlaying = useCurrentlyPlayingAlarm();
  console.log('Alarm playing: ', alarmPlaying);

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
          <Text>Turn Off Alarm</Text>
        </View>
      </TouchableNativeFeedback>
      <TouchableNativeFeedback onPress={() => snoozeAlarm(ALARM_ID)}>
        <View style={styles.box}>
          <Text>Snooze Alarm</Text>
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
        onPress={async () =>
          displayReadableUnixTime(await getAlarmTime(ALARM_ID))
        }
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

function displayReadableUnixTime(unixTime: number | null) {
  if (unixTime) console.log(getReadableAlarmTime(unixTime));
}
