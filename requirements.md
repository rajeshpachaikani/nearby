# Nearby Device Discovery App - Requirements Document

## 1. Project Overview

### 1.1 Purpose
A modern Android application that discovers and displays nearby network devices (via SSDP/mDNS) and Bluetooth Low Energy (BLE) devices in a developer-friendly interface. The app serves as a reference tool for low-level developers to understand device discovery protocols and inspect nearby devices.

### 1.2 Target Audience
- Android developers working with network protocols
- IoT developers
- Hardware engineers testing BLE devices
- Network administrators
- Tech enthusiasts exploring local network devices

### 1.3 Key Differentiators
- Modern UI/UX with Material Design 3
- Combined SSDP, mDNS, and BLE discovery in one app
- No authentication required
- Developer-focused information display
- Active maintenance and modern Android SDK support

## 2. Functional Requirements

### 2.1 Core Features

#### 2.1.1 Device Discovery
- **Network Discovery (SSDP)**
    - Discover UPnP devices using Simple Service Discovery Protocol
    - Display device type, manufacturer, model name, and service descriptions
    - Show device location URLs and service endpoints

- **Network Discovery (mDNS/Bonjour)**
    - Discover devices advertising via mDNS/DNS-SD
    - List service types (e.g., _http._tcp, _printer._tcp, _airplay._tcp)
    - Display hostname, IP address, port, and TXT records

- **Bluetooth Low Energy Discovery**
    - Scan for BLE devices in range
    - Display device name, MAC address, and RSSI (signal strength)
    - Show advertised services (UUIDs)
    - Display manufacturer data and service data when available
    - Indicate connectable vs non-connectable devices

#### 2.1.2 User Interface

**Main Screen**
- Tabbed interface with three tabs: "Network (SSDP)", "Network (mDNS)", "Bluetooth LE"
- Floating Action Button (FAB) to start/stop scanning
- Pull-to-refresh gesture for manual refresh
- Empty state with helpful instructions when no devices found
- Loading indicator during active scanning
- Device count badge on each tab

**Device List View**
- Card-based layout for each discovered device
- Primary information visible at a glance:
    - Device icon (auto-detected based on type)
    - Device name/hostname
    - Signal strength indicator (for BLE)
    - IP address (for network devices)
    - Quick status indicators (online/offline, connectable/non-connectable)
- Sort options: by name, signal strength, discovery time, device type
- Filter options: by device type, service type, manufacturer
- Search functionality across all device fields

**Device Detail View**
- Expandable sections for organized information display
- **Network Devices (SSDP):**
    - Basic info: Device type, manufacturer, model name, model number
    - Network info: IP address, location URL, server string
    - Services: List of available services with descriptions and URLs
    - UPnP info: UDN (Unique Device Name), presentation URL
    - Raw XML response (collapsible, syntax-highlighted)

- **Network Devices (mDNS):**
    - Basic info: Hostname, service type, instance name
    - Network info: IP address(es), port, protocol
    - TXT records: Key-value pairs in readable format
    - Raw DNS response (collapsible)

- **BLE Devices:**
    - Basic info: Device name, MAC address, address type
    - Signal info: RSSI value with visual indicator, TX power level
    - Services: List of advertised service UUIDs with common names
    - Manufacturer data: Hex dump and decoded interpretation
    - Service data: Per-service data in hex format
    - Advertisement type: Connectable, scannable, etc.
    - Raw advertisement data (collapsible hex view)

**Action Buttons on Detail View**
- Copy all information to clipboard
- Copy specific fields (IP, MAC, service URL)
- Share device information
- Open in browser (for network devices with web interfaces)
- Export as JSON

#### 2.1.3 Scanning Behavior
- Manual scan trigger via FAB
- Auto-stop after configurable duration (default: 30 seconds for BLE, continuous for network)
- Background scanning support with notification
- Scan interval configuration in settings
- Battery optimization warnings and guidance

### 2.2 Settings & Configuration

**Discovery Settings**
- Enable/disable individual discovery protocols
- BLE scan duration (15s, 30s, 60s, continuous)
- Network scan interval for SSDP/mDNS
- Filter by RSSI threshold for BLE devices
- Show/hide unnamed devices

**Display Settings**
- Theme selection: Light, Dark, System default
- Device icon style: Material icons, simple icons, emoji
- Show/hide technical details by default
- Information density: Compact, Normal, Detailed

**Advanced Settings**
- Developer mode toggle (shows raw packets, debug info)
- Enable logging to file
- Export scan history
- Clear cache/history

**Permissions Education**
- Clear explanation of why each permission is needed
- Link to Android documentation
- In-app permission request flow with rationale

### 2.3 Data Management

**Device History**
- Store last seen timestamp for each device
- Maintain scan history (last 7 days, configurable)
- Mark favorite devices for quick access
- Offline access to previously discovered devices
- Clear history option

**Export/Import**
- Export scan results as JSON, CSV, or TXT
- Export individual device details
- Share via Android share sheet
- No cloud sync (privacy-focused)

### 2.4 Help & Documentation

**In-App Help**
- Getting started guide (first launch)
- Protocol explanations (SSDP, mDNS, BLE basics)
- Common device types and their identifiers
- Troubleshooting guide (permissions, no devices found)
- FAQs

**Developer Resources**
- Links to protocol specifications (RFC documents)
- Code snippets for implementing discovery in own apps
- Common UUID references (BLE services and characteristics)
- About section with app version, libraries used, open source licenses

## 3. Non-Functional Requirements

### 3.1 Performance
- Initial scan results should appear within 2-3 seconds
- UI should remain responsive during scanning
- Memory usage under 100MB during active scanning
- Efficient battery usage with optimized scan intervals
- Handle discovery of 100+ devices without performance degradation

### 3.2 Compatibility
- Minimum Android SDK: API 23 (Android 6.0 Marshmallow)
- Target SDK: Latest stable Android version
- Support for Android 14+ privacy features
- Graceful degradation on devices without BLE support

### 3.3 Security & Privacy
- No data collection or analytics
- No internet permission required
- All data stored locally only
- Clear permission explanations
- No background location tracking

### 3.4 Reliability
- Handle permission denials gracefully
- Recover from Bluetooth adapter errors
- Handle network connectivity changes
- Proper error messages for all failure scenarios
- No crashes on malformed device responses

### 3.5 Usability
- Material Design 3 guidelines compliance
- Accessibility support (TalkBack, large text, high contrast)
- Intuitive navigation with clear information hierarchy
- Responsive design for tablets and foldables
- RTL language support

## 4. Technical Requirements

### 4.1 Android Permissions Required
```xml
<!-- Network discovery -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_MULTICAST_STATE" />

<!-- BLE discovery -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" android:usesPermissionFlags="neverForLocation" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- Android 12+ BLE permissions -->
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
```

### 4.2 Technology Stack Recommendations

**Architecture**
- MVVM (Model-View-ViewModel) pattern
- Single Activity architecture with Navigation Component
- Repository pattern for data management
- Kotlin Coroutines and Flow for asynchronous operations

**Core Libraries**
- AndroidX libraries (AppCompat, Fragment, RecyclerView)
- Material Design Components 3
- Jetpack Navigation Component
- Jetpack ViewModel and LiveData/StateFlow
- Room Database (for device history)
- DataStore (for settings)

**Discovery Libraries**
- Android's NsdManager for mDNS discovery
- Custom SSDP implementation using DatagramSocket
- Android BluetoothLeScanner for BLE discovery
- Consider using JmDNS library as fallback for mDNS

**UI Components**
- RecyclerView with DiffUtil for efficient list updates
- ViewBinding or Compose (if targeting modern approach)
- Material3 Theming
- SwipeRefreshLayout for pull-to-refresh

**Utilities**
- Gson or Moshi for JSON serialization
- Timber for logging
- LeakCanary for memory leak detection (debug builds)

### 4.3 Data Models

**Device Base Model**
```kotlin
interface Device {
    val id: String
    val name: String
    val type: DeviceType
    val discoveryTime: Long
    val lastSeen: Long
}

enum class DeviceType {
    SSDP, MDNS, BLE
}
```

**SSDP Device**
```kotlin
data class SsdpDevice(
    override val id: String,
    override val name: String,
    val ipAddress: String,
    val port: Int,
    val location: String,
    val server: String,
    val usn: String,
    val deviceType: String,
    val manufacturer: String,
    val modelName: String,
    val modelNumber: String,
    val services: List<SsdpService>,
    val rawXml: String
)
```

**mDNS Device**
```kotlin
data class MdnsDevice(
    override val id: String,
    override val name: String,
    val hostname: String,
    val ipAddresses: List<String>,
    val port: Int,
    val serviceType: String,
    val txtRecords: Map<String, String>
)
```

**BLE Device**
```kotlin
data class BleDevice(
    override val id: String,
    override val name: String,
    val macAddress: String,
    val rssi: Int,
    val txPowerLevel: Int?,
    val isConnectable: Boolean,
    val advertisedServices: List<UUID>,
    val manufacturerData: Map<Int, ByteArray>,
    val serviceData: Map<UUID, ByteArray>,
    val rawScanRecord: ByteArray
)
```

### 4.4 Key Implementation Notes

**SSDP Discovery**
- Send M-SEARCH multicast messages to 239.255.255.250:1900
- Parse SSDP response headers
- Fetch and parse device description XML from location URL
- Handle multiple network interfaces

**mDNS Discovery**
- Use NsdManager.discoverServices() for common service types
- Query for: _http._tcp, _printer._tcp, _airplay._tcp, _googlecast._tcp, _homekit._tcp, _ipp._tcp, _smb._tcp
- Handle service resolution asynchronously
- Parse TXT records properly

**BLE Discovery**
- Request BLUETOOTH_SCAN permission with neverForLocation flag
- Use BluetoothLeScanner.startScan() with ScanSettings
- Set scan mode to SCAN_MODE_LOW_LATENCY for quick discovery
- Parse advertisement data properly
- Handle BLE not supported on device

**Performance Optimizations**
- Use WorkManager for background periodic scans
- Implement proper lifecycle management to stop scans when app is paused
- Use Paging for large device lists
- Cache decoded device information
- Debounce rapid scan requests

## 5. UI/UX Specifications

### 5.1 Color Scheme
- Follow Material Design 3 dynamic color system
- Support for Material You theming (Android 12+)
- High contrast mode support
- Clear visual distinction between device types

### 5.2 Typography
- Roboto font family (system default)
- Clear hierarchy: Headlines, body, captions
- Monospace font for technical data (MAC addresses, UUIDs, hex values)

### 5.3 Icons
- Material Symbols for UI actions
- Device-specific icons for different device types
- Signal strength indicators for BLE devices
- Status indicators (online, offline, scanning)

### 5.4 Animations
- Smooth transitions between screens
- Pulsing animation for active scanning
- Fade in/out for list items
- Expand/collapse animations for detail sections

### 5.5 Responsive Design
- Adaptive layout for tablets (two-pane layout)
- Landscape orientation support
- Support for different screen sizes and densities
- Foldable device support

## 6. Error Handling & Edge Cases

### 6.1 Permission Scenarios
- Bluetooth disabled: Show prompt to enable with direct settings link
- Location permission denied: Explain limitation and provide rationale
- Location services disabled: Guide user to enable
- Multiple permission denials: Show comprehensive help screen

### 6.2 Device Scenarios
- No BLE hardware: Hide BLE tab, show informational message
- No Wi-Fi: Explain network discovery limitations
- Airplane mode: Detect and inform user
- No devices found: Helpful empty state with troubleshooting tips

### 6.3 Error Messages
- User-friendly language (avoid technical jargon)
- Actionable suggestions (e.g., "Try moving closer to devices")
- Links to help documentation
- Error logging for debugging

### 6.4 Network Edge Cases
- Malformed SSDP/mDNS responses: Gracefully skip and log
- Unreachable location URLs: Show partial information
- Timeout handling: Configure reasonable timeouts (5-10 seconds)
- Multiple network interfaces: Handle correctly

## 7. Testing Requirements

### 7.1 Unit Tests
- Device model parsing and serialization
- Discovery protocol implementation
- Data formatting and transformation
- Settings management

### 7.2 Integration Tests
- Permission handling flows
- Database operations
- Discovery service integration

### 7.3 UI Tests
- Navigation flows
- List scrolling and filtering
- Detail view expansion
- Settings changes

### 7.4 Manual Testing Scenarios
- Test with various real devices (smart home devices, printers, phones)
- Test on different Android versions (6.0 to latest)
- Test on different device form factors
- Battery drain testing during extended scans
- Memory leak testing

## 8. Deployment Requirements

### 8.1 Google Play Store
- App name: "Nearby Device Scanner" (or similar)
- Category: Tools > Developer Tools
- Content rating: Everyone
- Target audience: Developers and tech professionals
- Privacy policy URL (even if not collecting data)

### 8.2 App Metadata
- Clear screenshots showing all three discovery types
- Feature graphic highlighting key capabilities
- Detailed description with feature list
- Keywords: device discovery, network scanner, BLE scanner, UPnP, mDNS, Bonjour, SSDP

### 8.3 Version Management
- Semantic versioning (e.g., 1.0.0)
- Changelog maintained for each release
- Beta testing through Play Store internal/closed testing

### 8.4 Open Source Considerations
- Choose appropriate license (MIT, Apache 2.0)
- Include contribution guidelines
- Document code architecture
- Provide build instructions

## 9. Future Enhancements (Post-MVP)

### 9.1 Advanced Features
- Connect to BLE devices and read characteristics
- Invoke UPnP service actions
- Network port scanning
- Wake-on-LAN support
- Device comparison view

### 9.2 Developer Tools
- Packet capture and analysis
- Custom mDNS queries
- Advertisement simulator for testing
- API for other apps to query discovered devices

### 9.3 User Experience
- Widget for quick scanning
- Shortcuts for specific device types
- Dark theme enhancements
- Tablet-optimized UI with side-by-side comparison

### 9.4 Community Features
- Device database with crowd-sourced information
- Share custom device profiles
- Integration with developer forums

## 10. Success Metrics

### 10.1 Performance Metrics
- App launch time < 2 seconds
- Time to first device discovered < 5 seconds
- Crash-free rate > 99%
- Average memory usage < 100MB

### 10.2 User Metrics
- Daily active users
- Average session duration
- Feature usage statistics (which protocols are used most)
- User ratings and feedback

### 10.3 Technical Metrics
- Device discovery success rate
- False positive rate
- Battery impact measurement
- Network efficiency

---

## Appendix A: Common Device Types and Identifiers

**UPnP Device Types**
- MediaRenderer, MediaServer, InternetGatewayDevice
- Printer, Scanner, WANDevice
- HVAC, Light, DoorLock (IoT devices)

**Common mDNS Service Types**
- _http._tcp (web servers)
- _printer._tcp, _ipp._tcp (printers)
- _ssh._tcp (SSH servers)
- _airplay._tcp (Apple AirPlay)
- _googlecast._tcp (Chromecast)
- _homekit._tcp (Apple HomeKit)

**Common BLE Services**
- Heart Rate (0x180D)
- Battery Service (0x180F)
- Device Information (0x180A)
- Generic Access (0x1800)
- Environmental Sensing (0x181A)

## Appendix B: References

- UPnP Device Architecture: http://upnp.org/specs/arch/UPnP-arch-DeviceArchitecture-v2.0.pdf
- RFC 6762 (mDNS): https://tools.ietf.org/html/rfc6762
- RFC 6763 (DNS-SD): https://tools.ietf.org/html/rfc6763
- Bluetooth Core Specification: https://www.bluetooth.com/specifications/bluetooth-core-specification/
- Android BLE Documentation: https://developer.android.com/guide/topics/connectivity/bluetooth/ble-overview

---

**Document Version:** 1.0  
**Last Updated:** January 26, 2026  
**Author:** Requirements Specification for AI-Assisted Development