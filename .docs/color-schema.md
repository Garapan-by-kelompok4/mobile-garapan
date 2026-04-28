# GARAPAN Color Schema

Derived from brand colors and mockup references in `.docs/images/`.

---

## Brand Colors

| Name | Hex | Usage |
|---|---|---|
| Brand Navy | `#00288E` | Logo, app name, CTA buttons, hero section background, active bottom nav icon |
| Accent Blue | `#1560FC` | "Student" active tab, text links ("Sign In", "Lihat Semua", "Create one now"), focus state |

---

## Backgrounds

| Name | Hex | Usage |
|---|---|---|
| White | `#FFFFFF` | Main screen background, cards, bottom nav bar |
| Surface | `#F5F7FA` | Input field backgrounds, section backgrounds, tab switcher container |
| Light Gray | `#EEEEEE` | Dividers, inactive tab container, subtle borders |

---

## Text

| Name | Hex | Usage |
|---|---|---|
| Primary Text | `#0D0D0D` | Headlines, body copy, labels |
| Secondary Text | `#64748B` | Subtitles, supporting text (e.g. "The IT Project Marketplace") |
| Muted / Hint | `#9CA3AF` | Placeholder text inside input fields |
| On Primary | `#FFFFFF` | Text on navy buttons and hero section |
| Link / Accent Text | `#1560FC` | Clickable text links, active state labels |

---

## UI Components

| Name | Hex | Usage |
|---|---|---|
| Input Background | `#F5F7FA` | Text field fill (no border style) |
| Input Border | `#E5E7EB` | Subtle border when field is focused |
| Card Background | `#FFFFFF` | Service cards, project cards |
| Card Border | `#E5E7EB` | Thin border on cards |
| Tab Active BG | `#FFFFFF` | Active tab pill (Student / Client switcher) |
| Tab Inactive BG | `transparent` | Inactive tab state |
| Bottom Nav Active | `#00288E` | Active bottom nav icon |
| Bottom Nav Inactive | `#9CA3AF` | Inactive bottom nav icon |

---

## Feedback / Status

| Name | Hex | Usage |
|---|---|---|
| Success / Green | `#22C55E` | Completed order badge, success toast |
| Warning / Star | `#F59E0B` | Rating stars |
| Error | `#EF4444` | Validation errors, error toasts |
| Info | `#1560FC` | Info badge (reuses accent) |

---

## Elevation / Shadow

All cards and modals use a soft shadow:
```
color: #000000 at 6% opacity
blur: 12dp
offset: (0, 2dp)
```

---

## Kotlin Color Constants (for `ui/theme/Color.kt`)

```kotlin
// Brand
val BrandNavy = Color(0xFF00288E)
val AccentBlue = Color(0xFF1560FC)

// Backgrounds
val White = Color(0xFFFFFFFF)
val Surface = Color(0xFFF5F7FA)
val LightGray = Color(0xFFEEEEEE)

// Text
val PrimaryText = Color(0xFF0D0D0D)
val SecondaryText = Color(0xFF64748B)
val MutedText = Color(0xFF9CA3AF)
val OnPrimary = Color(0xFFFFFFFF)
val LinkText = Color(0xFF1560FC)

// Borders
val BorderColor = Color(0xFFE5E7EB)

// Feedback
val SuccessGreen = Color(0xFF22C55E)
val StarYellow = Color(0xFFF59E0B)
val ErrorRed = Color(0xFFEF4444)
```

---

## Notes

- The splash screen uses a **white background** with the navy/blue logo centered — simple and brand-consistent.
- Hero section on homepage uses `#00288E` as a full-width dark banner with white text.
- Do **not** use dark theme — the app is light-only based on mockups.
- Typography is bold for headlines, regular for body. No custom font specified in mockups — default system sans-serif is used.
