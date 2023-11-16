import React, { useEffect } from 'react';
import { Text, TouchableNativeFeedback, View } from 'react-native';
import {
  getNextAlarmTime,
  getReadableTime,
  snoozeAlarm,
  TAlarm,
  turnOffAlarm,
} from 'rn-alarm';
import { ALARM_ID } from './AlarmManagerScreen';
import { styles } from './App';

async function logNextAlarmTime(alarmID: number) {
  console.log('Next alarm at: ' + (await getNextAlarmTime(alarmID)));
}

export function AlarmScreen(props: { alarm: TAlarm }): JSX.Element {
  const { alarm } = props;

  useEffect(() => {
    console.log(
      'Alarm playing at: ' + getReadableTime(alarm.lastScheduledTime)
    );
    logNextAlarmTime(alarm.id);
  }, [alarm]);

  return (
    <>
      <Text style={styles.alarmTime}>
        {getReadableTime(alarm.lastScheduledTime, {
          hideDate: true,
          autoHideSeconds: true,
        })}
      </Text>

      <View style={styles.spacer} />

      <TouchableNativeFeedback onPress={() => snoozeAlarm(ALARM_ID)}>
        <View style={styles.box}>
          <Text>Snooze Alarm</Text>
        </View>
      </TouchableNativeFeedback>

      <View style={styles.spacer} />

      <TouchableNativeFeedback onPress={() => turnOffAlarm(ALARM_ID)}>
        <View style={styles.box}>
          <Text>Turn Off Alarm</Text>
        </View>
      </TouchableNativeFeedback>

      <View style={styles.spacer} />
    </>
  );
}
