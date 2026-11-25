# Frequently asked questions

## Does BeyondConsole stream PS5 games already?
No. Video, audio, session negotiation, authentication, and controller streaming
are not implemented yet.

## Does the network scan identify a PlayStation?
No. It only checks whether TCP port 9295 accepts a connection. Results are
potential matches, not verified consoles.

## Why does scanning require Wi-Fi?
The current discovery feature scans the local IPv4 subnet. The phone needs to
be on the same reachable network as the console.

## Why is my console not found?
The console may be asleep, on another subnet, behind client isolation, or not
accepting connections on the tested port. Add its local address manually.

## Where are console profiles stored?
Locally on the Android device using SharedPreferences.

## Does BeyondConsole collect my PSN password?
The current app has no PSN login or account integration and does not ask for
PSN credentials.

## Which Android versions are supported?
The app currently targets Android 8.0 (API 26) and newer.

## How do I report a problem?
Email hellospideydev@gmail.com or open an issue in the GitHub repository.
