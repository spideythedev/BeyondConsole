# BeyondConsole

BeyondConsole is a work-in-progress project exploring a better way to use a PlayStation console from devices people already own.

The first goal is simple: make Remote Play easier to set up and nicer to use on Android and desktop. A PS5 or PS4 still does the actual game processing. BeyondConsole is a client-side project; it does not turn console games into native Android or PC games.

## What's in the repository

- `app/` — the first Android application shell.
- `docs/architecture.md` — the current architecture and implementation plan.
- `docs/chiaki-review.md` — notes from reviewing the existing Remote Play implementation.

The Android shell currently saves a console IP address locally and reports the device's network connection. It does not connect to a console or stream a game yet.

## What we're working towards

- Connect to a console the user owns or is allowed to access.
- Stream video and audio to Android and desktop devices.
- Send controller input back to the console.
- Make touch controls practical on phones and tablets.
- Show useful connection, latency and stream information.
- Handle changing network conditions without making setup complicated.

## Build the Android app

The Android project uses JDK 17, Gradle 8.9 and Android SDK 35.

From the repository root:

```sh
gradle :app:assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

GitHub Actions also builds the debug APK on pushes and pull requests to `main`.

## Remote Play implementation

We're evaluating existing Remote Play work before writing protocol code ourselves. [chiaki-ng](https://github.com/chiaki-ng/chiaki-ng) is the main project under review. It already has an Android client, native streaming code and desktop support.

Any code we reuse or build on will keep its original license and notices. chiaki-ng is licensed under AGPL-3.0, so its terms need to be followed if we integrate or modify it.

## Scope

BeyondConsole is not affiliated with or endorsed by Sony Interactive Entertainment. PlayStation, PS4 and PS5 are trademarks of their respective owners.

The project will not distribute game files, bypass console security, or provide access to consoles without the owner's authorization.
