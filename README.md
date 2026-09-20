# Secret App

> A Kotlin-based Android application foundation focused on secure configuration and privacy-conscious development.

[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Security first](https://img.shields.io/badge/security-first-0A7B5A)](SECURITY.md)
[![Status](https://img.shields.io/badge/status-learning%20project-6E7781)](#roadmap)

## Overview

`secret-app` is an Android/Kotlin project scaffold designed to keep secrets out of source control and provide a clean foundation for secure application development.

## Project principles

- Never commit credentials, API keys, tokens, or signing files.
- Keep local configuration in `.env` or Gradle properties excluded by `.gitignore`.
- Validate externally supplied data at the application boundary.
- Prefer least-privilege Android permissions.
- Keep dependencies updated and review security advisories.

## Requirements

- Android Studio Ladybug or newer
- JDK 17+
- Android SDK configured through Android Studio
- Gradle wrapper included in the repository

## Getting started

1. Clone the repository.
2. Copy the safe configuration template:

   ```bash
   cp .env.example .env
   ```

3. Fill in local development values only; never add real secrets to `.env.example`.
4. Open the project in Android Studio.
5. Sync Gradle and run the app on an emulator or connected device.

## Repository structure

```text
app/              Android application module
gradle/            Gradle wrapper configuration
.env.example      Safe configuration template
metadata.json     Project metadata
```

## Security checklist

Before every commit:

- Confirm `.env`, keystores, and generated build files are ignored.
- Search for accidentally embedded tokens or passwords.
- Review permissions declared in `AndroidManifest.xml`.
- Run the project’s available tests and Gradle checks.

## Roadmap

- Add unit and instrumented tests.
- Add CI checks for formatting, tests, and dependency updates.
- Document the application architecture and threat model.
- Add a declared open-source license.

## Security reporting

If you discover a security issue, do not publish sensitive details in a public issue. Use the repository owner’s private contact channel and include safe reproduction steps without exposing credentials or personal data.

## License

No license has been declared yet. All rights are reserved until a license is added.

## Author

Behruzbek Sharifjonov — Android/Kotlin and cybersecurity learner.
