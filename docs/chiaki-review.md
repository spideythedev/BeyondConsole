# Remote Play implementation review

Reviewed against the chiaki-ng repository on 25 November 2026.

Repository: https://github.com/chiaki-ng/chiaki-ng

## What is already there

The project has a complete Android application, not just a protocol library. Its Android target includes:

- Kotlin activities and view models for console discovery, registration, settings and streaming.
- A C JNI layer around chiaki-lib.
- Android MediaCodec-based video decoding.
- Native audio output using Oboe.
- Touch controls and controller input handling.
- A Gradle Android application module that builds the native library with CMake.

The Android app currently uses the package `com.metallic.chiaki`, Kotlin/JVM 17, compile SDK 35, NDK 28.2 and CMake 3.22.1. Its native build config disables the desktop GUI and CLI and enables the Android target.

## License

The upstream repository declares AGPL-3.0. Its source files and notices must remain intact when reused. A distributed modified or combined work needs to comply with the applicable AGPL terms, including the corresponding source-code obligations. Review the exact distribution model before publishing builds.

## Decision

Do not reimplement Remote Play registration, transport, decoding or controller protocols until the existing implementation has been evaluated in a real Android build.

The immediate next engineering task is to create a reproducible build of the upstream Android client, then test whether its native core and Android session layer can be maintained as a fork or integrated behind a BeyondConsole-specific app shell.

The new BeyondConsole Android module is currently only a small project shell. It saves a console IP profile and reports Android network connectivity. It does not discover consoles, register a PSN account, establish a Remote Play session or stream media.
