import React, { useEffect, useState } from 'react';
import { StyleSheet, Text, TouchableNativeFeedback, View } from 'react-native';
import { setupAndroidNotifChannel, useCurrentlyPlayingAlarm } from 'rn-alarm';
import { AlarmManagerScreen } from './AlarmManagerScreen';
import { AlarmScreen } from './AlarmScreen';
import { PermissionScreen } from './PermissionScreen';

export default function App() {
  const [screen, setScreen] = useState<'Alarms' | 'Permissions'>('Alarms');

  const alarmPlaying = useCurrentlyPlayingAlarm();

  useEffect(() => {
    setupAndroidNotifChannel();
  }, []);

  return (
    <View style={styles.container}>
      <View style={styles.contentContainer}>
        {alarmPlaying ? (
          <AlarmScreen alarm={alarmPlaying} />
        ) : (
          <>
            {screen === 'Alarms' && <AlarmManagerScreen />}
            {screen === 'Permissions' && <PermissionScreen />}
          </>
        )}
      </View>

      <View style={styles.bottomBar}>
        <TouchableNativeFeedback onPress={() => setScreen('Alarms')}>
          <View style={styles.bottomBarItem}>
            <Text>Alarms</Text>
          </View>
        </TouchableNativeFeedback>
        <TouchableNativeFeedback onPress={() => setScreen('Permissions')}>
          <View style={styles.bottomBarItem}>
            <Text>Permissions</Text>
          </View>
        </TouchableNativeFeedback>
      </View>
    </View>
  );
}

export const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  contentContainer: {
    flex: 1,
    paddingVertical: 30,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: '75%',
    height: 50,
    borderWidth: 1,
    borderRadius: 5,
    alignItems: 'center',
    justifyContent: 'center',
  },
  spacer: {
    height: 30,
  },
  bottomBar: {
    height: 50,
    marginTop: 'auto',
    borderTopWidth: 1,
    flexDirection: 'row',
  },
  bottomBarItem: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  alarmTime: {
    fontSize: 50,
  },
});
