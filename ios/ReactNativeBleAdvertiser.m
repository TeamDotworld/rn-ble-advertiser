#import "ReactNativeBleAdvertiser.h"
#import "TransferService.h"

@implementation ReactNativeBleAdvertiser {
    CBMutableCharacteristic* transferCharacteristics;
    NSString* dataToSend;
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE(ReactNativeBleAdvertiser)

-(NSArray<NSString *> *)supportedEvents{ 
  return @[@"level"];
}

RCT_EXPORT_METHOD(init: (nonnull NSNumber *)companyId){
    RCTLogInfo(@"setCompanyId function called %@", companyId);
    peripheralManager = [[CBPeripheralManager alloc] initWithDelegate:self queue:nil options:nil];
}

RCT_EXPORT_METHOD(setData: (nonnull NSString *)data){
    RCTLogInfo(@"setData function called %@", data);
    dataToSend = data;
}

RCT_EXPORT_METHOD(startBroadcast) {
    NSLog(@"startAdvertising");
    [peripheralManager startAdvertising:@{ CBAdvertisementDataServiceUUIDsKey : @[[CBUUID UUIDWithString:TRANSFER_SERVICE_UUID]] }];
}

RCT_EXPORT_METHOD(stopBroadcast) {
    NSLog(@"stopAdvertising");
    [peripheralManager stopAdvertising];
}

- (void)peripheralManagerDidUpdateState:(CBPeripheralManager *)peripheral {
    // Opt out from any other state
    if (@available(iOS 10.0, *)) {
        if (peripheral.state != CBManagerStatePoweredOn) {
            return;
        }
    } else {
        // TODO
    }
    
    // We're in CBPeripheralManagerStatePoweredOn state...
    NSLog(@"self.peripheralManager powered on.");
    
    // ... so build our service.
    NSData* data = [self->dataToSend dataUsingEncoding:NSUTF8StringEncoding];
    
    // Start with the CBMutableCharacteristic
    self->transferCharacteristics  = [[CBMutableCharacteristic alloc] initWithType:[CBUUID UUIDWithString:TRANSFER_CHARACTERISTIC_UUID]
                                                                      properties:CBCharacteristicPropertyRead
                                                                           value:data
                                                                     permissions:CBAttributePermissionsReadable];

    // Then the service
    CBMutableService *transferService = [[CBMutableService alloc] initWithType:[CBUUID UUIDWithString:TRANSFER_SERVICE_UUID]
                                                                        primary:YES];
    
    // Add the characteristic to the service
    transferService.characteristics = @[self->transferCharacteristics];
    
    // And add it to the peripheral manager
    [peripheralManager removeAllServices];
    NSLog(@"self.peripheralManager removeAllServices");


    [peripheralManager addService:transferService];
    NSLog(@"self.peripheralManager transferService");
}

- (void)peripheralManager:(CBPeripheralManager *)peripheral
            didAddService:(CBService *)service
                    error:(NSError *)error {
 
    if (error) {
        NSLog(@"Error publishing service: %@", [error localizedDescription]);
    }else {
        NSLog(@"Successfully added service");
    }
}

- (void)peripheralManagerDidStartAdvertising:(CBPeripheralManager *)peripheral
                                       error:(NSError *)error {
 
    if (error) {
        NSLog(@"Error advertising: %@", [error localizedDescription]);
    }else{
        NSLog(@"Advertising started");
    }
}

- (void)peripheralManager:(CBPeripheralManager *)peripheral
    didReceiveReadRequest:(CBATTRequest *)request {
    NSLog(@"Received valid read request");
    if (request.offset > self->transferCharacteristics.value.length) {
            [peripheral respondToRequest:request
                withResult:CBATTErrorInvalidOffset];
            return;
        }
    
    request.value = [self->transferCharacteristics.value
            subdataWithRange:NSMakeRange(request.offset,
            self->transferCharacteristics.value.length - request.offset)];
    
    [peripheral respondToRequest:request withResult:CBATTErrorSuccess];
}

@end
