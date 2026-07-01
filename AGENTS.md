# AGENTS.md

Project-specific guidance for AI agents. See `CLAUDE.md` for architecture rules,
branch/commit conventions, and the mandatory design docs to read before coding.

## Cursor Cloud specific instructions

This is a single-module Android app (Gradle wrapper, Kotlin, Jetpack Compose).
Standard commands live in `README.md` / `CLAUDE.md`; the notes below only cover
things that are non-obvious in the Cursor Cloud VM.

### Environment
- The Android SDK is preinstalled in the VM snapshot at `~/android-sdk`
  (`cmdline-tools`, `platform-tools`, `platforms;android-36`, `build-tools;36.0.0`,
  `emulator`, `system-images;android-34;google_apis;x86_64`).
- `~/.bashrc` exports `ANDROID_HOME`/`ANDROID_SDK_ROOT` and adds the SDK tools to
  `PATH`. Non-interactive shells may not source it, but Gradle does not need the
  env var because the startup script writes `local.properties` (`sdk.dir=...`),
  which is how Gradle locates the SDK. Prefix ad-hoc `adb`/`sdkmanager`/`emulator`
  commands with `export ANDROID_HOME="$HOME/android-sdk"` if they are not on PATH.
- JDK 21 is used to run Gradle (see `gradle/gradle-daemon-jvm.properties`,
  `toolchainVersion=21`); the app itself targets JVM 11. Do not "fix" this mismatch.

### Build / lint / test (module `:app`)
- Build debug APK: `./gradlew assembleDebug` (output: `app/build/outputs/apk/debug/app-debug.apk`).
- Unit tests: `./gradlew testDebugUnitTest` (JUnit; currently scaffolding only).
- Lint: `./gradlew lintDebug`. NOTE: `main` currently has ~9 pre-existing lint
  errors (e.g. `NewApi` in `PortfolioImageReader.kt`), so `lintDebug` exits
  non-zero even on a clean checkout. Treat those as pre-existing unless your task
  is to fix them; the full report is at
  `app/build/intermediates/lint_intermediate_text_report/debug/lintReportDebug/lint-results-debug.txt`.
- Firebase: `app/google-services.json` is intentionally absent; the Google
  Services plugin is only applied when that file exists, so builds work without it.

### Running the app on the emulator (IMPORTANT — no hardware acceleration)
- This VM has **no `/dev/kvm`**, so the emulator runs in pure software (TCG) mode.
  A pre-created AVD `garapan_avd` exists (Pixel 6, API 34, x86_64, boosted to
  4 GB RAM / 3 cores in its `config.ini`).
- Start headless:
  `emulator -avd garapan_avd -no-accel -no-snapshot -no-window -no-audio -cores 3 -memory 4096 -gpu swiftshader_indirect -no-boot-anim`
- Cold boot takes **~10–15 minutes**. Poll `adb shell getprop sys.boot_completed`
  until it returns `1` before interacting.
- Software emulation is slow and the system throws frequent
  "System UI / Process system isn't responding" ANR dialogs, especially during
  cold-launch transitions. Mitigations that help a lot:
  - `adb shell settings put global hide_error_dialogs 1` (suppress ANR overlays)
  - disable animations: `settings put global {window,transition}_animation_scale 0`
    and `animator_duration_scale 0`
  - after cold-launching the app, let the system quiesce ~2–3 minutes before
    driving the UI; taps/`input text` are unreliable while the CPU is saturated.
  - `swiftshader_indirect` can occasionally stall to a black framebuffer under
    sustained load; if `screencap` returns all-black while the app is still the
    focused window, restart the emulator.
- Drive the UI with `adb shell input tap/text` and capture with
  `adb exec-out screencap -p`. Prefer `adb logcat` (the app uses an OkHttp logging
  interceptor) as authoritative proof that an action hit the backend, since screen
  rendering can lag.

### Backend
- The app is wired to a live backend at `BuildConfig.BASE_URL`
  (`https://api-garapan.up.railway.app/`); auth (login/register/2FA) makes real
  network calls. The README's "frontend-only / dummy data" note is stale for auth.
- For a device talking to a locally-run backend, the emulator loopback alias is
  `http://10.0.2.2:3000/`.
