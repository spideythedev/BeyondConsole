# Architecture

## Current Android app

The app is a single-activity Kotlin application using Android framework Views.

- `MainActivity` owns the current screen, console profile form, local profile
  list, network status, and LAN scan.
- `SharedPreferences` stores the profile list and selected profile address.
- `ConnectivityManager` reads the active network and link properties.
- The discovery scan enumerates a bounded IPv4 subnet and attempts TCP
  connections to port 9295 using a small worker pool.
- GitHub contributor information is read from the public GitHub REST API when
  the Credits section is opened.

## Current limitations

The app does not yet contain a Remote Play protocol implementation. It does
not authenticate with PSN, pair with a console, negotiate a session, decode
video, play audio, or transmit controller input.

The LAN scanner is a basic port check. It must not be presented as device
identity verification.

## Planned direction

1. Isolate console profiles and persistence from the activity.
2. Add a tested device-identification and registration flow.
3. Implement protocol/session handling after compatibility research.
4. Add video and audio transport with lifecycle-safe resource management.
5. Add a responsive touch controller and configurable input mapping.
6. Add end-to-end tests on real Android devices and consoles.
