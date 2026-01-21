# TrackerMoney 💸

TrackerMoney is a comprehensive **Kotlin Multiplatform (KMP)** application designed to help you manage both shared group expenses and personal finances. Built with **Compose Multiplatform** and **Ktor**, it runs seamlessly on Android, iOS, Desktop, and Web.

![UI Screenshot](https://ui-avatars.com/api/?name=TM&background=F59E0B&color=fff&size=128&bold=true) <!-- Replace with actual screenshot later -->

## ✨ Key Features

### 👥 Shared Group Expenses
*   **Create Rooms:** Easily set up groups for trips, housemates, or events.
*   **Track Spending:** Add expenses and specify who paid.
*   **Settle Debts:** Automated algorithm calculates the most efficient way to settle debts among group members.
*   **Real-time Updates:** (Planned) Sync shared expenses across all devices.

### 👤 Personal Finance Management
*   **Dashboard:** View your total balance, total income, and total expenses at a glance.
*   **Transaction Tracking:** Record personal income and expenses with categories and notes.
*   **Visual Breakdown:** Color-coded list of transactions (Green for Income, Red for Expense).
*   **Privacy:** Personal data is kept separate from shared group data.

### 🎨 Sleek UI/UX
*   **Modern Dark Mode:** A visually stunning Slate 900 theme with Amber 500 accents.
*   **Glassmorphism:** Premium feel with translucent cards and smooth gradients.
*   **Responsive Design:** Optimized layouts for mobile and desktop screens.

## 🛠️ Tech Stack

*   **Language:** Kotlin (100%)
*   **UI:** Compose Multiplatform (Android, iOS, Desktop, Web)
*   **Backend:** Ktor Server
*   **Database:** PostgreSQL with Exposed ORM
*   **Networking:** Ktor Client
*   **Serialization:** Kotlinx Serialization
*   **Date/Time:** Kotlinx DateTime

## 📂 Project Structure

*   `/composeApp`: Shared UI code (Compose Multiplatform).
    *   `commonMain`: Common UI logic and screens.
*   `/server`: Backend Ktor server application.
*   `/shared`: Shared business logic and data models.
*   `/iosApp`: iOS specific entry point.

## 🚀 Build and Run

### Android
```shell
./gradlew :composeApp:assembleDebug
```

### Desktop (JVM)
```shell
./gradlew :composeApp:run
```

### Server (Backend)
Required for the app to function. Ensure you have a local PostgreSQL database running or update the config.
```shell
./gradlew :server:run
```

### Web (Wasm)
```shell
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

### iOS
Open `iosApp/iosApp.xcodeproj` in Xcode and run.

---
*Built with ❤️ using Kotlin Multiplatform*