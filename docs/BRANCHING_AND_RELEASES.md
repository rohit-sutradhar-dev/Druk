# Branching and Release Management

## Branch Model

Long-lived branches:

- `dev`: integration branch for active development.
- `qa`: release candidate branch.
- `prd`: production branch.
- `main`: repository bootstrap/default branch until GitHub is reconfigured.

Recommended steady-state flow:

1. Create feature branches from `dev`.
2. Open pull requests into `dev`.
3. Promote `dev` to `qa` using a pull request.
4. Promote `qa` to `prd` using a pull request.
5. Deployments are triggered only after merges to `dev`, `qa`, or `prd`.

## Branch Naming

Use:

- `feature/<short-description>`
- `fix/<short-description>`
- `chore/<short-description>`
- `docs/<short-description>`
- `release/<version>`

Examples:

- `feature/session-dashboard`
- `fix/bac-curve-rounding`
- `docs/azure-setup`

## Pull Request Rules

Apply branch protection rules to `dev`, `qa`, and `prd`.

Recommended settings:

- Require a pull request before merging.
- Require at least 1 approval.
- Dismiss stale approvals when new commits are pushed.
- Require status checks to pass.
- Require conversation resolution before merging.
- Require linear history.
- Do not allow force pushes.
- Do not allow deletions.
- Include administrators once the rules are confirmed.

Suggested required checks:

- `Backend CI`
- `Android CI`

## Environment Gates

Create GitHub Environments:

- `dev`
- `qa`
- `prd`

Recommended protection:

- `dev`: no manual approval required.
- `qa`: require approval from a maintainer.
- `prd`: require approval from an owner/release manager.

## Merge Policy

Recommended:

- Squash merge feature branches into `dev`.
- Merge commits or squash merges are acceptable for `dev` to `qa` and `qa` to `prd`, but be consistent.
- Tag production releases from `prd`.

Tag format:

- `v0.1.0`
- `v0.1.1`

## Initial Setup Commands

Run these locally after this scaffold is committed:

```bash
git checkout -b dev
git push -u origin dev
git checkout -b qa
git push -u origin qa
git checkout -b prd
git push -u origin prd
git checkout dev
```

Then set `dev` as the default branch in GitHub repository settings.

