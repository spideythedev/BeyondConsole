# BeyondConsole user guide

## Current status

BeyondConsole is an early Android companion project. The current app stores
console connection profiles and scans the local Wi-Fi subnet for devices that
accept a TCP connection on port 9295.

It does not currently register with PlayStation Network, authenticate a
console, negotiate a Remote Play session, decode a video stream, play console
audio, or send controller input.

## Save a console profile

1. Connect your Android device to a network you control.
2. Find the console's local IPv4 address in its network settings or router.
3. Open BeyondConsole.
4. Enter a name such as `Living room PS5`.
5. Enter the console's local IPv4 address.
6. Tap **Save console address**.
7. Tap **Select** on the profile you want to work with.

Profiles are stored locally on the Android device. They are not uploaded to a
BeyondConsole server.

## Scan the local network

1. Connect the phone and console to the same Wi-Fi network.
2. Tap **Scan local network**.
3. Wait for the scan to finish.
4. Treat each result as a potential device only.

The scan tests TCP port 9295. Routers, firewalls, console sleep states, guest
network isolation, and Remote Play settings can affect results. A port match
does not verify the device identity or guarantee that Remote Play will work.

## Remove a profile

Tap **Remove** beside the profile. This removes the saved entry from this
device; it does not change settings on the console.

## Troubleshooting

- Make sure the phone and console are on the same local network.
- Disable guest Wi-Fi isolation if your router separates wireless clients.
- Check the console's network settings for its current local IP address.
- A console may not respond while asleep or when Remote Play is unavailable.
- You can still save an address manually when scanning finds nothing.

## Privacy

The app currently saves console names and local IP addresses in on-device
SharedPreferences. The LAN scan sends connection attempts to local addresses
on TCP port 9295. The contributor screen requests public repository metadata
from GitHub's public API. No BeyondConsole account or project backend is used.
