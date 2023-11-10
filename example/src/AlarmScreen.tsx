import React from 'react';
import { Text, TouchableNativeFeedback, View } from 'react-native';
import {
  cancelAlarm,
  getAlarm,
  getAlarmTime,
  getAllAlarms,
  getCurrentlyPlayingAlarm,
  scheduleAlarm,
} from 'rn-alarm';
import { styles } from './App';

const ALARM_ID = 1;

export function AlarmScreen(): JSX.Element {
  return (
    <>
      <TouchableNativeFeedback
        onPress={async () =>
          displayReadableTime(await scheduleAlarmInFiveSeconds(ALARM_ID))
        }
      >
        <View style={styles.box}>
          <Text>Alarm in 5 seconds</Text>
        </View>
      </TouchableNativeFeedback>
      <TouchableNativeFeedback
        onPress={async () => console.log(await cancelAlarm(ALARM_ID))}
      >
        <View style={styles.box}>
          <Text>Cancel Alarm</Text>
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
      <TouchableNativeFeedback
        onPress={async () => console.log(await getAlarm(ALARM_ID))}
      >
        <View style={styles.box}>
          <Text>Get Last Alarm</Text>
        </View>
      </TouchableNativeFeedback>
      <TouchableNativeFeedback
        onPress={async () => displayReadableTime(await getAlarmTime(ALARM_ID))}
      >
        <View style={styles.box}>
          <Text>Get Last Alarm Time</Text>
        </View>
      </TouchableNativeFeedback>
      <TouchableNativeFeedback
        onPress={async () => console.log(await getAllAlarms())}
      >
        <View style={styles.box}>
          <Text>Get All Alarms</Text>
        </View>
      </TouchableNativeFeedback>
    </>
  );
}

async function scheduleAlarmInFiveSeconds(id: number): Promise<number> {
  const currentTime = new Date(Date.now());
  const currentHour = currentTime.getHours();
  const currentMinute = currentTime.getMinutes();
  const currentSecond = currentTime.getSeconds();
  return await scheduleAlarm({
    id,
    hour: currentHour,
    minute: currentMinute,
    second: currentSecond + 5,
    launchApp: true,
    showNotification: true,
    notificationConfig: {
      title: 'Test Title',
      description: 'Bing Bong',
    },
  });
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
