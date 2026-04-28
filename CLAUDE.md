# GARAPAN Mobile — Rules for AI Agents & Team

## Read Before Doing Anything

Before writing a single line of code, read these two files in order:
1. `.docs/requirements/mobile-requirements.md` — screens, API endpoints, data models
2. `.docs/2026-04-27-garapan-design.md` — full system design and architecture decisions

Do not skip this. These files are the source of truth. If the code conflicts with them, the code is wrong.

---

## Project Overview

Android app for the GARAPAN IT Freelancer Marketplace — connecting Indonesian university students (Mahasiswa) as freelancers with clients (Klien). Built by a small team using AI agents.

**Repo:** `https://github.com/Garapan-by-kali/mobile-garapan.git`

---

## Stack

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
| Chat | Socket.io client |
| Push Notifications | Firebase FCM |
| Payments | Midtrans SDK |

---

## Architecture Rules — Non-Negotiable

```
data/
  remote/api/       ← Retrofit interfaces only
  remote/dto/       ← Request/response data classes only
  local/db/         ← Room database class
  local/dao/        ← Room DAO interfaces
  local/entity/     ← Room entity data classes
  repository/       ← Repository implementations (combine remote + local)

domain/
  model/            ← Pure Kotlin data classes — NO Android imports allowed here
  repository/       ← Repository interfaces (contracts)
  usecase/          ← One class per action (GetJasaListUseCase, CreatePesananUseCase)

presentation/
  screen/           ← One folder per screen, contains Screen.kt + ViewModel.kt
  navigation/       ← NavGraph, Routes, NavHost

di/                 ← Hilt modules only
```

**Hard rules:**
- No Android imports inside `domain/` — ever
- No direct Retrofit calls in ViewModels — always go through a UseCase
- No business logic in Composables — only UI and ViewModel calls
- One screen = one folder in `presentation/screen/`
- One action = one UseCase class
- Every screen has its own ViewModel — no shared ViewModels between screens
- Use `StateFlow` for UI state, `SharedFlow` for one-time events (navigation, toasts)
- Handle loading, success, and error states for every API call

**Data flow:**
```
Composable → ViewModel (StateFlow) → UseCase → Repository interface → Retrofit / Room
```

---

## Branch Strategy

```
main          ← protected, always production-ready
feat/*        ← new feature (e.g. feat/login-screen)
fix/*         ← bug fix (e.g. fix/token-refresh)
chore/*       ← maintenance (e.g. chore/update-dependencies)
```

- Branch from `main`, merge back to `main` via Pull Request
- Never push directly to `main`
- One feature = one branch = one PR
- Delete the branch after merging

---

## Commit Message Format

Use Conventional Commits:

```
feat(auth): add login screen with email and password
fix(chat): reconnect socket on network loss
chore(deps): update Hilt to 2.52
refactor(home): extract top worker list into separate composable
```

Format: `type(scope): short description`

Types: `feat` | `fix` | `chore` | `refactor` | `docs` | `test`

Scope: the screen or layer being changed (e.g. `auth`, `home`, `domain`, `data`)

---

## Pull Request Rules

- Every PR must have a clear title following commit format
- Describe what changed and why in the PR body
- At least 1 team member must review before merging
- The build must pass (`./gradlew assembleDebug`) before requesting review
- Link the PR to the relevant task/issue if one exists

---

## AI Agent — Specific Instructions

- Always read both `.docs` files before starting any task
- Never modify files outside the scope of your assigned task
- **Before writing any code**, create a feature branch: `git checkout -b feat/<screen-name>`
- **Never commit or push directly to `main`** — every change goes through a branch and a PR
- After pushing the branch, always create a PR using `gh pr create` — do not merge it yourself, wait for review
- Always run `./gradlew assembleDebug` before committing to verify the build
- Follow the commit message format above exactly
- If a task requires changes in multiple layers (data + domain + presentation), do them all in one branch but commit layer by layer
- Never add dependencies without checking if an alias already exists in `gradle/libs.versions.toml`
- Firebase is currently commented out in `app/build.gradle.kts` — do not uncomment until `google-services.json` is added
- `BASE_URL` for development (emulator): `http://10.0.2.2:3000/` — already set in `BuildConfig`
- Color palette is documented in `.docs/color-schema.md` — always refer to it before picking any color value
- All screens are UI-only (frontend-first) until API integration is explicitly requested — ViewModels hold UI state only, no UseCase or repository wiring

---

## What Must Not Change Without Team Discussion

- Architecture layer boundaries
- Tech stack (no replacing Hilt, Retrofit, Room without agreement)
- `minSdk` (currently 26)
- Package name (`com.app.garapan`)
- The folder structure defined above
