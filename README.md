# Unixi Auth App

Android application built in Kotlin using Android Studio with Jetpack Compose for the Unixi junior mobile developer assignment.


## How to run:
1. Download, open and run the server file, instructions in it's ReadME (python run.py)
2. Run the application using a physical android with a camera (if used, emulator will only simulate a room for the camera)
3. Scan the qrcodes from the terminal, each will provide a different result

---

## Overview

This app implements the demanded flow:

1. Scan a QR code
2. Read backend configuration from the QR payload
3. Resolve the QR token with the backend
4. Display the user email on an authentication screen
5. Validate the entered password with the backend
6. Show:
   - an error screen if authentication fails
   - a success screen if authentication succeeds
7. Redirect the user to the main application

The main application contains two tabs:
- Home: displays user information
- Device: displays device and app information

---

## Tech Stack

- **Kotlin**
- **Jetpack Compose** UI
- **Koin** for dependency injection
- **Ktor Client** for backend communication
- **CameraX** for camera preview
- **ML Kit Barcode Scanning** for QR scanning
- **Kotlinx Serialization** for JSON parsing

---

## QrDecoding:

1. Each QR code contains a signed JWT (HS256). The JWT holds two claims: 
	a. qrToken - readable plaintext, tamper-proof via JWT signature
	b. url - the proxy address encrypted with AES-256-GCM.
	c. the rawdata itself is Base64url encoded. 
2. in the server, Two additional claims are included but not used in the app. Additional data is for prod/future development: 
	a. jti - unique UUID so individual QR codes can be revoked server-side in the future
	b. iat - for expiry. Now QR codes are valid indefinitely. 
I haven't implemented these extra cautious steps, as they are not demanded. Yet it's still important.
The app verifies the JWT signature first, then decrypts and reads the proxy and qrtoken.

I chose JWT because it's a well-established standard with mature libraries on both Python and Kotlin. meaning the serialization, Base64url encoding, signature format, and claim structure are all handled correctly and simply. It is also a natural choice for qr code encryption with features like expiration.
HS256 was chosen because it's symmetric, simple enough for the task - yet still a strong solution.

*keys are hardcoded, which should never be done! but for this development task it's enough. The right solution for production is wraping the keys in the Android Keystore system.

---

## Architecture

The project follows a clean, layered structure with separation of concerns:

- **UI layer**
  - Compose screens
  - screen-specific UI state
  - ViewModels

- **Domain / state orchestration**
  - ViewModels coordinate user actions and app flow

- **Data layer**
  - repositories map remote results into app-specific results
  - remote data source performs network requests
  - session store holds the active in-memory session

- **Utility layer**
  - QR parsing / decrypting
  - device information provider
  - camera preview wrapper

### Main flow

- `ScanScreen` scans a QR code
- `ScanViewModel` parses and encodes the QR and resolves it with the backend
- `SessionStore` saves:
  - backend endpoint
  - user data
- `AuthScreen` shows the email and validates password
- `SuccessScreen` redirects to `HomeScreen`
- `HomeScreen` shows:
  - user info tab
  - device info tab

---

## Assumptions:
1. Client is always connected to network. 
2. User input is considered trusted, as no input validation is required.

---

Package Manager With explanations:

```

app/
├── UnixiApplication.kt              ← Application class, startKoin with all 3 modules
├── MainActivity.kt               ← single activity, setContent, calls AppNavGraph
├── NavGraph.kt                   ← all routes, startDestination = scan, back stack management
├── Navigation.kt                 ← route constants, NavigationActions helper class
├── Theme.kt                      ← MaterialTheme wrapper, colors, typography
│
├── scan/
│   ├── ScanScreen.kt             ← camera UI, permission handling, error dialogs, rate limit UI
│   ├── ScanViewModel.kt          ← decrypts QR, calls AuthRepository, manages rate limiting, writes to SessionStore
│   └── ScanUiState.kt            ← loading, dialog type, rate limit counters, navigateToAuth flag
│
├── auth/
│   ├── AuthScreen.kt             ← shows email (read-only), password input field, submit button
│   ├── AuthViewModel.kt          ← reads email from SessionStore, calls PasswordRepository on submit
│   └── AuthUiState.kt            ← email string, password value, loading, navigateToSuccess, navigateToError
│
├── error/
│   └── ErrorScreen.kt            ← "wrong password" message, try again button → pops back to AuthScreen
│
├── success/
│   └── SuccessScreen.kt          ← success message, continue button → navigates to HomeScreen
│
├── home/
│   ├── HomeScreen.kt             ← renders NavigationBar with 2 tabs, swaps content based on selected tab
│   ├── HomeViewModel.kt          ← owns selected tab state
│   ├── HomeUiState.kt            ← selected tab enum (Tab1 / Tab2)
│   ├── tab1/
│   │   ├── Tab1Screen.kt         ← displays 6 user fields from state
│   │   ├── Tab1ViewModel.kt      ← reads SessionStore, maps UserData fields to Tab1UiState
│   │   └── Tab1UiState.kt        ← fullName, email, company, department, userId, accountCreationDate
│   └── tab2/
│       ├── Tab2Screen.kt         ← displays 6 device fields from state
│       ├── Tab2ViewModel.kt      ← receives DeviceInfoProvider, calls getDeviceInfo() on init
│       └── Tab2UiState.kt        ← model, manufacturer, osVersion, sdkVersion, language, os, appVersion
│
├── util/
│   ├── CameraPreview.kt          ← AndroidView wrapper for CameraX + ML Kit, fires callback on QR detected
│   ├── QrDecryptor.kt            ← pure stateless function, decrypts raw QR string using SECRET, returns AppConfig
│   └── DeviceInfoProvider.kt     ← wraps Build.* and PackageManager calls, plain class, no Android lifecycle dependency
│
├── di/
│   ├── AppModule.kt              ← single { SessionStore() }, single { DeviceInfoProvider(androidContext()) }
│   ├── NetworkModule.kt          ← Retrofit instance, Json config, AuthApiService, AuthRemoteDataSource, AuthRepository, PasswordRepository
│   └── ViewModelModule.kt        ← all viewModel { } declarations for every ViewModel in the app
│
└── data/
    ├── model/
    │   ├── AppConfig.kt                  ← @Serializable, endpointUrl + userId, output of QrDecryptor
    │   ├── UserData.kt                   ← @Serializable, @SerialName on all snake_case fields
    │   ├── DeviceInfo.kt                 ← plain data class, 7 device fields, never serialized
    │   └── PasswordValidationResult.kt   ← sealed class: Success / WrongPassword
    │
    ├── session/
    │   └── SessionStore.kt               ← @Singleton via Koin, holds UserData + endpoint URL in memory
    │
    ├── repository/
    │   ├── AuthRepository.kt             ← maps HTTP response to AuthResult sealed class, no HTTP knowledge
    │   └── PasswordRepository.kt         ← reads endpoint from SessionStore, maps response to PasswordValidationResult
    │
    └── source/
        └── remote/
            ├── AuthApiService.kt         ← Retrofit interface, POST /auth/login and POST /auth/validate
            └── AuthRemoteDataSource.kt   ← calls AuthApiService, returns raw Response, all suspend funs

```

---

## Notes:
1. I have chosen Koin only because Hiltnis genuinely broken right now as of early 2026.
4. Although not required, I did choose to add a rate limiter for entering passwords and scanning Qrs.

## Video
(https://github.com/Tal-Miron/unixiauthapp/blob/main/Screen_Recording_20260317_095028_Unixi%20Auth%20App.mp4)
