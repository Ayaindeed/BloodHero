# BloodHero

A blood donation mobile application for Android that connects donors with blood donation campaigns across Morocco.

## Features

- User authentication (email/password, Google Sign-In)
- Profile management with blood type selection
- Browse and search blood donation campaigns
- Book donation appointments with date/time selection
- Track donation history and statistics
- Gamification system with points and achievement badges
- Leaderboard to encourage community participation


## Tech Stack

- Language: Java
- UI: Material Design 3
- Database: SQLite (local storage)
- Architecture: Activity-based with SharedPreferences

## Project Structure

```
app/src/main/java/com/example/bloodhero/
├── activities/          # Secondary activities
├── adapters/            # RecyclerView adapters
├── database/            # SQLite helper
├── models/              # Data models
├── HomeActivity.java
├── LoginActivity.java
├── RegisterActivity.java
├── ProfileSetupActivity.java
└── SplashActivity.java
```
