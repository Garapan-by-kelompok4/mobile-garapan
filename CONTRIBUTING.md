# Contributing to GARAPAN Mobile

This project is built by a small team using AI agents. Read this before contributing.

---

## First Time Setup

```bash
git clone https://github.com/Garapan-by-kali/mobile-garapan.git
cd mobile-garapan
```

Open the project root in Android Studio. Let Gradle sync finish before doing anything else.

### Required: `google-services.json`

This file is **not committed to the repo** (it's in `.gitignore`). You must get it before the app can use Firebase (push notifications).

1. Ask the team lead to add you to the Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
2. Open the project → Project Settings → Your apps → Android app
3. Download `google-services.json`
4. Place it in the `app/` folder: `mobile-garapan/app/google-services.json`
5. Sync/rebuild the project. The Google Services plugin is applied automatically when the file exists.

> Until you do this, the app still builds and runs — Firebase Messaging compiles, but push token registration is skipped because Firebase cannot initialize without the config file.

### Required reading before writing any code:
- `.docs/requirements/mobile-requirements.md`
- `.docs/2026-04-27-garapan-design.md`

---

## Branch Strategy

| Branch | Purpose |
|---|---|
| `main` | Protected — production ready, PR only |
| `feat/*` | New feature (e.g. `feat/login-screen`) |
| `fix/*` | Bug fix (e.g. `fix/token-refresh`) |
| `chore/*` | Maintenance (e.g. `chore/update-deps`) |

```bash
# Start a new feature
git checkout main
git pull
git checkout -b feat/your-feature-name
```

Never push directly to `main`.

---

## Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
feat(auth): add login screen with email and password
fix(chat): reconnect socket on network loss
chore(deps): update Hilt to 2.52
refactor(home): extract top worker list into composable
```

Format: `type(scope): short description`

**Types:** `feat` | `fix` | `chore` | `refactor` | `docs` | `test`

**Scope:** the screen or layer (`auth`, `home`, `domain`, `data`, etc.)

---

## Pull Requests

1. Make sure the build passes before opening a PR:
   ```bash
   ./gradlew assembleDebug
   ```
2. PR title must follow commit message format
3. Describe what changed and why in the PR body
4. At least **1 team member** must approve before merging
5. Delete the branch after merging

---

## Architecture

This project uses MVVM + Clean Architecture. See `CLAUDE.md` for the full rules.

Short version:
- **`data/`** — API calls, Room DB, repository implementations
- **`domain/`** — pure Kotlin models, repository interfaces, use cases. No Android imports.
- **`presentation/`** — Compose screens + ViewModels. No business logic.

---

## Adding Dependencies

Always use the version catalog. Add to `gradle/libs.versions.toml` first, then reference via `libs.X` in `app/build.gradle.kts`. Never hardcode group/artifact/version strings directly in the build file.

---

## Questions

Open an issue or ask in the team chat before making large changes to the architecture or tech stack.
