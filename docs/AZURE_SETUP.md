# Azure Setup Guide

This guide assumes the backend will run as a Dockerized FastAPI service on Azure Container Apps.

## Azure Resources

Create one resource group per environment:

- `rg-druk-dev`
- `rg-druk-qa`
- `rg-druk-prd`

Create these resources per environment:

- Azure Container Apps Environment
- Azure Container App for the backend API
- Azure Container Registry, or one shared registry with environment-specific image tags
- Azure Database for PostgreSQL Flexible Server
- Application Insights
- Log Analytics Workspace
- Key Vault

## Recommended Naming

Use short, predictable names:

- `ca-druk-api-dev`
- `ca-druk-api-qa`
- `ca-druk-api-prd`
- `crdrukshared`
- `pg-druk-dev`
- `pg-druk-qa`
- `pg-druk-prd`
- `kv-druk-dev`
- `appi-druk-dev`

## GitHub to Azure Authentication

Use OpenID Connect from GitHub Actions to Azure instead of storing long-lived Azure passwords.

High-level steps:

1. Create a Microsoft Entra app registration for GitHub Actions.
2. Add a federated credential for the GitHub repository and target branch/environment.
3. Assign least-privilege Azure roles to the app registration.
4. Store these values as GitHub repository or environment variables:
   - `AZURE_CLIENT_ID`
   - `AZURE_TENANT_ID`
   - `AZURE_SUBSCRIPTION_ID`

Recommended Azure role assignments:

- Container Apps Contributor on the target resource group.
- AcrPush on the container registry.

## GitHub Secrets and Variables

Use GitHub Environments for `dev`, `qa`, and `prd`.

Per-environment variables:

- `AZURE_CLIENT_ID`
- `AZURE_TENANT_ID`
- `AZURE_SUBSCRIPTION_ID`
- `AZURE_RESOURCE_GROUP`
- `AZURE_CONTAINER_APP_NAME`
- `AZURE_CONTAINER_REGISTRY`

Per-environment secrets:

- Database connection string, if not injected from Key Vault.
- Firebase service account credentials, once backend auth verification is implemented.

## Database

Use Azure Database for PostgreSQL Flexible Server.

Recommended starting configuration:

- Burstable or low-cost general purpose tier for dev and QA.
- Production tier sized after load testing.
- Private networking later, public restricted firewall during early MVP if needed.
- Automated backups enabled.

## Deployment Flow

GitHub Actions should:

1. Run CI on pull requests.
2. Build the backend Docker image after merge to an environment branch.
3. Push the image to Azure Container Registry.
4. Deploy the image to the matching Azure Container App.
5. Run a `/health` smoke test.

## Android Releases

For the first backend-focused CI/CD setup, Android builds should produce debug artifacts only.

Later release rollout options:

- Firebase App Distribution for dev and QA testers.
- Google Play internal testing for pre-production.
- Google Play staged rollout for production.

