# Authentication System 2.0

Spring Boot backend with a JavaFX desktop frontend for authentication, OTP, TOTP, face login, sessions, notifications, and an authenticator vault.

## Run locally

Backend:

```bash
cd backend
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
mvn javafx:run
```

The backend defaults to `http://localhost:8082`. On Railway it uses the `PORT` environment variable automatically.

## Railway deployment

Railway can host the Spring Boot backend using `railway.json`.

Set these environment variables in Railway:

```text
JWT_SECRET=<a long random secret, 32+ characters>
AUTH_GMAIL_USER=<optional Gmail address>
AUTH_GMAIL_APP_PASSWORD=<optional Gmail app password>
```

Important: the current frontend is JavaFX desktop, not a browser web app. Railway will publish the backend API URL. To let anyone use the full app from a link, the JavaFX frontend must be rebuilt as a web frontend, or users must download and run the desktop app.

## Test account

The backend creates a local test user when the database is empty:

```text
username: testuser
password: Test123!
```
