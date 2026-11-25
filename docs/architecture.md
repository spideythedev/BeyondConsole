# Architecture notes

These are working notes, not a description of code that already exists.

## First release

Start with one reliable Remote Play path. The first milestone is a small Android client that can discover or register an authorized console, connect, display the stream and pass controller input. Desktop support follows once the shared parts are understood.

## Existing work

Before implementing a protocol, compare the available options and their licenses. chiaki-ng is the main candidate to evaluate. It already has Android and desktop support, so we should first find out whether its existing client or core can be used cleanly.

Do not copy code from another project without keeping its license, copyright notices and required source disclosures. chiaki-ng is AGPL-3.0. If BeyondConsole distributes a modified or combined version, get the licensing details right before release.

## Proposed boundaries

- **Client UI:** console list, connection flow, session controls and settings.
- **Remote Play layer:** registration, connection lifecycle and protocol integration.
- **Media:** video decode, rendering and audio playback.
- **Input:** controller mapping and optional touch controls.
- **Diagnostics:** connection state, measured latency, frame rate and dropped-frame information.
- **Platform layer:** Android and desktop-specific APIs such as hardware decoding, audio and controller access.

These are boundaries to guide the first implementation. They do not need to become separate libraries until the code gives us a reason.

## Things to measure

- Time from selecting a console to seeing the first frame.
- End-to-end input response where it can be measured reliably.
- Video frame rate and dropped frames.
- Audio interruptions.
- Stream stability on local Wi-Fi and remote networks.
- Battery and thermal behaviour on Android.

## Not in scope

- Running PS5 game binaries natively on Android or ordinary PCs.
- Downloading or distributing commercial game files.
- Circumventing console security or account restrictions.
- Claiming compatibility before it has been tested.

## Current state

No application code exists yet. The next step is a short technical spike: build and run the candidate Remote Play client/core on the target platforms, document what can be reused, then choose the smallest maintainable integration.
