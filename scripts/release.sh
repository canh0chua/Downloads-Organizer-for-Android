#!/bin/bash

# Release script for Downloads Organizer
# Builds, installs, and pushes the APK to the device downloads folder

# Set path to the build.gradle.kts file
BUILD_GRADLE="app/build.gradle.kts"

# Extract versionName from build.gradle.kts
VERSION=$(grep "versionName =" "$BUILD_GRADLE" | head -1 | awk -F '"' '{print $2}')

# Set timestamp
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Build the APK
echo "Building APK..."
./gradlew clean assembleDebug

if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
REMOTE_FILENAME="DownloadsOrganizer-v${VERSION}-${TIMESTAMP}.apk"

# Install the APK to the device
echo "Installing APK to device..."
adb install -r "$APK_PATH"

if [ $? -ne 0 ]; then
    echo "Installation failed!"
    exit 1
fi

# Push the APK to the device download folder
echo "Pushing APK to /sdcard/Download/${REMOTE_FILENAME}..."
adb push "$APK_PATH" "/sdcard/Download/${REMOTE_FILENAME}"

if [ $? -ne 0 ]; then
    echo "Push failed!"
    exit 1
fi

echo "Release successful!"
echo "Version: ${VERSION}"
echo "Remote Path: /sdcard/Download/${REMOTE_FILENAME}"
