import React, { useState } from 'react';
import { Text, TouchableNativeFeedback, View } from 'react-native';
import {
  hasAlarmPermission,
  hasLaunchAppPermission,
  hasNotificationPermission,
  requestAlarmPermission,
  requestLaunchAppPermission,
  requestNotificationPermission,
} from 'rn-alarm';
import { styles } from './App';

export function PermissionScreen(): JSX.Element {
  const [alarmPerm, setAlarmPerm] = useState<boolean | undefined>();
  const [notifPerm, setNotifPerm] = useState<boolean | undefined>();
  const [launchAppPerm, setLaunchAppPerm] = useState<boolean | undefined>();

  return (
    <>
      <TouchableNativeFeedback
        onPress={() => hasAlarmPermission().then(setAlarmPerm)}
      >
        <View style={styles.box}>
          <Text>
            Alarm Permission:{' '}
            {alarmPerm === undefined ? '' : alarmPerm ? 'True' : 'False'}
          </Text>
        </View>
      </TouchableNativeFeedback>
      <TouchableNativeFeedback
        onPress={() => requestAlarmPermission().then(setAlarmPerm)}
      >
        <View style={styles.box}>
          <Text>Request Alarm Permission</Text>
        </View>
      </TouchableNativeFeedback>

      <View style={styles.spacer} />

      <TouchableNativeFeedback
        onPress={() => hasNotificationPermission().then(setNotifPerm)}
      >
        <View style={styles.box}>
          <Text>
            Notification Permission:{' '}
            {notifPerm === undefined ? '' : notifPerm ? 'True' : 'False'}
          </Text>
        </View>
      </TouchableNativeFeedback>
      <TouchableNativeFeedback
        onPress={() => requestNotificationPermission().then(setNotifPerm)}
      >
        <View style={styles.box}>
          <Text>Request NotificationPermission</Text>
        </View>
      </TouchableNativeFeedback>

      <View style={styles.spacer} />

      <TouchableNativeFeedback
        onPress={() => hasLaunchAppPermission().then(setLaunchAppPerm)}
      >
        <View style={styles.box}>
          <Text>
            Launch App Permission:{' '}
            {launchAppPerm === undefined
              ? ''
              : launchAppPerm
              ? 'True'
              : 'False'}
          </Text>
        </View>
      </TouchableNativeFeedback>
      <TouchableNativeFeedback
        onPress={() => requestLaunchAppPermission().then(setLaunchAppPerm)}
      >
        <View style={styles.box}>
          <Text>Request Launch App Permission</Text>
        </View>
      </TouchableNativeFeedback>

      <View style={styles.spacer} />
    </>
  );
}
