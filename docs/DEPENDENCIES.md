# Dependencies and build tools

## Application runtime

The current Android app has **no third-party runtime library dependencies**.
It uses Android platform APIs and Kotlin / Java standard-library functionality.

| Area | Technology |
|---|---|
| Language | Kotlin |
| Android UI | Android framework Views and XML layouts |
| Local profile storage | Android SharedPreferences |
| JSON handling | Android `org.json` |
| Network state | Android ConnectivityManager |
| Local discovery | Java networking APIs (TCP socket checks) |
| Build system | Gradle |
| Android Gradle Plugin | 8.7.3 |
| Kotlin Gradle Plugin | 2.0.21 |
| Java toolchain | JDK 17 |
| Compile SDK | Android API 35 |
| Minimum Android | API 26 |

## Repositories

Gradle resolves build plugins from Google, Maven Central, and the Gradle Plugin
Portal. Project dependency resolution uses Google and Maven Central.

## Updating dependencies

When adding a library:
1. Confirm that it is needed and actively maintained.
2. Review its license and Android compatibility.
3. Add it to the appropriate Gradle configuration.
4. Update this document and the relevant third-party notices.
5. Keep the dependency footprint small.

Build plugin versions are build-time dependencies; they are not bundled as
application runtime libraries.
