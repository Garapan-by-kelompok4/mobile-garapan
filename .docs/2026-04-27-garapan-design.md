# GARAPAN — System Design Spec

**Platform:** IT Freelancer Marketplace for Indonesian university students  
**Date:** 2026-04-27  
**Status:** Approved

---

## Final Tech Stack

| Layer | Tech |
|---|---|
| Mobile | Kotlin + Jetpack Compose + MVVM + Clean Architecture |
| Android HTTP | Retrofit + Kotlin Coroutines |
| Android DI | Hilt |
| Android Image Loading | Coil |
| Android Navigation | Jetpack Navigation Component |
| Android Local DB | Room |
| Backend API | NestJS (TypeScript) |
| Real-time | Socket.io (inside NestJS) |
| Admin Panel | React + Vite + shadcn/ui + Zustand + React Router |
| ORM | Prisma |
| Database | PostgreSQL |
| File Storage | Cloudinary |
| Backend Hosting | Railway |
| Admin Hosting | Vercel |
| Push Notifications | Firebase FCM |
| Payments | Midtrans |
| Repository | Monorepo |

---

## Repository Structure

```
garapan/
├── mobile/     # Android Kotlin project (open root in Android Studio)
├── backend/    # NestJS project (deployed to Railway)
└── admin/      # React + Vite project (deployed to Vercel)
```

---

## Section 1: System Architecture

```
[Android App - Kotlin/Jetpack Compose]
        |
        | HTTPS (REST)        | WebSocket
        v                     v
[NestJS Backend API] ←→ [Socket.io Gateway]
        |
        |— PostgreSQL (via Prisma)
        |— Cloudinary (file/image uploads)
        |— Firebase FCM (push notifications)
        |— Midtrans API (payments)

[React + Vite Admin Panel]
        |
        | HTTPS (REST)
        v
[NestJS Backend API]  ← same backend, admin-only routes protected by role guard
```

**Key decisions:**
- Single NestJS backend serves both the Android app and Admin Panel
- Socket.io is integrated inside NestJS as a Gateway — not a separate server
- Railway hosts the backend and PostgreSQL database
- Vercel hosts the admin panel (static deploy, free tier)
- Cloudinary handles all file and image storage (profile photos, portfolio images, gig covers, article images)

---

## Section 2: Android App Structure

Architecture: **MVVM + Clean Architecture** — 3 strict layers, each with one responsibility.

```
app/
├── data/
│   ├── remote/             # Retrofit API service interfaces + DTOs
│   ├── local/              # Room database, DAOs, entities
│   └── repository/         # Implementations — combines remote + local, single source of truth
│
├── domain/
│   ├── model/              # Pure data classes: User, Jasa, Pesanan, Chat, etc.
│   ├── repository/         # Repository interfaces (contracts for data layer)
│   └── usecase/            # One class per action: GetJasaListUseCase, CreatePesananUseCase, etc.
│
└── presentation/
    ├── screen/             # One folder per screen
    │   ├── auth/
    │   │   ├── LoginScreen.kt
    │   │   └── LoginViewModel.kt
    │   ├── home/
    │   │   ├── HomeScreen.kt
    │   │   └── HomeViewModel.kt
    │   ├── search/
    │   ├── jasa_detail/
    │   ├── project_detail/
    │   ├── checkout/
    │   ├── chat/
    │   ├── profile/
    │   ├── portfolio/
    │   ├── order_history/
    │   └── wallet/
    └── navigation/         # NavGraph, Routes, NavHost
```

**Data flow per screen:**
```
User action in Composable
→ ViewModel (holds UI state as StateFlow)
→ UseCase (pure business logic)
→ Repository interface
→ Retrofit (network) or Room (cache)
→ Result flows back up via StateFlow
→ Compose collects StateFlow and re-renders
```

**Key Android libraries:**

| Library | Purpose |
|---|---|
| Retrofit | HTTP calls to NestJS REST API |
| Kotlin Coroutines + Flow | Async operations, reactive streams |
| Hilt | Dependency injection — wires all layers automatically |
| Room | Local SQLite cache for offline support |
| Coil | Load images from Cloudinary URLs in Compose |
| Jetpack Navigation | Screen transitions, back stack, deep links |
| DataStore | Store auth token locally |

---

## Section 3: NestJS Backend Structure

```
src/
├── auth/               # JWT register, login, email verification, refresh token, 2FA
├── users/              # Mahasiswa profile, Klien profile, Admin profile
├── jasa/               # Gig catalog: create, list, search, filter, update, delete
├── project/            # Job board: create, list, search, filter, update, delete
├── pesanan/            # Order lifecycle: create, status updates, history
├── pembayaran/         # Escrow logic, Midtrans token creation, webhook handler
├── chat/               # Socket.io Gateway — real-time messaging per Pesanan
├── review/             # Create review after order completion, fetch ratings
├── portofolio/         # Upload to Cloudinary, CRUD portfolio items
├── artikel/            # Blog & Tips CMS — admin writes, public reads
├── laporan/            # Dispute/report submission, admin resolution
├── top-worker/         # Leaderboard calculation and ranking
├── notifications/      # Firebase FCM — send push to device tokens
├── admin/              # Admin-only: moderation actions, stats dashboard data
└── prisma/             # PrismaService (shared singleton database connection)
```

**Request flow:**
```
HTTP Request → Controller (validates input with class-validator)
             → Service (business logic)
             → PrismaService (PostgreSQL query)
             → Response
```

**Auth:** JWT Bearer tokens. Guards applied at controller level. Role-based: `@Roles(Role.ADMIN)` blocks non-admins from admin routes.

**WebSocket:** `ChatGateway` in `chat/` module. Client connects with JWT token. Messages saved to PostgreSQL and broadcast to the other participant in the room in real time.

**Midtrans webhook:** `POST /pembayaran/webhook` — NestJS verifies Midtrans signature, then updates `Pembayaran` and `Pesanan` status.

---

## Section 4: Admin Panel Structure

Stack: React + Vite + TypeScript + shadcn/ui + Zustand + React Router

```
src/
├── api/                    # All NestJS API calls, one file per domain
│   ├── auth.ts
│   ├── users.ts
│   ├── orders.ts
│   ├── disputes.ts
│   ├── articles.ts
│   └── chat.ts
│
├── components/
│   ├── ui/                 # shadcn/ui auto-generated components
│   ├── DataTable.tsx       # Reusable table (used by Users, Orders, Disputes, Content)
│   └── Sidebar.tsx         # Navigation sidebar
│
├── pages/
│   ├── Dashboard.tsx       # Stats: total users, active orders, revenue, disputes
│   ├── Users.tsx           # List, search, ban/unban users
│   ├── Orders.tsx          # Transaction & escrow monitoring
│   ├── Disputes.tsx        # Dispute & report handling, resolve actions
│   ├── Content.tsx         # Content moderation (flag/remove Jasa, Project posts)
│   ├── Articles.tsx        # Blog & Tips CMS — create, edit, publish articles
│   └── LiveChat.tsx        # Live chat with users
│
├── store/
│   └── authStore.ts        # Zustand: admin auth state (token, user)
│
└── router.tsx              # React Router: protected routes, redirect if not logged in
```

**2-day execution order:**
1. `npm create vite`, install shadcn/ui, React Router, Zustand (30 min)
2. Build `Sidebar` + layout shell with routing (1 hour)
3. Login page + auth store (1 hour)
4. Dashboard with stat cards (1 hour)
5. Users, Orders, Disputes pages — all reuse `DataTable`, just different columns (remaining time)

---

## Section 5: Database Schema

All tables managed by Prisma, deployed to PostgreSQL on Railway.

### Users

```
User
  id          String   @id @default(uuid())
  email       String   @unique
  password    String
  role        Role     (MAHASISWA | KLIEN | ADMIN)
  createdAt   DateTime @default(now())

Mahasiswa
  id            String  @id
  userId        String  @unique → User
  university    String
  skills        String[]
  bio           String
  walletBalance Decimal @default(0)
  rating        Float   @default(0)

Klien
  id            String  @id
  userId        String  @unique → User
  companyName   String?
  bio           String
  walletBalance Decimal @default(0)
```

### Marketplace

```
Kategori
  id    String @id
  name  String
  icon  String

Jasa
  id          String  @id
  mahasiswaId String  → Mahasiswa
  kategoriId  String  → Kategori
  title       String
  description String
  price       Decimal
  imageUrl    String
  status      JasaStatus (ACTIVE | INACTIVE)

Project
  id          String  @id
  klienId     String  → Klien
  kategoriId  String  → Kategori
  title       String
  description String
  budget      Decimal
  deadline    DateTime
  status      ProjectStatus (OPEN | CLOSED)

Portofolio
  id          String @id
  mahasiswaId String → Mahasiswa
  title       String
  description String
  imageUrl    String
  projectUrl  String?
```

### Transactions

```
Pesanan
  id          String       @id
  klienId     String       → Klien
  mahasiswaId String       → Mahasiswa
  jasaId      String?      → Jasa
  projectId   String?      → Project
  totalPrice  Decimal
  status      PesananStatus
  createdAt   DateTime     @default(now())

  PesananStatus: PENDING → PAID → IN_PROGRESS → DELIVERED → COMPLETED | DISPUTED

Pembayaran
  id             String          @id
  pesananId      String          @unique → Pesanan
  amount         Decimal
  method         PaymentMethod   (GOPAY | OVO | QRIS | VA_BCA | VA_MANDIRI)
  midtransToken  String
  status         PaymentStatus   (PENDING | SUCCESS | FAILED)
  paidAt         DateTime?
```

### Interactions

```
Chat
  id         String   @id
  pesananId  String   → Pesanan
  senderId   String   → User
  receiverId String   → User
  message    String
  createdAt  DateTime @default(now())

Review
  id         String   @id
  pesananId  String   @unique → Pesanan
  reviewerId String   → User
  rating     Int      (1–5)
  comment    String
  createdAt  DateTime @default(now())

Laporan
  id         String        @id
  reporterId String        → User
  targetId   String        → User
  reason     String
  status     LaporanStatus (PENDING | RESOLVED)
  createdAt  DateTime      @default(now())

LiveChatAdmin
  id        String   @id
  userId    String   → User
  adminId   String   → User
  message   String
  createdAt DateTime @default(now())
```

### Engagement

```
Artikel
  id          String   @id
  adminId     String   → User
  title       String
  content     String
  imageUrl    String?
  publishedAt DateTime?

TopWorker
  id          String @id
  mahasiswaId String → Mahasiswa
  score       Float
  period      String (e.g. "2026-04")
  rank        Int

LogAktivitas
  id        String   @id
  userId    String   → User
  action    String
  createdAt DateTime @default(now())
```

**Central table:** `Pesanan` — every other table (Pembayaran, Chat, Review, Laporan) references it.

---

## Section 6: Payment & Escrow Flow

```
1. CLIENT places order
   → POST /pesanan
   → Pesanan created, status = PENDING

2. CLIENT initiates payment
   → POST /pembayaran/create-token
   → NestJS calls Midtrans API, gets payment token
   → Token sent to Android app
   → App opens Midtrans payment UI (GoPay / OVO / QRIS / Virtual Account)

3. Midtrans confirms payment
   → POST /pembayaran/webhook (Midtrans calls this automatically)
   → NestJS verifies Midtrans signature
   → Pembayaran.status = SUCCESS
   → Funds held in escrow (NOT yet in Mahasiswa wallet)
   → Pesanan.status = IN_PROGRESS
   → Firebase FCM notifies Mahasiswa "New order started"

4. MAHASISWA completes and marks delivered
   → PATCH /pesanan/:id/deliver
   → Pesanan.status = DELIVERED
   → Firebase FCM notifies Klien "Work delivered, please review"

5a. CLIENT accepts delivery
    → PATCH /pesanan/:id/complete
    → Pesanan.status = COMPLETED
    → Mahasiswa.walletBalance += Pesanan.totalPrice
    → Firebase FCM notifies Mahasiswa "Payment released to your wallet"

5b. CLIENT opens dispute
    → POST /laporan (with pesananId)
    → Pesanan.status = DISPUTED
    → Admin sees it in Admin Panel Disputes page
    → Admin resolves:
      - Release funds → Mahasiswa.walletBalance += totalPrice
      - Refund → Klien.walletBalance += totalPrice
```

**Implementation notes:**
- npm package: `midtrans-client`
- Wallet balance is stored as `Decimal` in the DB — no real bank withdrawal in v1
- Midtrans webhook URL must be set in Railway env and configured in Midtrans dashboard

---

## Section 7: Deployment & Project Setup

### Local Development

| Service | How to run |
|---|---|
| PostgreSQL | Install directly on machine (not Docker) |
| NestJS backend | `npm run start:dev` (hot reload) |
| React admin | `npm run dev` (hot reload) |
| Android app | Android Studio — emulator or physical device |

Android points to `http://10.0.2.2:3000` (emulator localhost) or your machine's IP for a physical device.

### Production

| Service | Platform | How |
|---|---|---|
| Backend API | Railway | Connect GitHub repo → auto-deploy on push to main |
| PostgreSQL | Railway | Add PostgreSQL plugin, Railway provides DATABASE_URL |
| Admin Panel | Vercel | Connect GitHub repo → auto-deploy on push to main |
| Android | Play Store | Build signed `.aab` in Android Studio → upload to Play Console |

### NestJS Environment Variables (.env)

```
DATABASE_URL=postgresql://...      # From Railway dashboard
JWT_SECRET=your-random-secret
CLOUDINARY_URL=cloudinary://...    # From Cloudinary dashboard
MIDTRANS_SERVER_KEY=...            # From Midtrans dashboard
MIDTRANS_IS_PRODUCTION=false       # true when going live
FIREBASE_PROJECT_ID=...
FIREBASE_PRIVATE_KEY=...
FIREBASE_CLIENT_EMAIL=...
```

### Play Store Publishing Checklist
- Build release AAB: `Build → Generate Signed Bundle/APK → Android App Bundle`
- Target SDK minimum: API 26 (Android 8.0) recommended
- Add privacy policy URL (required by Google)
- Use internal testing track → closed → open → production

---

## Actors & Features Summary

| Actor | Key Features |
|---|---|
| Mahasiswa | Register, create Jasa/Gig, manage orders, upload portfolio, receive payments, chat, ratings |
| Klien | Register, browse Jasa, post Project, place orders, escrow payment, review, dispute |
| Admin | Moderate content, manage users, resolve disputes, manage escrow, publish articles, live chat |
