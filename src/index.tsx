import { NativeModules } from 'react-native';

type BleAdvertiserType = {
  setUserId(userId: string): void;
  resetUserId(): void;
  startService(): void;
};

const { BleAdvertiser } = NativeModules;

export default BleAdvertiser as BleAdvertiserType;
