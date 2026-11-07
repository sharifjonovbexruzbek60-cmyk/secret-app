# Secret App

My first Kotlin-based Android app project. I’m using it to learn Android development and build good habits around privacy and secure configuration as the project grows.

## Project goals

- Keep API keys, passwords, tokens, and signing files out of source control.
- Store local development settings in files excluded by `.gitignore`.
- Validate data received from outside the app.
- Request only the Android permissions the app actually needs.
- Keep dependencies updated and review security advisories.

## Requirements

- Android Studio Ladybug or newer
- JDK 17 or later
- Android SDK configured through Android Studio
- Gradle wrapper included in the repository

## Getting started

1. Clone the repository.
2. Copy the example configuration file:

   ```bash
   cp .env.example .env
   ```

3. Add local development values to `.env` only. Do not put real secrets in `.env.example`.
4. Open the project in Android Studio and sync Gradle.
5. Run the app on an emulator or connected Android device.

## Repository structure

```text
app/              Android application module
gradle/           Gradle wrapper configuration
.env.example      Example configuration without real secrets
metadata.json     Project metadata
```

## Before committing

- Check that `.env`, keystores, and generated build files are excluded from Git.
- Make sure credentials or tokens have not been added to the source.
- Review permissions in `AndroidManifest.xml`.
- Run the available tests and Gradle checks.

## Next steps

- Add unit and instrumented tests.
- Add CI checks for formatting, tests, and dependency updates.
- Document the app architecture as it develops.
- Add an open-source license if and when appropriate.

## Security reporting

Please do not post sensitive vulnerability details in a public issue. Contact the repository owner privately with a responsible report.

## License

No license has been declared yet. All rights are reserved until one is added.

## About

Built by Behruzbek Sharifjonov as my first app and a learning project in Kotlin and Android development.
