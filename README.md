# GARAPAN Mobile

Android app for the GARAPAN IT Freelancer Marketplace — connecting Indonesian university students (Mahasiswa) as freelancers with clients (Klien).

### Download

APK and related files are available on Google Drive:

**[Download from Google Drive](https://drive.google.com/drive/folders/1Q2IHhKweH_PniVr7FFck-15lqt12Ms2A?usp=drive_link)**

---

## Tech Stack

| Layer              | Tech                                  |
| ------------------ | ------------------------------------- |
| Language           | Kotlin                                |
| UI                 | Jetpack Compose (Material 3)          |
| Architecture       | MVVM + Clean Architecture             |
| DI                 | Hilt                                  |
| HTTP               | Retrofit + OkHttp + Gson + Coroutines |
| Navigation         | Jetpack Navigation Compose            |
| Auth               | Credential Manager + Google Sign-In   |
| Token Storage      | DataStore                             |
| Image              | Coil 3.x                              |
| Push Notifications | Firebase FCM                          |
| Payments           | Midtrans Snap (Custom Tabs)           |

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

For Firebase and environment configuration, see [CONTRIBUTING.md](CONTRIBUTING.md).

### Run

```bash
./gradlew assembleDebug
```

### Test

```bash
./gradlew test
```

---

## Project Structure

```
app/src/main/java/com/app/garapan/
├── data/
│   ├── auth/             ← Google Sign-In client
│   ├── remote/
│   │   ├── api/          ← Retrofit API interfaces
│   │   ├── dto/          ← Request / response DTOs
│   │   ├── error/        ← API error mapping
│   │   └── interceptor/  ← Auth headers & token refresh
│   ├── local/            ← DataStore token storage (+ Room planned below)
│   │   ├── db/           ← Room database (planned)
│   │   ├── dao/          ← Room DAOs (planned)
│   │   └── entity/       ← Room entities (planned)
│   ├── mapper/           ← DTO ↔ domain mappers
│   ├── repository/       ← Repository implementations
│   └── util/             ← File/image readers for uploads
├── domain/
│   ├── common/           ← Shared types (e.g. Resource)
│   ├── model/            ← Pure Kotlin data classes
│   ├── repository/       ← Repository interfaces
│   ├── usecase/          ← One UseCase per action
│   └── validation/       ← Input validation rules
├── presentation/
│   ├── components/       ← Reusable Compose UI
│   ├── navigation/       ← NavGraph, Routes, NavHost
│   ├── notification/     ← FCM token registration
│   ├── payment/          ← Midtrans Snap launcher
│   ├── screen/           ← One folder per screen
│   └── util/             ← UI helpers & formatters
├── notification/         ← FCM service & notification routing
├── ui/theme/             ← Colors, typography, theme
├── di/                   ← Hilt modules
├── GarapanApplication.kt
└── MainActivity.kt
```

---

## Documentation

| File | Contents |
|---|---|
| [CONTRIBUTING.md](CONTRIBUTING.md) | Setup, branch strategy, commit format, PR rules |
| [CLAUDE.md](CLAUDE.md) | Architecture rules and AI agent instructions |
| [`.docs/requirements/mobile-requirements.md`](.docs/requirements/mobile-requirements.md) | Screens, API endpoints, data models |
| [`.docs/2026-04-27-garapan-design.md`](.docs/2026-04-27-garapan-design.md) | System design and architecture decisions |
