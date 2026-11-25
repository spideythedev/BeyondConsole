# Remote Play integration roadmap

This roadmap is based on the pinned chiaki-ng source at `43728af2e639fecb8a7af1cd68c29820a40de973` and its successful Android debug build in GitHub Actions run [45535017493](https://github.com/spideythedev/BeyondConsole/actions/runs/45535017493).

## What the reference build proves

The upstream Android project can be configured and compiled in CI with JDK 17, Android SDK 35, NDK 28.2.13676358, CMake 3.22.1, recursive submodules and the CI-only curl/libpsl adjustment.

It proves that a reproducible Android APK can be produced from the pinned source. It does not prove that the APK installs or streams correctly on a physical phone, nor that BeyondConsole has Remote Play functionality.

## Runtime pieces identified in upstream

The Android implementation is not a standalone Kotlin library:

- `DiscoveryManager` owns the native discovery service and publishes discovered hosts.
- `MainViewModel` and the common storage classes coordinate saved/manual hosts, registered hosts and preferences.
- The registration activities and view models handle console registration and registration keys.
- `Chiaki.kt` declares the Kotlin/JNI API and loads `chiaki-jni`. It defines connection data, video profiles, controller state, discovery, registration and session wrappers.
- `StreamSession` owns the native `Session`, forwards controller state, exposes connection events and attaches the decoder to an Android surface.
- `StreamActivity` and `StreamViewModel` own the stream screen and its lifecycle.
- `android/app/src/main/cpp` implements JNI glue, video decoder integration and audio output; the root native project builds chiaki-lib and its dependencies.

The important dependency chain is:

```
BeyondConsole UI
  -> host/profile and registration state
  -> ConnectInfo + video profile
  -> Kotlin Session wrapper
  -> JNI (libchiaki-jni)
  -> chiaki-lib / protocol / media
  -> Android Surface + audio output
```

A working session therefore needs much more than copying a single streaming class.

## Recommended engineering sequence

### Gate 1 — validate the reference app on hardware

Install the artifact from the successful Actions run on a physical Android device. Test console discovery, manual host entry, registration, first frame, audio, controller input, disconnect and reconnect. Use the checklist in [device-test-plan.md](device-test-plan.md).

Record device model, Android version, console generation, network topology and sanitized logs. Do not publish registration keys, PSN tokens or credentials.

### Gate 2 — choose the source strategy

There are three realistic strategies:

1. **Maintain a clearly attributed chiaki-ng-based Android fork.** This is the shortest technical path because it retains the working app, native build, resources, storage and lifecycle together.
2. **Integrate the upstream Android and native layers behind a BeyondConsole UI.** This may provide a more distinct product architecture, but it is still a derivative integration and must be reviewed under the upstream license. Keep the complete native dependency/build chain; do not cherry-pick isolated Kotlin files.
3. **Implement a genuinely independent Remote Play client.** This avoids importing upstream implementation code, but requires substantial protocol, registration, codec, audio, input and compatibility engineering. It is not a shortcut.

chiaki-ng is AGPL-3.0-only with an additional OpenSSL permission. Do not assume that a wrapper, renamed package or separate module removes the license obligations. Before selecting strategy 1 or 2 for distribution, review the complete license and obtain appropriate legal advice for the intended distribution model. This roadmap does not change BeyondConsole's project-wide license.

### Gate 3 — productize after the strategy is approved

Only after the source strategy is settled:

- Establish BeyondConsole application identity, package, icons, name and user-facing design.
- Keep upstream attribution, copyright and license notices where required.
- Define host profiles and secure storage for registration material.
- Keep registration, session lifecycle, media and controller input as explicit boundaries.
- Add actionable connection errors and diagnostics without logging secrets.
- Keep Android and desktop release plans separate until both have real device/OS test coverage.

### Gate 4 — release criteria

A release must not claim Remote Play support until physical-device tests pass for registration, video, audio, controller input, lifecycle handling, clean disconnect/reconnect and a sustained local-LAN session. State PS4/PS5 and Android compatibility only for combinations actually tested.
