# Dev Auth Bypass Workflow

This branch is a temporary development base for UI slicing and redesign work.
It exists so contributors can inspect the app quickly without going through
Google Sign-In on every emulator or physical device.

## Purpose

- Help UI work move faster while auth/API integration is still changing.
- Let each contributor branch from the same runnable app state.
- Keep the real `main` branch clean from any auth bypass behavior.

## Branch Flow

Start from this branch when working on UI tasks that need fast access to Home,
Search, Pesan, Profile, or role-specific screens.

```bash
git checkout chore/dev-auth-bypass
git pull origin chore/dev-auth-bypass
git checkout -b feat/your-feature-name
```

Examples:

```bash
git checkout -b feat/client-role-ui
git checkout -b feat/profile-portfolio-screen
git checkout -b feat/student-create-jasa-screen
```

## Rules

- Do not merge `chore/dev-auth-bypass` directly into `main`.
- Do not open a PR from this branch to `main`.
- Feature branches may branch from this branch for local UI work.
- Before opening a feature PR to `main`, remove or revert the auth bypass commit.
- Never include local IDE files, crash logs, or generated caches in commits.

Files that should not be committed:

```text
.idea/
.kotlin/
AGENTS.md
hs_err_pid*.log
replay_pid*.log
```

## Before PR to Main

Check the branch history:

```bash
git log --oneline --decorate --max-count=20
```

If the auth bypass commit exists in your feature branch, revert it before
opening the PR:

```bash
git revert <auth-bypass-commit-hash>
```

Then verify:

```bash
git status
.\gradlew.bat --no-daemon assembleDebug
```

The final feature PR should contain the UI/API work only, not the temporary
login skip behavior.

## AI Agent Checklist

Before editing code:

1. Read `.docs/requirements/mobile-requirements.md`.
2. Read `.docs/2026-04-27-garapan-design.md`.
3. Confirm the current branch is not `main`.
4. Keep the change scoped to the assigned feature.
5. Run `.\gradlew.bat --no-daemon assembleDebug` before committing when possible.

When publishing work:

1. Stage only feature files.
2. Do not stage `.idea`, `.kotlin`, local logs, or machine-specific files.
3. Use Conventional Commit format.
4. Push the feature branch.
5. Open a PR to `main`, not to `chore/dev-auth-bypass`, unless the team explicitly
   wants an intermediate review.
