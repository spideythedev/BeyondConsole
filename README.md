# BeyondConsole

Project website: https://spideythedev.github.io/BeyondConsole/

<p align="center">
  <img src="assets/brand/logo.svg" alt="BeyondConsole logo" width="180" />
</p>

BeyondConsole is a project to make PlayStation Remote Play easier to use on Android and desktop devices.

The PS5 or PS4 runs the game. BeyondConsole handles the remote session: video, audio and controller input. It will work with consoles the player owns or is allowed to access.

## Current state

The repository license is now AGPL-3.0 with the additional OpenSSL permission used by the pinned Chiaki-ng reference. See [LICENSE](LICENSE) and [NOTICE](NOTICE). This license change prepares the project for a Chiaki-ng-based implementation; it does not mean the current BeyondConsole app shell has gained streaming.

The Android app uses a dark, controller-inspired interface with a remote-play hero panel, thumbstick/action-button artwork, compact network status, and focused console controls. It can save multiple named console profiles (name and local IP address), select one, and remove saved entries. Profiles stay on the device. It shows the current network transport and can scan the connected Wi-Fi subnet for hosts accepting TCP connections on port 9295, the PlayStation Remote Play control port. A port match is only a candidate; it does not identify or verify a PlayStation. Console registration, session negotiation, video, audio and controller streaming are not implemented yet. Android 8+ adaptive launcher icons and the Android 12+ system splash screen use the BeyondConsole brand mark.

The desktop app has not been started.

## Documentation

- [User guide](docs/USER-GUIDE.md)
- [Frequently asked questions](docs/FAQ.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Dependencies and build tools](docs/DEPENDENCIES.md)
- [Contributing](CONTRIBUTING.md)
- [Security](SECURITY.md)
- [Support](SUPPORT.md)
- [License](LICENSE) — GNU Affero General Public License 3.0 with OpenSSL permission

## Repository

- `app/` contains the Android project, adaptive launcher icon and splash branding.
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

GitHub Actions builds this app when Android source or build files change. The workflow uploads the debug APK as an artifact. The Android workflow builds the debug APK and uploads it as `beyondconsole-android-debug`. Check the [Actions tab](https://github.com/spideythedev/BeyondConsole/actions) for the latest result.

## Build the Remote Play reference

The upstream Android client has its own Gradle and native CMake build. To check it, open the Actions tab and run **Upstream Android build**. That workflow checks out chiaki-ng at a pinned commit, initializes its submodules and attempts to assemble its Android APK.

The pinned upstream Android source now builds successfully in [Actions run 45535017493](https://github.com/spideythedev/BeyondConsole/actions/runs/45535017493); its APK is uploaded as the `chiaki-ng-android-reference` artifact. This confirms the CI build path, not physical-device streaming. Install and validate that APK using [docs/device-test-plan.md](docs/device-test-plan.md). The source review and integration sequence are in [docs/chiaki-review.md](docs/chiaki-review.md) and [docs/integration-roadmap.md](docs/integration-roadmap.md).

## Remote Play implementation

We are reviewing [chiaki-ng](https://github.com/chiaki-ng/chiaki-ng) before writing any of the Remote Play protocol ourselves. Its repository already has an Android client, native streaming code and desktop support.

chiaki-ng is licensed under AGPL-3.0 with an additional permission for linking with OpenSSL. Any code taken from it must keep the original notices and follow the license. We will settle the integration and distribution details before shipping a build based on that code.

## License and copyright

BeyondConsole is licensed under the [Apache License, Version 2.0](LICENSE). Copyright © 2026 Fahad Malik and BeyondConsole contributors. See [NOTICE](NOTICE) for project attribution and trademark information.

## Scope

BeyondConsole is an independent project. It is not affiliated with or endorsed by Sony Interactive Entertainment. PlayStation, PS4 and PS5 are trademarks of their respective owners.

BeyondConsole will not distribute game files, bypass console security or connect to consoles without the owner's authorization.

## Brand assets

The project logo lives in `assets/brand/`:

- `logo.svg` — primary symbol mark.
- `icon.svg` — standalone app/icon mark.
- `wordmark.svg` — horizontal BeyondConsole wordmark.

The SVG files are resolution-independent and can be used in the app, website and project materials.
