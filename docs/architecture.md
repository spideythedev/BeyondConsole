# Architecture notes

These are working notes. They describe the direction of the project, not features that are already finished.

## First target

Android comes first. The initial app shell is in `app/`. It can save one named console profile (name plus local IP address) on the device and show the current network transport. It does not perform console discovery, registration or streaming.

The next milestone is to get a real Remote Play session working. That means console registration, session setup, video, audio and controller input. The UI should follow the working session layer, not pretend those pieces are ready.

## Existing work

Before implementing a protocol, compare the available options and their licenses. chiaki-ng is the main candidate to evaluate. Its repository already contains:

- A Kotlin Android application with console discovery, registration, settings and streaming activities.
- A C JNI layer connected to chiaki-lib.
- Android MediaCodec video decoding.
- Oboe-based audio output.
- Touch controls and controller input handling.
- A Gradle build that compiles the native library with CMake.

The upstream Android project uses package `com.metallic.chiaki`, Kotlin/JVM 17, compile SDK 35, NDK 28.2 and CMake 3.22.1.

## Integration decision

The next engineering task is to build the upstream Android client unchanged and test it on a device. After that, decide whether to maintain a BeyondConsole fork or integrate its native and session layers into this app.

Do not copy source files selectively until the required native dependencies, submodules, assets, generated files and build settings are understood.

chiaki-ng is AGPL-3.0. Keep its copyright and license notices. Review the obligations for modified or combined distributions before publishing BeyondConsole builds.

## Proposed boundaries

- **App:** console list, connection flow, session controls and settings.
- **Remote Play:** registration, connection lifecycle and protocol integration.
- **Media:** video decode, rendering and audio playback.
- **Input:** controller mapping and optional touch controls.
- **Diagnostics:** connection state, measured latency, frame rate and dropped-frame information.
- **Platform:** Android and desktop APIs for decoding, audio, networking and controller access.

These boundaries are a guide, not a reason to split every folder into its own library.

## Things to measure

- Time from selecting a console to seeing the first frame.
- Input response time, where it can be measured reliably.
- Video frame rate and dropped frames.
- Audio interruptions.
- Stream stability on local Wi-Fi and remote networks.
- Battery and thermal behaviour on Android.

## Not in scope

- Running PS5 game binaries natively on Android or ordinary PCs.
- Downloading or distributing commercial game files.
- Circumventing console security or account restrictions.
- Claiming compatibility before it has been tested.
