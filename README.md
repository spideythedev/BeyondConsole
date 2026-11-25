# BeyondConsole

BeyondConsole is a project to make PlayStation Remote Play easier to use on Android and desktop devices.

The PS5 or PS4 runs the game. BeyondConsole handles the remote session: video, audio and controller input. It will work with consoles the player owns or is allowed to access.

## Current state

The Android app is an early shell. It can save a console's local IP address and show the phone's network status. Console discovery, registration and game streaming are not connected yet.

The desktop app has not been started.

## Repository

- `app/` contains the Android project.
- `docs/architecture.md` records the implementation plan.
- `docs/chiaki-review.md` covers the Remote Play code we are evaluating.

## Build Android

The project uses JDK 17, Gradle 8.9 and Android SDK 35.

Run this from the repository root:

```sh
gradle :app:assembleDebug
```

Gradle writes the APK to `app/build/outputs/apk/debug/app-debug.apk`.

The Android build also runs on GitHub Actions for pushes and pull requests to `main`.

## Remote Play implementation

We are reviewing [chiaki-ng](https://github.com/chiaki-ng/chiaki-ng) before writing any of the Remote Play protocol ourselves. Its repository already has an Android client, native streaming code and desktop support.

chiaki-ng is licensed under AGPL-3.0. Any code taken from it must keep the original notices and follow the license. We will settle the integration and distribution details before shipping a build based on that code.

## Scope

BeyondConsole is an independent project. It is not affiliated with or endorsed by Sony Interactive Entertainment. PlayStation, PS4 and PS5 are trademarks of their respective owners.

BeyondConsole will not distribute game files, bypass console security or connect to consoles without the owner's authorization.
