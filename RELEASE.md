# Release Process

This project uses [JReleaser](https://jreleaser.org/) and GitHub Actions to automate deployments to Maven Central and GitHub Releases.

## Prerequisites

The following **Secrets** must be configured in the GitHub Repository (`Settings` > `Secrets and variables` > `Actions`):

| Secret Name | Description |
|---|---|
| `JRELEASER_MAVENCENTRAL_USERNAME` | Your Sonatype/Maven Central username (or token user) |
| `JRELEASER_MAVENCENTRAL_TOKEN` | Your Sonatype/Maven Central password (or token) |
| `JRELEASER_GPG_PASSPHRASE` | The passphrase for your GPG key |
| `JRELEASER_GPG_PUBLIC_KEY` | Your **Public** GPG key (ASCII armored) |
| `JRELEASER_GPG_SECRET_KEY` | Your **Secret** GPG key (ASCII armored) |

*Note: `GITHUB_TOKEN` is automatically provided by GitHub Actions.*

## How to Release

To trigger a new release, follow these steps:

1. Browse to https://github.com/glaforge/gemini-interactions-api-sdk
2. Navigate to the **Actions** tab.
3. Select the **Release** workflow from the sidebar.
4. Click on the **Run workflow** button.

## Automation

The process is handled by the GitHub Action workflow defined in `.github/workflows/release.yml`.
When the workflow is triggered:
1.  The project is built (`mvnw deploy` to a local staging directory).
2.  JReleaser signs the artifacts using the provided GPG secrets.
3.  Artifacts are deployed to Maven Central.
4.  A GitHub Release is created with a changelog.
