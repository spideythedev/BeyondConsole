# Working on BeyondConsole

Keep changes small enough to review and test.

- Write Kotlin and native code in the style already used by the project or the platform.
- Prefer a direct implementation over adding layers that have no current use.
- Comments should explain a reason or a constraint. Do not narrate the code.
- Do not add screens that imply a feature works when its implementation is missing.
- Keep connection, registration and streaming states tied to real results from the Remote Play layer.
- Do not log PSN credentials, account tokens, registration keys or other secrets.
- Keep third-party copyright notices and licenses with any reused code.
- Update the README when the actual build or supported features change.
- Run the Android build before calling an Android change complete.
