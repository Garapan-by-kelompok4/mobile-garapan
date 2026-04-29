# GARAPAN Mobile

Android app for the GARAPAN IT Freelancer Marketplace — connecting Indonesian university students (Mahasiswa) as freelancers with clients (Klien).

> **Status: Frontend-only (UI prototype).** All screens display dummy data. API integration, authentication, real-time chat, and payment are not yet wired up.

---

## Tech Stack

| Layer | Tech | Status |
|---|---|---|
| Language | Kotlin | ✅ Active |
| UI | Jetpack Compose | ✅ Active |
| Architecture | MVVM + Clean Architecture | ✅ Active |
| DI | Hilt | ✅ Active |
| Navigation | Jetpack Navigation Compose | ✅ Active |
| HTTP | Retrofit + OkHttp + Coroutines | 🔜 Planned |
| Local DB | Room | 🔜 Planned |
| Image | Coil 3.x | 🔜 Planned |
| Token Storage | DataStore | 🔜 Planned |
| Chat | Socket.io | 🔜 Planned |
| Push Notifications | Firebase FCM | 🔜 Planned |
| Payments | Midtrans SDK | 🔜 Planned |

---

## Screens Implemented

| Screen | Description |
|---|---|
| Splash | Entry point |
| Login / Register | Auth forms (UI only) |
| Setup Account | Role selection (Mahasiswa / Klien) |
| Home | Project list, services, top workers, blog |
| Search | Search bar and filters |
| Project Detail | Project info, apply button |
| Jasa Detail | Service info, chat & order button |
| Chat | Chat room with file attachment and order confirmation card |
| Checkout | Order summary and payment method selection |
| Blog Detail | Full article with body blocks and recommendations |
| Pesan | Conversation list including admin support |
| Post Project | Project posting form |
| Profile | User profile with services management |
| Edit Profile | Profile editing form |
| Order History | Past orders list |
| Security | Password and security settings |

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

### Run

Build and install directly from Android Studio, or via terminal:

```bash
./gradlew assembleDebug
```

> No backend connection is required to run the app — all data is hardcoded dummy data.

### Firebase Setup (not required yet)

`google-services.json` is not committed to this repo. Firebase is currently disabled. When integration begins:

1. Ask the team lead to add you to the Firebase project
2. Go to [Firebase Console](https://console.firebase.google.com) → Project Settings → Your apps → Android app
3. Download `google-services.json` and place it in the `app/` folder
4. Uncomment the Firebase lines in `app/build.gradle.kts` and root `build.gradle.kts`

### Backend URL

`BuildConfig.BASE_URL` is set to `http://10.0.2.2:3000/` (emulator localhost). Not active until API integration begins. For a physical device, update the URL in `app/build.gradle.kts` to your machine's local IP.

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
