# BloodHero SQLite Migration Guide

## Critical Data Bleeding Fix

### Problem
SharedPreferences was storing all user data in a single file, causing data to bleed between accounts. When logging in with different users, donation counts and other data would persist or change unexpectedly.

### Solution
Migrated to SQLite database with proper user isolation:
- Each user has their own row in the `users` table
- User data is fetched based on unique user ID
- Session only stores current user ID (not all user data)

## Files Created

### 1. UserRepository.java
Location: `app/src/main/java/com/example/bloodhero/repository/UserRepository.java`

Provides clean interface for all user operations:
- `registerUser()` - Create new user account
- `loginUser()` - Authenticate user
- `getUserByEmail()` - Fetch user by email
- `getUserById()` - Fetch user by ID  
- `updateUser()` - Update user profile
- `incrementDonations()` - Add donation count
- `clearAllUsers()` - Admin function to reset all data

### 2. SessionManager.java
Location: `app/src/main/java/com/example/bloodhero/utils/SessionManager.java`

Manages current logged-in user session:
- Uses SharedPreferences ONLY for session (user_id, is_logged_in, is_admin)
- All actual user data comes from SQLite
- `createLoginSession()` - Start new session
- `getUserId()` - Get current user ID
- `isLoggedIn()` - Check login status
- `logout()` - Clear session

### 3. UserHelper.java
Location: `app/src/main/java/com/example/bloodhero/utils/UserHelper.java`

Convenient static methods:
- `getCurrentUser()` - Get full User object from SQLite
- `isLoggedIn()` - Quick login check
- `logout()` - Quick logout

## Files Modified

### 1. User.java
Added `password` field to model

### 2. BloodHeroDatabaseHelper.java
Added `password` column to users table

### 3. LoginActivity.java
**BEFORE**: Used SharedPreferences for everything
**AFTER**: 
- Uses `UserRepository.loginUser()` to authenticate
- Creates session with `SessionManager.createLoginSession()`
- No data stored in SharedPreferences except session ID

### 4. RegisterActivity.java  
**BEFORE**: Used SharedPreferences for everything
**AFTER**:
- Uses `UserRepository.registerUser()` to create account
- Auto-creates session after registration
- No data bleeding between registrations

## Migration Steps for Remaining Activities

### Pattern to Follow:

**OLD CODE**:
```java
SharedPreferences prefs = getSharedPreferences("BloodHeroPrefs", MODE_PRIVATE);
String name = prefs.getString("user_name", "");
int points = prefs.getInt("user_points", 0);
```

**NEW CODE**:
```java
User user = UserHelper.getCurrentUser(this);
if (user != null) {
    String name = user.getName();
    int points = user.getTotalPoints();
}
```

### Activities That Need Migration:

1. **ProfileSetupActivity.java**
   - Change: Save profile to SQLite instead of SharedPreferences
   - Use: `userRepository.updateUser(user)`

2. **HomeActivity.java**
   - Change: Load user data from SQLite
   - Use: `User user = UserHelper.getCurrentUser(this)`

3. **ProfileActivity.java**
   - Change: Display and edit SQLite data
   - Use: `UserHelper.getCurrentUser()` and `userRepository.updateUser()`

4. **All Admin Activities**
   - AdminUsersActivity - Already uses UserStorage, should migrate to SQLite
   - AdminDonationsActivity - Update to use SQLite donations table
   - Others - Migrate similarly

## Testing the Fix

### Test Case 1: New Account Registration
1. Register new account with email test1@example.com
2. Complete profile setup with blood type A+
3. Check donations = 0, points = 0
4. Logout

### Test Case 2: Existing Account Login
1. Login with test1@example.com
2. Verify: Blood type = A+, donations = 0, points = 0
3. Logout

### Test Case 3: Second Account (Critical Test)
1. Register new account test2@example.com  
2. Complete profile with blood type B+
3. **VERIFY**: donations = 0, points = 0 (NOT test1's data!)
4. Logout

### Test Case 4: Return to First Account
1. Login with test1@example.com
2. **VERIFY**: Still A+, still 0 donations
3. Data has NOT changed

## Database Schema

```sql
CREATE TABLE users (
    id TEXT PRIMARY KEY,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    name TEXT,
    blood_type TEXT,
    location TEXT,
    phone TEXT,
    total_donations INTEGER DEFAULT 0,
    total_points INTEGER DEFAULT 0,
    profile_image_url TEXT,
    created_at TEXT
);
```

## Next Steps

1. **Complete Migration** - Update ProfileSetupActivity, HomeActivity, ProfileActivity
2. **Remove Old Files** - Delete UserStorage.java (SharedPreferences-based)
3. **Donation Repository** - Create DonationRepository for proper donation tracking
4. **Appointment Repository** - Create AppointmentRepository
5. **Admin Panel Update** - Migrate admin activities to use repositories

## Blood Compatibility Animation

For the flowing blood bag animation showing connections to compatible types:
- Requires redesign of BloodCompatibilityActivity layout
- Need target coordinates for each blood type to draw animated lines/particles
- Consider using custom Canvas drawing with PathEffect for flowing effect
- Can be implemented after critical data fix is complete
