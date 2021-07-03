
#import <React/RCTBridgeModule.h>
#import <React/RCTLog.ßh>
#import <CoreBluetooth/CoreBluetooth.h>


@interface BleAdvertiser : NSObject <RCTBridgeModule, CBCentralManagerDelegate, CBPeripheralManagerDelegate, CBPeripheralDelegate> {
    CBPeripheralManager *peripheralManager;
}

@end
  
