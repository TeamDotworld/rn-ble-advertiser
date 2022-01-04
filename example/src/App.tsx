import * as React from 'react';

import { StyleSheet, View, Text } from 'react-native';
import ReactNativeBleAdvertiser from '@teamdotworld/rn-ble-advertiser';

export default function App() {
  const [result, setResult] = React.useState<number | undefined>();

  React.useEffect(() => {
    ReactNativeBleAdvertiser.initializeBle();
    ReactNativeBleAdvertiser.setData('testing data from ble app');
    setTimeout(() => {
      ReactNativeBleAdvertiser.startBroadcast();
    }, 4000);
    setResult(1);
  }, []);

  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
