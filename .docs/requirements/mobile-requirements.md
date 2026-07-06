# GARAPAN Mobile — Requirements

> Full system spec: `../../docs/superpowers/specs/2026-04-27-garapan-design.md`

---

## Overview

Android app for the GARAPAN IT Freelancer Marketplace. Serves two user types:
- **Mahasiswa** (student freelancer) — posts services, receives orders, gets paid
- **Klien** (client) — browses services, posts projects, pays via escrow

---

## Tech Stack

| Layer | Tech |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Architecture | MVVM + Clean Architecture |
| HTTP | Retrofit + Kotlin Coroutines |
| DI | Hilt |
| Local DB | Room |
| Image Loading | Coil |
| Navigation | Jetpack Navigation Component |
| Auth Token Storage | DataStore |
| Real-time Chat | WebSocket (Socket.io client) |
| Push Notifications | Firebase FCM |
| Payments | Midtrans SDK (opens payment UI in-app) |

---

## Project Structure

```
app/src/main/java/com/app/garapan/
├── data/
│   ├── remote/             # Retrofit API interfaces + request/response DTOs
│   ├── local/              # Room DB, DAOs, entities (offline cache)
│   └── repository/         # Repository implementations (remote + local combined)
├── domain/
│   ├── model/              # Pure Kotlin data classes (no Android imports)
│   ├── repository/         # Repository interfaces
│   └── usecase/            # One UseCase per action
└── presentation/
    ├── screen/             # One folder per screen
    └── navigation/         # NavGraph, Routes, NavHost
```

**Rule:** No Android imports in `domain/`. No direct API calls in ViewModels — always go through UseCases.

---

## Screens

### Auth
| Screen | Description |
|---|---|
| SplashScreen | App entry, checks token → routes to Home or Login |
| LoginScreen | Email + password login, tab switch for Mahasiswa / Klien |
| RegisterScreen | Register with role selection (Mahasiswa or Klien) |
| SetupAccountScreen | Post-register profile setup (name, university, bio, etc.) |

### Mahasiswa Screens
| Screen | Description |
|---|---|
| HomeScreen | Top Workers leaderboard, latest Blog/Tips, featured Jasa |
| SearchScreen | Browse & filter Jasa and Projects by category, price, rating |
| CreateJasaScreen | Form to create a new Gig (title, description, price, image) |
| MyJasaScreen | List of own Jasa with edit/delete |
| PortfolioScreen | View and manage portfolio items |
| AddPortfolioScreen | Upload portfolio item (image + description + project URL) |
| SkillsScreen | Manage skills listed on profile |
| OrderHistoryScreen | List of incoming orders with status |
| OrderDetailScreen | Detail of a single order, status updates, deliver button |
| ChatScreen | Real-time chat per order (WebSocket) |
| ChatListScreen | All active chat conversations |
| WalletScreen | Wallet balance, transaction history |
| ProfileScreen | Own profile view (rating, bio, portfolio preview) |
| EditProfileScreen | Edit name, bio, university, avatar |
| SecurityScreen | Change password, 2FA toggle, login history |

### Klien Screens
| Screen | Description |
|---|---|
| HomeScreen | Same as Mahasiswa home |
| SearchScreen | Browse Jasa and Job Board posts |
| JasaDetailScreen | Gig detail page, reviews, order button |
| PostProjectScreen | Form to post a new Job Board project |
| MyProjectsScreen | List of own posted projects |
| CheckoutScreen | Escrow explanation + order confirmation |
| PaymentScreen | Payment method selection (GoPay, OVO, QRIS, VA) → opens Midtrans UI |
| OrderHistoryScreen | List of placed orders with status |
| OrderDetailScreen | Detail of order, accept/dispute buttons |
| ReviewScreen | Leave rating and review after order completion |

### Shared Screens
| Screen | Description |
|---|---|
| NotificationsScreen | Push notification history |
| ArticleListScreen | Blog & Tips list |
| ArticleDetailScreen | Single article view |
| PublicProfileScreen | View another user's profile and portfolio |
| DisputeScreen | Submit a dispute for an order |

---

## Navigation Flow

```
SplashScreen
  ├── (no token) → LoginScreen → RegisterScreen → SetupAccountScreen → HomeScreen
  └── (has token) → HomeScreen

HomeScreen (bottom nav)
  ├── Home tab
  ├── Search tab
  ├── Post tab (Mahasiswa: CreateJasa / Klien: PostProject)
  ├── Orders tab → OrderHistoryScreen → OrderDetailScreen
  └── Profile tab → ProfileScreen

ChatListScreen → ChatScreen (accessible from OrderDetailScreen)
WalletScreen (accessible from ProfileScreen)
PaymentScreen (accessible from CheckoutScreen)
```

---

## API Endpoints (consumed by Android app)

### Auth
| Method | Endpoint | Description |
|---|---|---|
| POST | /auth/register | Register new user |
| POST | /auth/login | Login, returns JWT token |
| POST | /auth/verify-email | Email verification |
| POST | /auth/refresh | Refresh JWT token |

### Users
| Method | Endpoint | Description |
|---|---|---|
| GET | /users/me | Get own profile |
| PATCH | /users/me | Update own profile |
| GET | /users/:id | Get public profile |

### Jasa (Gigs)
| Method | Endpoint | Description |
|---|---|---|
| GET | /jasa | List all Jasa (with filters) |
| GET | /jasa/:id | Get single Jasa detail |
| POST | /jasa | Create new Jasa (Mahasiswa only) |
| PATCH | /jasa/:id | Update Jasa |
| DELETE | /jasa/:id | Delete Jasa |

### Project (Job Board)
| Method | Endpoint | Description |
|---|---|---|
| GET | /project | List all Projects |
| GET | /project/:id | Get single Project |
| POST | /project | Create Project (Klien only) |
| PATCH | /project/:id | Update Project |

### Pesanan (Orders)
| Method | Endpoint | Description |
|---|---|---|
| POST | /pesanan | Place an order |
| GET | /pesanan | Get own orders |
| GET | /pesanan/:id | Get order detail (includes latest `laporan` when disputed) |
| PATCH | /pesanan/:id/deliver | Mahasiswa marks delivered |
| PATCH | /pesanan/:id/complete | Klien accepts delivery |

### Pembayaran (Payment)
| Method | Endpoint | Description |
|---|---|---|
| POST | /pembayaran/create-token | Get Midtrans payment token |
| POST | /pembayaran/webhook | Midtrans callback (called by Midtrans, not app) |

### Chat
| WebSocket | Event | Description |
|---|---|---|
| connect | — | Connect with JWT token |
| joinRoom | pesananId | Join order chat room |
| sendMessage | { pesananId, message } | Send message |
| receiveMessage | — | Listen for incoming messages |

### Others
| Method | Endpoint | Description |
|---|---|---|
| GET | /portofolio/:mahasiswaId | Get portfolio items |
| POST | /portofolio | Upload portfolio item |
| GET | /review/:jasaId | Get reviews for a Jasa |
| GET | /review/pesanan/:pesananId | Get review for an order (buyer-only; 404 if none) |
| POST | /review | Submit review (409 if order already reviewed) |
| PATCH | /review/:id | Update own review (rating, comment) |
| GET | /artikel | List articles |
| GET | /artikel/:id | Article detail |
| GET | /top-worker | Leaderboard |
| POST | /laporan | Submit dispute |

---

## Data Models (Domain Layer)

```kotlin
data class User(val id: String, val email: String, val role: Role)
data class Mahasiswa(val id: String, val userId: String, val university: String,
                     val skills: List<String>, val bio: String,
                     val walletBalance: Double, val rating: Float)
data class Klien(val id: String, val userId: String, val companyName: String?, val bio: String)
data class Jasa(val id: String, val mahasiswaId: String, val kategoriId: String,
                val title: String, val description: String, val price: Double,
                val imageUrl: String, val status: JasaStatus)
data class Project(val id: String, val klienId: String, val title: String,
                   val description: String, val budget: Double, val deadline: String)
data class Pesanan(val id: String, val klienId: String, val mahasiswaId: String,
                   val jasaId: String?, val projectId: String?,
                   val totalPrice: Double, val status: PesananStatus)
data class Chat(val id: String, val pesananId: String, val senderId: String,
                val message: String, val createdAt: String)
data class Review(val id: String, val pesananId: String, val rating: Int, val comment: String)
data class Laporan(val id: String, val reporterId: String, val reason: String,
                   val status: LaporanStatus, val resolutionNote: String?,
                   val refundAmount: Double?, val createdAt: String, val resolvedAt: String?)
data class Artikel(val id: String, val title: String, val content: String, val imageUrl: String?)
data class TopWorker(val mahasiswaId: String, val score: Double, val rank: Int)

enum class Role { MAHASISWA, KLIEN, ADMIN }
enum class PesananStatus { PENDING, PAID, IN_PROGRESS, DELIVERED, COMPLETED, DISPUTED }
enum class LaporanStatus { PENDING, RESOLVED, REJECTED }
enum class JasaStatus { ACTIVE, INACTIVE }
```

---

## Local API Base URL (Development)

- Emulator: `http://10.0.2.2:3000`
- Physical device: `http://<your-machine-local-ip>:3000`
- Production: Railway URL (set via BuildConfig)

---

## Key Rules for Implementation

1. Always read this file and the full spec before starting any feature
2. No business logic in Composables — only UI and ViewModel calls
3. No direct Retrofit calls in ViewModels — always through UseCases
4. Every screen has its own ViewModel — no shared ViewModels between screens
5. Use `StateFlow` for UI state, `SharedFlow` for one-time events (navigation, toasts)
6. Handle loading, success, and error states for every API call
7. Store JWT token in DataStore, attach to every request via Retrofit interceptor
