# BloodHero

A blood donation mobile application for Android that connects donors with blood donation campaigns across Morocco. Built to help save lives by making blood donation more accessible and engaging.

## Features

### Donor Features
- User authentication (email/password)
- Profile management with blood type selection
- Browse and search blood donation campaigns by city
- Interactive map view of nearby campaigns (OpenStreetMap)
- Book donation appointments with confirmation
- Track donation history and statistics
- View blood compatibility information
- Gamification system with points and achievement badges
- Leaderboard to encourage community participation
- Light/Dark theme support

### Admin Panel
- Separate admin dashboard
- View registered users and their blood types
- Manage appointments
- Track donations across the platform
- Manage user badges
- Campaign and reports management (coming soon)

### Additional Features
- Moroccan cities support (Casablanca, Rabat, Marrakech, Fes, Tangier, etc.)
- Real campaign data from Moroccan blood donation centers
- Offline-first design with local storage
- Material Design 3 UI components
- Smooth animations and transitions

## Tech Stack

- Language: Java
- Minimum SDK: 29 (Android 10)
- Target SDK: 36
- UI: Material Design 3, CardView, RecyclerView
- Maps: OpenStreetMap (osmdroid)
- Database: SQLite with SharedPreferences
- Architecture: Activity-based

## Project Structure

```
app/src/main/java/com/example/bloodhero/
├── activities/
│   ├── AdminDashboardActivity.java
│   ├── AdminAppointmentsActivity.java
│   ├── AdminUsersActivity.java
│   ├── AdminDonationsActivity.java
│   ├── AdminBadgesActivity.java
│   ├── CampaignsActivity.java
│   ├── CampaignMapActivity.java
│   ├── DonationHistoryActivity.java
│   ├── AchievementsActivity.java
│   ├── LeaderboardActivity.java
│   ├── ProfileActivity.java
│   ├── SettingsActivity.java
│   ├── MyAppointmentsActivity.java
│   ├── BloodCompatibilityActivity.java
│   ├── HelpSupportActivity.java
│   └── NotificationSettingsActivity.java
├── adapters/
│   ├── CampaignAdapter.java
│   ├── AchievementAdapter.java
│   └── LeaderboardAdapter.java
├── database/
│   └── BloodHeroDatabaseHelper.java
├── models/
│   ├── Campaign.java
│   ├── Achievement.java
│   └── LeaderboardEntry.java
├── utils/
│   └── UserStorage.java
├── HomeActivity.java
├── LoginActivity.java
├── RegisterActivity.java
├── ProfileSetupActivity.java
└── SplashActivity.java
```


## Setup and Installation

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run on emulator or physical device (API 29+)

