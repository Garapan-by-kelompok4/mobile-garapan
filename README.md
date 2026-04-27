# GARAPAN Mobile

Android app for the GARAPAN IT Freelancer Marketplace — connecting Indonesian university students (Mahasiswa) as freelancers with clients (Klien).

---

## Tech Stack

| Layer | Tech |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| HTTP | Retrofit + OkHttp + Coroutines |
| Local DB | Room |
| Image | Coil 3.x |
| Navigation | Jetpack Navigation Compose |
| Token Storage | DataStore |
| Chat | Socket.io |
| Push Notifications | Firebase FCM |
| Payments | Midtrans SDK |

---

## Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- JDK 11
- Android device or emulator (API 26+)

### Clone & Setup

```bash
git clone https://github.com/Garapan-by-kali/mobile-garapan.git
cd mobile-garapan
```

Open the project root in Android Studio and let Gradle sync finish.

### Firebase Setup (Required for push notifications)

`google-services.json` is not committed to this repo. To get it:

1. Ask the team lead to add you to the Firebase project
2. Go to [Firebase Console](https://console.firebase.google.com) → Project Settings → Your apps → Android app
3. Download `google-services.json`
4. Place it in the `app/` folder
5. Uncomment the Firebase lines in `app/build.gradle.kts` and root `build.gradle.kts`

> The app builds and runs without this file — Firebase is disabled until the file is added.

### Backend URL

The app points to `http://10.0.2.2:3000/` (emulator localhost) by default via `BuildConfig.BASE_URL`. For a physical device, update the URL in `app/build.gradle.kts` to your machine's local IP.

---

## Project Structure

```
app/src/main/java/com/app/garapan/
├── data/
│   ├── remote/api/       ← Retrofit API interfaces
│   ├── remote/dto/       ← Request / response DTOs
│   ├── local/db/         ← Room database
│   ├── local/dao/        ← Room DAOs
│   ├── local/entity/     ← Room entities
│   └── repository/       ← Repository implementations
├── domain/
│   ├── model/            ← Pure Kotlin data classes
│   ├── repository/       ← Repository interfaces
│   └── usecase/          ← One UseCase per action
├── presentation/
│   ├── screen/           ← One folder per screen
│   └── navigation/       ← NavGraph, Routes, NavHost
└── di/                   ← Hilt modules
```

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for branch strategy, commit format, and PR rules.

See [CLAUDE.md](CLAUDE.md) for AI agent instructions and architecture rules.
