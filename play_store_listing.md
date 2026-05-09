# Google Play Console — Store Listing for Nearby

Reference doc to copy/paste into Play Console fields when publishing.
Package: `in.unartech.nearbydevs` · versionCode 1 · versionName 1.0

---

## 1. App details

### App name (max 30 chars)
Nearby — BLE / mDNS / SSDP

### Short description (max 80 chars)
Reference scanner for BLE, mDNS and SSDP devices nearby. Open source, MIT.

### Full description (max 4000 chars)
Nearby is a developer-focused reference Android app that scans for and decodes
three of the most common discovery protocols on a wireless LAN:

• Bluetooth Low Energy (BLE) — passive and active scanning, advertising payload
  decode, GATT service / characteristic enumeration on connect.
• Multicast DNS / DNS-SD (RFC 6762) — service browse on 224.0.0.251:5353,
  including TXT record decode and IPv4 resolution.
• SSDP / UPnP — M-SEARCH on 239.255.255.250:1900, NOTIFY parsing, and optional
  fetch of the device description XML from the LOCATION header.

It is built as a teaching artefact for embedded firmware developers and Android
engineers who want to see, in one place, what bytes a wireless device is
actually emitting and how the platform parses them. Every screen in the app
links back to the source file that produced it.

Features
• Live scanner with merged BLE / mDNS / SSDP feed and per-protocol filter chips.
• RSSI sorting, MAC / vendor / name search.
• Tap a device for a detailed view: hex dump of the raw scan record, decoded
  AD structures, GATT services with READ / WRITE / NOTIFY flags, mDNS PTR / SRV
  / TXT records, full SSDP description XML.
• Favorites tab — pin devices to keep across scans and app restarts. Devices
  not currently visible are flagged "Not nearby".
• Logs tab — chronological event stream of every scan callback, useful when
  debugging timing-sensitive devices.
• Configurable scan window, scan interval, active-vs-passive mode, M-SEARCH MX,
  duplicate filtering — all persisted via DataStore.
• Light / dark / system theming.
• Share device info as a plain-text file via the Android share sheet.
• Fully open source under MIT. No tracking, no analytics, no ads, no account.

Source: https://github.com/rajeshpachaikani/nearby

Permissions explained
• Bluetooth scan / connect — required for BLE discovery and GATT inspection.
  We declare `usesPermissionFlags="neverForLocation"` so the OS never derives
  location from BLE results.
• Location (Android 6 – 11 only) — legacy requirement for BLE scanning on older
  Android versions. Not requested on Android 12+.
• Internet, network state, Wi-Fi state, multicast lock — required for mDNS and
  SSDP, both of which rely on multicast UDP on the local network.

What this app does NOT do
• Does not connect to the internet for any purpose other than receiving
  multicast traffic on your local Wi-Fi.
• Does not collect, transmit or store any personal data.
• Does not log RSSI, MACs or device names off-device.

Suggested for
• Embedded firmware engineers building BLE peripherals, Matter / Thread
  bridges, UPnP gateways or mDNS-advertised IoT devices.
• Android engineers learning the BluetoothLeScanner / NsdManager APIs.
• Network administrators auditing what is broadcasting on a network segment.

### Tags / app category
Primary category: Tools
Secondary tag: Developer tools / Networking

### Application type
App (not Game)

### Free or paid
Free

### Contains ads
No

### In-app purchases
No

---

## 2. Contact details

| Field | Value |
| --- | --- |
| Email | rajesh.unartech@gmail.com |
| Website | https://github.com/rajeshpachaikani/nearby |
| Phone | (leave blank — optional) |
| External marketing | Off |

---

## 3. Store listing assets

Provide files at the resolutions Play requires. Source files live in `docs/store/`
(create that folder if absent).

| Asset | Spec | Required? | Notes |
| --- | --- | --- | --- |
| App icon | 512×512 PNG, 32-bit, no alpha | Required | Use `app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp` upscaled, or export from Studio. |
| Feature graphic | 1024×500 PNG/JPG | Required | Hero banner. Recommended: dark background with "Nearby — BLE · mDNS · SSDP scanner" wordmark + ProtoIcon trio. |
| Phone screenshots | min 2, max 8 · 1080×2412 PNG | Required | Suggested: 1) Discover/landing, 2) Device detail (BLE adv decode), 3) Saved tab with "Not nearby" pill, 4) Settings, 5) Logs. |
| 7-inch tablet screenshots | min 1, max 8 | Optional | Skip — phone-only layout. |
| 10-inch tablet screenshots | min 1, max 8 | Optional | Skip. |
| Promo video (YouTube URL) | Optional | Optional | Skip for v1. |

### Screenshot caption ideas (overlay text)
1. "Live scan across BLE, mDNS, SSDP."
2. "Decoded advertising payload, byte by byte."
3. "Pin devices. See when they leave the room."
4. "Tune your scan window."
5. "Every callback, in order."

---

## 4. Content rating

Open Play Console questionnaire. Suggested answers for Nearby:

| Question | Answer |
| --- | --- |
| Category of app | Utility / productivity / communication |
| Violence | None |
| Sexuality | None |
| Profanity | None |
| Controlled substances | None |
| Gambling | None |
| User-generated content | None |
| User-to-user communication | None |
| Shares user location | No |
| Personal info collection | No |
| Digital purchases | No |
| Miscellaneous | None apply |

Expected rating: **Everyone (IARC) / 3+ (PEGI) / Rated for 3+ (USK 0)**.

---

## 5. Target audience and content

| Field | Value |
| --- | --- |
| Target age groups | 18+ |
| Appeals to children | No |
| Ads disclosure | No ads |
| Mixed audience | No |

Reason: developer tool, not consumer-facing.

---

## 6. Data safety form

Declare every collection truthfully. For Nearby:

| Section | Declaration |
| --- | --- |
| Does your app collect or share any of the required user data types? | **No** |
| Is all user data encrypted in transit? | N/A (no data collected) |
| Do you provide a way for users to request data deletion? | N/A |

Result for the data safety card: "No data collected. No data shared."

---

## 7. App content

### Privacy policy URL (required)
`https://github.com/rajeshpachaikani/nearby/blob/main/PRIVACY.md`

(Create `PRIVACY.md` in the repo root with the text from §13 below before
submitting.)

### Government app
No

### Financial features
None

### Health
None

### Families policy
Not applicable (not targeted at children)

### News app
No

### COVID-19 contact tracing or status
No

---

## 8. Permissions declaration (Play Console "App content" → "Sensitive permissions")

| Permission | Declared use |
| --- | --- |
| `BLUETOOTH_SCAN` (`neverForLocation`) | Scan for nearby BLE advertisers and decode their advertising payload for inspection. Not used to derive location. |
| `BLUETOOTH_CONNECT` | Optional GATT connect to a chosen BLE peripheral so the user can enumerate its services and characteristics. |
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | Only on Android 11 and below. Pre-API 31, the Bluetooth LE scan API requires location permission even when not deriving location. Capped via `android:maxSdkVersion="30"`. |
| `CHANGE_WIFI_MULTICAST_STATE` | Required to acquire a multicast lock for mDNS (224.0.0.251:5353) and SSDP (239.255.255.250:1900). |
| `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE` | Standard local-network access. App does not contact any remote server. |

No background location → no Google policy review needed for that.

---

## 9. App access

| Field | Value |
| --- | --- |
| Is all functionality available without restrictions? | Yes — all features available on first launch, no sign-in. |
| Login credentials for review | Not required |
| Demo instructions | "Launch the app and grant Bluetooth & nearby-devices permission. The Discover tab populates within seconds on any Wi-Fi with at least one mDNS or SSDP-advertising device." |

---

## 10. Countries and pricing

Distribute in: All countries (or pick a smaller set). Free tier — no pricing
config needed.

---

## 11. App release

### Track
Production (after closed/internal testing if required by Play Console).
Recommended path for v1: **Internal testing → Closed testing → Production**.

### Release name
`1.0 (1)`

### Release notes — `en-US` (max 500 chars)
```
First public release.
- Live scan: BLE, mDNS, SSDP.
- Decoded advertising payloads, GATT discovery on connect.
- Saved tab with "Not nearby" indicator for devices out of range.
- Configurable scan window, interval, active mode, M-SEARCH MX.
- Light / dark / system theme.
- Share device info as text via the Android share sheet.
- 100% open source under MIT.
```

### Bundle / APK
Upload `app/build/outputs/bundle/release/app-release.aab`.
- versionCode: 1
- versionName: 1.0
- Signed with upload key alias `nearbydevs` (RSA 2048, valid until 2053-09-24).

---

## 12. Versioning policy (for future releases)

- Bump `versionCode` by 1 every release uploaded to Play (must be strictly increasing).
- `versionName` follows semver MAJOR.MINOR.PATCH.
- Tag the release commit `v<versionName>` and push the tag.
- Release notes go in this file under §13.

---

## 13. Privacy policy (paste into PRIVACY.md at repo root)

```markdown
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
- BLUETOOTH_SCAN, BLUETOOTH_CONNECT — for BLE discovery and GATT inspection.
- ACCESS_FINE_LOCATION (Android 11 and below only) — legacy OS requirement
  for BLE scanning on older Android versions.
- INTERNET, ACCESS_NETWORK_STATE, ACCESS_WIFI_STATE,
  CHANGE_WIFI_MULTICAST_STATE — for mDNS and SSDP, which require multicast
  UDP on the local Wi-Fi.

## Children
The app is targeted at developers and is not directed at children.

## Open source
The full source code is available at
https://github.com/rajeshpachaikani/nearby under the MIT licence. Anyone can
audit the codebase to verify the claims above.

## Contact
rajesh.unartech@gmail.com
```

---

## 14. Pre-submission checklist

- [ ] `versionCode` bumped (currently 1, fine for first upload).
- [ ] Release AAB built (`./gradlew :app:bundleRelease`) and present at
      `app/build/outputs/bundle/release/app-release.aab`.
- [ ] Upload key fingerprint matches what Play expects:
      SHA1  `93:09:C8:E3:39:C4:03:E5:00:B0:50:6E:D5:09:4D:59:D9:1C:2B:1C`
      SHA256 `29:BB:B2:71:EE:7F:36:F7:94:0D:9B:73:F1:71:16:AC:DD:9B:EF:CA:51:53:35:F4:E3:BC:F9:A5:40:86:F7:59`
- [ ] `release.jks` and `keystore.properties` backed up off-machine.
- [ ] App icon (512×512), feature graphic (1024×500), at least 2 phone
      screenshots prepared.
- [ ] `PRIVACY.md` written and pushed to GitHub at the URL declared above.
- [ ] Sensitive permissions section filled with the table from §8.
- [ ] Data safety form submitted as "No data collected".
- [ ] Content rating questionnaire completed (expected: Everyone).
- [ ] Closed / internal testing track populated with at least one tester
      before promoting to Production (Play now blocks first-time developers
      who skip closed testing).
