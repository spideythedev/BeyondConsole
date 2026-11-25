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
- `docs/device-test-plan.md` defines the physical-device validation required before calling streaming ready.
- `docs/integration-roadmap.md` records the upstream integration findings and release gates.

## Build the Android shell

The project uses JDK 17, Gradle 8.9 and Android SDK 35.

Run this from the repository root:

```sh
gradle :app:assembleDebug
```

Gradle writes the APK to `app/build/outputs/apk/debug/app-debug.apk`.

GitHub Actions builds this app when Android source or build files change. The workflow uploads the debug APK as an artifact.

## Build the Remote Play reference

The upstream Android client has its own Gradle and native CMake build. To check it, open the Actions tab and run **Upstream Android build**. That workflow checks out chiaki-ng at a pinned commit, initializes its submodules and attempts to assemble its Android APK.

The pinned upstream Android source now builds successfully in GitHub Actions and its APK is uploaded as the `chiaki-ng-android-reference` artifact. This confirms the CI build path, not physical-device streaming. Install and validate that APK using [docs/device-test-plan.md](docs/device-test-plan.md). The source review and integration sequence are in [docs/chiaki-review.md](docs/chiaki-review.md) and [docs/integration-roadmap.md](docs/integration-roadmap.md).

## Remote Play implementation

We are reviewing [chiaki-ng](https://github.com/chiaki-ng/chiaki-ng) before writing any of the Remote Play protocol ourselves. Its repository already has an Android client, native streaming code and desktop support.

chiaki-ng is licensed under AGPL-3.0 with an additional permission for linking with OpenSSL. Any code taken from it must keep the original notices and follow the license. We will settle the integration and distribution details before shipping a build based on that code.

## Scope

BeyondConsole is an independent project. It is not affiliated with or endorsed by Sony Interactive Entertainment. PlayStation, PS4 and PS5 are trademarks of their respective owners.

BeyondConsole will not distribute game files, bypass console security or connect to consoles without the owner's authorization.
