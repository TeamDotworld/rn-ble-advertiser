import { NativeModules } from 'react-native';


type BleAdvertiserType = {
  init(companyId: number): void;
  setData(data: string): void;
  startBroadcast(): void;
  stopBroadcast(): void;
};





const { BleAdvertiser } = NativeModules;

export default BleAdvertiser as BleAdvertiserType;
