# Remote Play implementation review

Reviewed against chiaki-ng commit `43728af2e639fecb8a7af1cd68c29820a40de973` on 25 November 2026.

Repository: https://github.com/chiaki-ng/chiaki-ng

## Android application

The upstream Android target contains a full client application.

The Kotlin side includes:

- Console discovery through `DiscoveryManager`.
- Manual console profiles and registered-console storage.
- Registration flow and registration-key handling.
- Session lifecycle in `StreamSession`.
- Video surface management.
- Controller input from touch controls, Android key events, gamepads and motion sensors.
- Settings, logging and connection state.

The native side is in `android/app/src/main/cpp`. JNI connects the Kotlin session classes to `chiaki-lib`. The native layer handles Remote Play session creation, video decode through Android MediaCodec, audio decode and output, controller state and discovery.

The Android app uses package `com.metallic.chiaki`, compile SDK 35, min SDK 24, NDK `28.2.13676358`, CMake `3.22.1`, JDK 17 and Gradle wrapper 8.11.1. Its Android module builds the root CMake project with the desktop GUI and CLI disabled.

## Build dependencies

The upstream repository uses Git submodules. The Android build needs at least the nanopb, gf-complete, jerasure and curl sources. A checkout for a build must initialize submodules recursively.

The Android build also downloads native dependencies through CMake/Gradle. CI can fail because an upstream package registry rate-limits requests; that failure should be distinguished from compiler or source errors.

A manual GitHub Actions workflow in this repository checks out the upstream source at the commit listed above, initializes its submodules and attempts to assemble its debug APK. This keeps the reference build separate from the BeyondConsole app shell.

## Desktop client

The upstream repository also contains a Qt desktop application. The root CMake project builds the core library and optional GUI separately from the Android target. Desktop reuse needs its own build and packaging review.

## License

The upstream repository includes AGPL-3.0-only with additional OpenSSL permissions in `LICENSES/AGPL-3.0-only-OpenSSL.txt`. Keep upstream source notices and license files intact. Before distributing a modified or combined application, review the full license and satisfy its corresponding source and notice requirements.

## Current decision

Do not copy selected Kotlin or JNI files into the BeyondConsole app yet. They depend on the upstream database, preferences, model classes, resource IDs, native library, CMake options, third-party submodules and application lifecycle.

First build and run the upstream Android app unchanged. Then decide whether to maintain a clearly attributed fork or to integrate the native core and the required Kotlin layers behind a BeyondConsole interface.

The current BeyondConsole Android app is a separate shell. It saves a console IP address locally and displays Android network status. It does not discover consoles, register accounts, connect to a Remote Play session or stream media.
