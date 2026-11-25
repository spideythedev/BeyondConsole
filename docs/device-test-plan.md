# Android device test plan

This plan is for validating a real Remote Play session, not just confirming that the APK installs.

## Test device and console

Record these before testing:

- Android device model and Android version.
- PlayStation model (PS5 or PS4) and system software version.
- Network topology: same Wi-Fi/LAN or remote network.
- Router band and signal quality.
- App build commit and APK version.

Use a console and PSN account that the tester owns or is authorized to access. Never paste account passwords, registration tokens or console PINs into issue reports or logs.

## Console preparation

### PS5

1. Open **Settings → System → Remote Play**.
2. Enable **Enable Remote Play**.
3. For wake-from-rest testing, open **Settings → System → Power Saving → Features Available in Rest Mode**.
4. Enable **Stay Connected to the Internet** and **Enable Turning On PS5 from Network**.

### PS4

1. Open **Settings → Remote Play Connection Settings**.
2. Enable **Enable Remote Play**.
3. For wake-from-rest testing, open **Settings → Power Save Settings → Set Features Available in Rest Mode**.
4. Enable **Stay Connected to the Internet** and **Enable Turning On PS4 from Network**.

## Test sequence

Run these in order. Do not start WAN testing until local-network streaming is stable.

1. **Install and launch** — APK installs, opens and survives a force-stop/relaunch.
2. **Local network** — phone and console are on the same LAN; app reports the actual network transport.
3. **Discovery/manual address** — console can be found or entered without assuming an IP remains permanent.
4. **Registration** — complete the console's official Remote Play device-link process. Confirm invalid or expired codes fail clearly.
5. **First frame** — start a session and record time from tapping Connect to the first decoded frame.
6. **Video** — check orientation, scaling, frame pacing, dropped frames and recovery after a brief network interruption.
7. **Audio** — check output routing, synchronization, mute/unmute and recovery after audio focus changes.
8. **Input** — test touch controls, Android gamepad buttons, both sticks, triggers, D-pad, PS button and touchpad gestures where supported.
9. **Lifecycle** — background the app, lock/unlock the phone, rotate the display and return to the session.
10. **Disconnect/reconnect** — disconnect cleanly, reconnect and confirm no stale session or duplicate audio remains.
11. **Rest mode** — test wake-on-LAN only after normal local streaming works.
12. **Remote network** — test outside the home LAN only after local streaming passes.

## Results to capture

For each test, record pass/fail, device/console details and a short sanitized log excerpt. Measure:

- Time to first frame.
- Approximate stream frame rate and dropped frames.
- Input response and any missed inputs.
- Audio interruptions or sync drift.
- Session stability and reconnect time.
- Battery drain and device temperature over a 20–30 minute session.

Do not include PSN credentials, registration keys, tokens, private IPs or personally identifying account details in public reports.

## Release gate

A build is not considered Remote Play-ready merely because Gradle produced an APK. At minimum, registration, video, audio, controller input, clean disconnect and a sustained local-LAN session must pass on a physical Android device. Record any untested console generation or device limitation instead of claiming broad compatibility.
