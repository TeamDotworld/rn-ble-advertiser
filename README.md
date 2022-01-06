# @teamdotworld/rn-ble-advertiser

[![Build Status](https://github.com/TeamDotworld/rn-ble-advertiser/actions/workflows/publish-package.yml/badge.svg)](https://github.com/TeamDotworld/rn-ble-advertiser/actions/workflows/publish-package.yml)

Advertise given message using BLE

---

## Installation

For installing this package from GitHub Package registry read the steps [here](https://docs.github.com/en/enterprise-server@2.22/packages/working-with-a-github-packages-registry/working-with-the-npm-registry#installing-a-package)

Authenticate to GitHub package registry using below command. Use Token

```sh
$ npm login --scope=@teamdotworld --registry=https://npm.pkg.github.com

> Username: USERNAME
> Password: TOKEN
> Email: PUBLIC-EMAIL-ADDRESS
```

Now install the package. See the releases and use latest version

```sh
npm install @teamdotworld/rn-ble-advertiser@3.0.3
```

---

## Usage

### Android

Add this to your AndroidManifest.xml inside application tag

```xml
	  <uses-permission android:name="android.permission.BLUETOOTH" />
	  <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
	  <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
	  <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <application ...>
        ...
    	<service android:name="dev.dotworld.ble.bluetooth.gatt.GattBackgroundService" />
        ...
    </application>
```

### iOS

Add this to Info.plist file for permission

```xml
		<key>NSLocationWhenInUseUsageDescription</key>
		<string>Need location permission to verify if you are scanning in correct location</string>
		<key>NSBluetoothAlwaysUsageDescription</key>
		<string>Need bluetooth permission for this app to function properly</string>
		<key>UIBackgroundModes</key>
		<array>
			<string>bluetooth-central</string>
			<string>bluetooth-peripheral</string>
		</array>
		<key>UIRequiredDeviceCapabilities</key>
		<array>
			<string>bluetooth-le</string>
		</array>

```

### React Native

```js
import { Platform } from 'react-native';
import ReactNativeBleAdvertiser from '@teamdotworld/rn-ble-advertiser';

// Use a switch to turn it on or off
ReactNativeBleAdvertiser.initializeBle(); // Initalize the service
ReactNativeBleAdvertiser.setData('1234'); // set the data
setTimeout(() => {
  // start the service after setting data. Restart if the data is changed after starting
  ReactNativeBleAdvertiser.startBroadcast();

  setTimeout(() => {
    // start the service after setting data. Restart if the data is changed after starting
    ReactNativeBleAdvertiser.stopBroadcast();
  }, 4000);
}, 4000);
```

---

## Issues

Known issues

- Module currenly supports minimum android sdk of 22. Change your **_minSdkVersion_** in android/build.gradle to 22

---

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
