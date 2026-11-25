# Contributing to BeyondConsole

Thanks for taking an interest in the project.

## Before you start

- Check the existing issues and pull requests before starting duplicate work.
- For larger changes, open an issue first so the direction can be discussed.
- Keep changes focused. Avoid mixing unrelated refactors with feature work.

## Development setup

1. Install Android Studio and a JDK 17 distribution.
2. Clone this repository.
3. Open the project in Android Studio and allow Gradle to sync.
4. Run the `:app:assembleDebug` Gradle task.
5. Install the debug APK on an Android 8.0 (API 26) or newer device.

## Pull requests

- Explain the problem and the behavior being changed.
- Include device / Android version details for Android-specific fixes.
- Add screenshots or a short recording for visible UI changes.
- Test the debug build before opening the pull request.
- Do not include signing keys, passwords, API tokens, or personal data.

By intentionally submitting a contribution for inclusion, you agree that it is
provided under the repository's Apache License 2.0, unless a separate written
agreement says otherwise.
