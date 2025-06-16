# 🎮 GameVault

GameVault is a modern Android application built with **Kotlin**, **Jetpack Compose**, **Room**, **Retrofit**, and **Moshi**, following the **MVVM architecture**. It allows users to browse video games using the IGDB API, manage a personal game library, and sync data with Firebase.

---

## ✨ Features

- 🔍 Browse and search games via the **IGDB API**
- 🧾 View detailed game information
- 📥 Save games to a personal library using **Room**
- ☁️ Cloud synchronization with **Firebase Firestore**
- 🔐 Authentication using **email/password** and **Google Sign-In**
- ⚙️ Reactive UI with **Jetpack Compose**
- 🧠 Architecture based on **MVVM + Kotlin Coroutines**

---

## 🛠️ Tech Stack

| Category            | Libraries / Tools                             |
|---------------------|-----------------------------------------------|
| Language            | Kotlin                                        |
| UI Framework        | Jetpack Compose                               |
| Local Storage       | Room (SQLite ORM)                             |
| Remote Data         | Retrofit, Moshi, IGDB API                     |
| Authentication      | Firebase Auth (Email/Password, Google OAuth) |
| State Management    | ViewModel, Kotlin Coroutines, Flow            |
| Architecture        | MVVM                                          |
| Dependency Injection| *(Optional: Hilt/Koin – if used)*             |

---

```markdown
## 📁 Project Structure

<pre>
com.example.gamevault
├── core
│   ├── model                 # Data models (Game, UserGameEntity, etc.)
├── data
│   ├── local                 # Room database + DAOs
│   ├── remote                # API services, Retrofit client, Firebase helpers
│   └── repository            # Repositories combining local and remote data
├── ui
│   ├── theme                 # UI theming files
│   ├── components            # Reusable UI components
│   └── screens               # UI screens grouped by feature (login, search, etc.)
├── GameVaultApp.kt           # Application class
├── MainActivity.kt           # Root composable and navigation host
└── GameVaultAppViewModelProvider.kt # ViewModel providers
</pre>
