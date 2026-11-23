# FolioScript

A private, on-device Android notes and scriptwriting app styled in a **Warm Paper & Ink** editorial theme.

## Features
- **Private Local Storage:** Powered by Room SQLite with automatic debounced saving—no sign-in or cloud backend required.
- **Quick Script Cues:** Insert `[SCENE]`, `ACTION:`, `DIALOGUE:`, and `[NOTE]` cues right at your cursor from a docked editor toolbar.
- **Editorial Read Mode:** Switch to a clean formatted script & note reading view anytime.
- **Warm Paper & Ink Theme:** Switch between **System**, **Warm Paper (Light)**, and **Warm Charcoal (Dark)** modes, paired with bundled *Newsreader*, *Plus Jakarta Sans*, and *JetBrains Mono* fonts.
- **Adaptive Layout:** Smooth phone navigation and a tablet List-Detail split pane.

## GitHub Actions CI & Downloading APKs
This repository includes `.github/workflows/android-ci.yml`, which automatically runs on every push to `main` / `master`:

1. **Instant Debug APK (No secrets required):**
   - Open your repository's **Actions** tab on GitHub.
   - Click the latest **Android CI & Release Build** run.
   - Scroll to **Artifacts** and download **`FolioScript-debug-apk`**, unzip it, and install the `.apk` on your Android phone.

2. **Signed Release APK & AAB (Optional):**
   - In GitHub, go to **Settings → Secrets and variables → Actions** and add:
     - `RELEASE_KEYSTORE_BASE64`: Base64-encoded JKS keystore containing key alias `upload`
     - `STORE_PASSWORD`: Password for the keystore file
     - `KEY_PASSWORD`: Password for the `upload` key
   - Re-run or push to `main` to generate **`FolioScript-release-apk`** and **`FolioScript-release-aab`** artifacts.
