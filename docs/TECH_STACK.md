# Druk Tech Stack

## Recommendation

Druk should start as a local-first Android app with a small backend for account data, session sync, and future analytics.

## Mobile

- Kotlin
- Jetpack Compose
- MVVM or MVI-style state management
- Room for local session/drink storage
- DataStore for profile and settings
- WorkManager for local pacing alerts
- Local notifications for MVP

Live BAC estimation should run on-device so the app works in bars, events, and low-connectivity environments.

## Backend

- FastAPI
- PostgreSQL
- REST API
- Docker container
- Azure Container Apps for hosting
- Azure Container Registry for images
- Application Insights for telemetry
- Key Vault for secrets

## Auth

Recommended MVP path:

- Firebase Auth for Google Sign-In and email/password.
- Backend verifies Firebase ID tokens before accepting synced user/session data.

Azure-native alternative:

- Microsoft Entra External ID.

Firebase Auth is the faster MVP choice for Android. Entra can be revisited if keeping identity fully in Azure becomes important.

## Environments

Use three long-lived branches and three Azure environments:

| Branch | Environment | Purpose |
| --- | --- | --- |
| `dev` | Development | Integration and internal testing |
| `qa` | QA/Staging | Release candidate validation |
| `prd` | Production | User-facing production |

Feature work should happen on short-lived branches and enter `dev` via pull request.

## Deployment Strategy

- Pull request to `dev`, `qa`, or `prd`: run CI only.
- Merge to `dev`: deploy backend to Azure dev.
- Merge to `qa`: deploy backend to Azure QA.
- Merge to `prd`: deploy backend to Azure production.

Android release distribution can be added after signing and Play Console/Firebase App Distribution are configured.

