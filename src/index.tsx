import { NativeModules } from 'react-native';

type ReactNativeBleAdvertiserType = {
  multiply(a: number, b: number): Promise<number>;
};

const { ReactNativeBleAdvertiser } = NativeModules;

export default ReactNativeBleAdvertiser as ReactNativeBleAdvertiserType;
