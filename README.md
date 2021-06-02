# React Native BLE Advertiser

Advertise given message using BLE

## Installation

For installing this package from GitHub Package registry read the steps [here](https://docs.github.com/en/enterprise-server@2.22/packages/working-with-a-github-packages-registry/working-with-the-npm-registry#installing-a-package)

Authenticate to GitHub package registry using below command. Use Token

```sh
$ npm login --scope=@teamdotworld --registry=https://npm.pkg.github.com

> Username: USERNAME
> Password: TOKEN
> Email: PUBLIC-EMAIL-ADDRESS
```

Now install the package

```sh
npm install @teamdotworld/rn-ble-advertiser@1.0.3
```

## Usage

Add this to your AndroidManifest.xml inside application tag

```xml
    <application ...>
        ...
    <service android:name="dev.dotworld.ble.services.BluetoothMonitoringService" />
        ...
    </application>
```

```js
import { Platform } from 'react-native';
import BleAdvertiser from '@teamdotworld/rn-ble-advertiser';

// This module is currenly available only for android. Check if the platform is android and then use
if (Platform.OS === 'android') {
  BleAdvertiser.setUserId('test');
  BleAdvertiser.startService();
}
```

## Issues

Known issues

- Module currenly supports minimum android sdk of 22. Change your **_minSdkVersion_** in android/build.gradle to 22

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
