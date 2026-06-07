# RoleCall – Android App

**RoleCall** is a native Android application that uses **semantic AI** to match a user’s résumé to the most relevant job descriptions.  
Simply upload a résumé (PDF, DOCX, or photo) and instantly receive a ranked list of matching jobs with transparent match scores and explanations.

---

## 🚀 Features

- 📄 **One‑tap résumé upload** – PDF, DOCX, or camera photo  
- 🤖 **AI‑powered matching** – semantic search using a MiniLM sentence transformer  
- 🎯 **Match explainability** – see exactly which phrases drove a high score  
- 💾 **Save jobs locally** – bookmark interesting roles  
- 📊 **Match history** – revisit past matches anytime  
- 🌙 **Dark theme** – custom colour palette & typography  
- 📱 **Offline‑capable** – core features work without a network (mock data stage)

---

## 🧰 Tech Stack

| Layer          | Technology |
|----------------|------------|
| **Language**   | Kotlin 2.0+ |
| **UI Toolkit** | Jetpack Compose + Material 3 |
| **Navigation** | Navigation Compose |
| **Local DB**   | Room (SQLite) |
| **Networking** | Retrofit + OkHttp |
| **DI**         | Hilt |
| **AI Backend** | Python FastAPI (separate repository) |

---

## 📁 Project Structure (simplified)

RoleCall-Android/
├── app/src/main/java/com/example/rolecall/

│ ├── data/

│ │ ├── model/ # Data classes (JobItem, etc.)

│ │ ├── mock/ # Mock data for development

│ │ └── ... (future: Room DB, Retrofit service)

│ ├── navigation/ # NavGraph, Routes

│ ├── ui/

│ │ ├── components/ # Reusable Composables (JobCard, MatchBadge)

│ │ ├── screens/ # UploadScreen, ResultsScreen, JobDetailScreen, HistoryScreen

│ │ └── theme/ # Color, Typography, Theme

│ └── MainActivity.kt

├── app/src/main/res/

│ ├── font/ # Custom fonts (Balsamiq Sans, Baloo Thambi 2, Baloo Tamma 2)

│ └── drawable/ # RoleCall logo

├── gradle/

│ └── libs.versions.toml # Version catalog (all dependencies)

└── build.gradle.kts


---

## 🔧 Getting Started

### Prerequisites
- **Android Studio Hedgehog (2023.1.1)** or later
- **Android SDK 35** installed
- **JDK 17** (bundled with Android Studio)
- A physical device or emulator running **Android 8.0+ (API 26)**

### Setup
1. **Clone the repository**
   ```bash
   git clone https://github.com/your-org/RoleCall-Android.git
   cd RoleCall-Android
Open in Android Studio – Choose Open an existing project and select the RoleCall-Android folder.

Sync Gradle – Android Studio will prompt you to sync; click Sync Now.

Add your logo – Place your app icon in app/src/main/res/drawable/rolecall_logo.png (rename as needed).

Run the app – Select a device/emulator and press Run (Shift+F10).

## 🎨 Design

| Element              |	Value |
|----------------------|------------|
| **Background**	     | #121B2A (Foundation Dark)|
| **Surface**          |#1E2D42 (Foundation Surface)|
| **Primary Text**     |	#FFFFFF|
| **Secondary Text**   |	#94A3B8|
| **Accent (Success)** |	#1EA355|
| **Accent (Alert)**   | #EF4444|
| **Interactive**      |	#3572B6|
| **Borders**          |	#2E3F56|

## Typography:

Headlines → Balsamiq Sans

Sub‑headlines → Baloo Thambi 2

Body → Baloo Tamma 2

## 🧪 Testing

Mock data is used during development – no backend required for the UI flow.

Integration tests will be added once the backend API is connected.

Use MockWebServer (OkHttp) to simulate API responses.

## 📝 License

This project is created for educational purposes as part of a Computer Science capstone.
Fonts are used under the SIL Open Font License.
All other code is copyright © 2025 RoleCall Team.

## 👥 Team
| Name                 |	Email |
|----------------------|------------|
| **Megan Torres**     | megan.torres1171991@gmail.com|
| **Timothy Duquette** | tdduquette@student.fullsail.edu|




## Built with ❤️ for the Capstone Project.
