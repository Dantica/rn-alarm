import React from 'react';
import { Text, TouchableNativeFeedback, View } from 'react-native';
import {
  deleteAlarm,
  getAlarm,
  getAllAlarms,
  getNextAlarmTime,
  getReadableTime,
  setAlarm,
  turnOffAlarm,
  updateAlarm,
} from 'rn-alarm';
import { styles } from './App';

export const ALARM_ID = 1;

async function scheduleAlarmIn15Seconds(id: number) {
  const currentTime = new Date(Date.now());
  const currentHour = currentTime.getHours();
  const currentMinute = currentTime.getMinutes();
  const currentSecond = currentTime.getSeconds();
  const timeInMillis = await setAlarm({
    id,
    hour: currentHour,
    minute: currentMinute,
    second: currentSecond + 15,
    // militaryTime: false,
    showReminderNotif: true,
    reminderTimeBefore: 10000,
    soundPath: '/raw/alarm_tone',
    soundDuration: 5000,
    snoozeTime: 15000,
    showNotif: true,
    showMissedNotif: true,
    soundVolume: 10,
    maxAutoSnoozeCount: 3,
  });
  console.log('Scheduled alarm for: ' + getReadableTime(timeInMillis));
}

async function updateAlarmVolume(id: number) {
  await updateAlarm({
    id,
    soundVolume: 50,
    reminderSoundVolume: 50,
  });
}

export function AlarmManagerScreen(): JSX.Element {
  return (
    <>
      <TouchableNativeFeedback
        onPress={() => scheduleAlarmIn15Seconds(ALARM_ID)}
      >
        <View style={styles.box}>
          <Text>Alarm in 15 seconds</Text>
        </View>
      </TouchableNativeFeedback>
      <TouchableNativeFeedback onPress={() => updateAlarmVolume(ALARM_ID)}>
        <View style={styles.box}>
          <Text>Update alarm to new track</Text>
        </View>
      </TouchableNativeFeedback>

      <View style={styles.spacer} />

      <TouchableNativeFeedback onPress={() => turnOffAlarm(ALARM_ID, true)}>
        <View style={styles.box}>
          <Text>Turn Off Alarm For Today</Text>
        </View>
      </TouchableNativeFeedback>

      <View style={styles.spacer} />

      <TouchableNativeFeedback
        onPress={async () => console.log(await getAlarm(ALARM_ID))}
      >
        <View style={styles.box}>
          <Text>Get Alarm</Text>
        </View>
      </TouchableNativeFeedback>
      <TouchableNativeFeedback
        onPress={async () =>
          console.log(getReadableTime(await getNextAlarmTime(ALARM_ID)))
        }
      >
        <View style={styles.box}>
          <Text>Get Next Alarm Time</Text>
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
