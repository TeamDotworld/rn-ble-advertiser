import * as React from 'react';

import { StyleSheet, View, Text } from 'react-native';
import ReactNativeBleAdvertiser from '@teamdotworld/react-native-ble-advertiser';

export default function App() {
  const [result, setResult] = React.useState<number | undefined>();

  React.useEffect(() => {
    ReactNativeBleAdvertiser.init(1023)
    ReactNativeBleAdvertiser.setData("1234")
    setTimeout(() => {
      ReactNativeBleAdvertiser.startBroadcast()
    },4000)
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
