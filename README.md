@'
# Plan For Plant (Android + Spring Boot)

App demo: https://drive.google.com/file/d/1bYRp-kZwunJq1fdHt7VptHaUZUbMCy0-/view?usp=sharing

Plan For Plant is a Java-based Android app for plant care: identify plants, detect diseases, manage your gardens, plan daily care tasks automatic and manual, and chabot AI.

This repository contains two main modules:
- `mobile/`: Android app (Java)
- `backend/Plant_sever/`: Spring Boot API (Java)

![Plan For Plant](mobile/app/src/main/sample_plant-playstore.png)

## Features
- User authentication (register, login, forgot password, reset password)
- Plant identification from camera/gallery images
- Disease assessment from plant images
- Garden management (add plants, update status, upload gallery photos)
- Garden diary entries per plant
- Care schedule management (create, edit, delete, day grouping)
- Weather-based schedule generation
- Local reminders with WorkManager
- Push notification flow with Firebase token registration
- Plant-care chatbot (text + image, AI-powered with function recall)
+ General Q&A about plant care (watering, fertilizing, pruning, lighting, soil, pests, etc.)
+ Context-aware consultation for user’s own plants (based on stored garden data)
+ Automatic schedule adjustment when disease is detected (e.g., modify watering/fertilizing plan)
+ Persist disease records and update plant health status in the database
+ App-specific knowledge integration via function recall (accessing plant history, care schedules, weather data, and user context)
- Watering streak mini-game with level/progress tracking

## Tech Stack

### Android (`mobile`)
- Java 11 compatibility
- Android SDK 36 (minSdk 24)
- AndroidX + Material Components
- Retrofit2 + OkHttp + Gson
- CameraX
- WorkManager
- Firebase Messaging
- Glide / Picasso

### Backend (`backend/Plant_sever`)
- Java 17
- Spring Boot 3.3
- Spring Web, Security, Data JPA
- PostgreSQL
- JWT auth + refresh flow
- Firebase Admin SDK
- Cloudinary integration
- Gemini API integration

## Project Structure

```text
Plan-for-plant/
|-- mobile/
|   |-- app/
|   |-- gradle/
|   `-- build.gradle.kts
`-- backend/
    `-- Plant_sever/
        |-- pom.xml
        `-- src/
```

## Prerequisites
- JDK 17
- Android Studio (latest stable) with Android SDK 36 installed
- PostgreSQL database
- API keys/services (depending on features):
  - PlantNet
  - Plant.id
  - Meteosource
  - Gemini
  - Firebase service account JSON (for backend push)
  - SMTP account (for email reset flow)

## Backend Setup (`backend/Plant_sever`)

1. Go to backend directory:

```bash
cd backend/Plant_sever
```

2. Create file `src/main/resources/application.properties`.

3. Add a base configuration like this:

```properties
server.port=8080

spring.datasource.url=jdbc:postgresql://localhost:5432/planforplant
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

jwt.secret=replace_with_a_long_random_secret
jwt.expiration=86400000

plantnet.api.key=your_plantnet_key
plantnet.api.base-url=https://my-api.plantnet.org/v2

meteosource.api.key=your_meteosource_key
meteosource.api.url=https://www.meteosource.com/api/v1/free/point

gemini.api-key=your_gemini_api_key

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email
spring.mail.password=your_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Optional if using legacy FCM server key flow in NotificationService
fcm.server.key=
```

4. Add Firebase Admin credentials (if using push from backend):
- Place file at `src/main/resources/firebase-service-account.json`

5. Run backend:

```bash
# Windows
mvnw.cmd spring-boot:run

# macOS/Linux
./mvnw spring-boot:run
```

## Android Setup (`mobile`)

1. Go to Android project:

```bash
cd mobile
```

2. Configure API keys in `gradle.properties`:

```properties
PLANTNET_API_KEY=your_plantnet_key
PLANT_ID_API_KEY=your_plant_id_key
```

3. Configure local backend URL for your environment.

Update these files:
- `app/src/main/java/com/example/planforplant/api/ApiClient.java`
- `app/src/main/java/com/example/planforplant/api/AuthInterceptor.java`
- `app/src/main/res/xml/network_security_config.xml`

Use:
- Android Emulator: `http://10.0.2.2:8080/`
- Physical device: `http://<your-lan-ip>:8080/`

4. Build/run app:

```bash
# Windows
gradlew.bat assembleDebug

# macOS/Linux
./gradlew assembleDebug
```

Or open `mobile/` in Android Studio and run the `app` module.

## Run End-to-End
1. Start PostgreSQL.
2. Start backend (`backend/Plant_sever`).
3. Start Android app (`mobile`).
4. Register/login and test features.

## Tests

```bash
# Android tests
cd mobile
./gradlew test

# Backend tests
cd ../backend/Plant_sever
./mvnw test
```

## Notes
- The project is currently development-oriented (cleartext traffic enabled for local API hosts).
- Keep secrets out of Git in production (API keys, JWT secret, service accounts).
- Before public deployment, rotate any exposed keys and remove hardcoded credentials.
'@ | Set-Content -Path "c:\Users\OS\Downloads\Plan-for-plant\README.md
