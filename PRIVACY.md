# Privacy Policy — Nearby

Last updated: 2026-05-09

Nearby ("the app") is an open-source Android utility for discovering devices
on a local network using Bluetooth Low Energy (BLE), mDNS and SSDP. The app
is published by Unartech.

## What we collect
Nothing. The app does not collect, store, transmit or share any personal data,
analytics, crash reports or telemetry. There are no accounts, no logins and no
third-party SDKs that send data off the device.

## What is processed locally
The app reads broadcast packets from BLE, mDNS and SSDP. These packets are
displayed in the user interface and held in memory only. If the user explicitly
"saves" a device, a minimal subset of that device's broadcast metadata
(name, MAC / hostname, vendor, protocol, last seen time) is written to the
app's private DataStore on the device. This data never leaves the device.

If the user taps the share button on a device detail screen, the app writes a
plain-text file (`device_info.txt`) into its own cache directory and hands it
to the standard Android share sheet. The user chooses where it goes; the app
itself sends nothing.

## Permissions
- `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT` — for BLE discovery and GATT inspection.
  Declared with `usesPermissionFlags="neverForLocation"` so the OS never derives
  location from BLE results.
- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` (Android 11 and below only) —
  legacy OS requirement for BLE scanning on older Android versions. Capped via
  `android:maxSdkVersion="30"`.
- `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE`,
  `CHANGE_WIFI_MULTICAST_STATE` — for mDNS and SSDP, which require multicast
  UDP on the local Wi-Fi.

## Children
The app is targeted at developers and is not directed at children.

## Open source
The full source code is available at
https://github.com/rajeshpachaikani/nearby under the MIT licence. Anyone can
audit the codebase to verify the claims above.

## Contact
rajesh.unartech@gmail.com
