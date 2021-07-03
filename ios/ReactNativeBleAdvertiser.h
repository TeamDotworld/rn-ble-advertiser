#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTLog.h>
#import <CoreBluetooth/CoreBluetooth.h>

@interface ReactNativeBleAdvertiser : NSObject <RCTBridgeModule, CBCentralManagerDelegate, CBPeripheralManagerDelegate, CBPeripheralDelegate> {
    CBPeripheralManager *peripheralManager;
}

@end
