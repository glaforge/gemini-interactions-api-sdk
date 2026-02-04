# Cloud Run Deployment Walkthrough

## Overview
We have configured the project to package `ResearchFrontend.java` (located in `src/test/java`) as a standalone Runnable JAR (Uber-JAR) that includes all dependencies and test classes.
This JAR can be deployed directly to Google Cloud Run using the "deploy from source without build" feature.

## Prerequisite
Ensure you have the Google Cloud SDK installed and authenticated.

## Steps

### 1. Build the Runnable JAR
Run the following Maven command to build the application with the `deploy-frontend` profile:
```bash
mvn clean package -Pdeploy-frontend -DskipTests
```
This will create `target/gemini-interactions-api-sdk-*-frontend.jar`.

### 2. Prepare for Deployment
Create a deployment directory and copy the JAR:
```bash
mkdir -p target/deploy
cp target/gemini-interactions-api-sdk-*-frontend.jar target/deploy/app.jar
```

### 3. Grant Permission to Access Secret
The Cloud Run service account needs permission to access the secret. Run this command (replace `[PROJECT_NUMBER]` with your project number, e.g., `1029513523185`):

```bash
gcloud secrets add-iam-policy-binding GEMINI_API_KEY \
    --member="serviceAccount:[PROJECT_NUMBER]-compute@developer.gserviceaccount.com" \
    --role="roles/secretmanager.secretAccessor"
```

### 4. Deploy to Cloud Run
Execute the deployment command:
```bash
gcloud beta run deploy research-frontend \
  --source target/deploy \
  --no-build \
  --base-image=us-central1-docker.pkg.dev/serverless-runtimes/google-24/runtimes/java25 \
  --command="java" \
  --args="-jar","app.jar" \
  --set-secrets="GEMINI_API_KEY=GEMINI_API_KEY:latest" \
  --timeout=60m \
  --region=europe-west1 \
  --session-affinity \
  --max-instances=1 \
  --allow-unauthenticated
```

## Verification
After deployment, `gcloud` will output the Service URL. Visit that URL to access the Research Frontend.
