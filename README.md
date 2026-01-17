# Downloads Organizer

A Material 3 Android application designed to keep your Downloads folder clean and organized.

## Features

- **Categorized Scanning**: Automatically identifies and categorizes files into Images, Videos, Audio, Documents, PDFs, APKs, and more (including HEIC and APK variants).
- **Recursive Scan**: Deeply scans your Downloads and Quick Share folders to find files nested in sub-directories.
- **Advanced Thumbnails**: High-quality thumbnails for images, extracted video frames, and actual app icons for APK files.
- **One-Click Organization**: Moves files from the Downloads root into neatly labeled sub-folders based on their category.
- **File Management**: Rename files, bulk delete selection, and age-based cleanup.
- **Sorting & Details**: Show file timestamps and sort by Name, Date, or Size (defaults to modified date).
- **Unsorted Tiles**: Dedicated dashboard tiles for files still sitting in the `Downloads` or `Quick Share` root.
- **Privacy Mode**: Automatically terminates when backgrounded for enhanced security and zero battery impact.

## 📥 Download

You can download the latest APK from the [Final Release](https://github.com/canh0chua/Downloads-Organizer-for-Android/releases/tag/final).

## Installation

1.  Clone the repository.
2.  Open in Android Studio.
3.  Connect an Android device (API 21+).
4.  Run `make build` or `./gradlew assembleDebug` to build the APK.
5.  Run `make install` to install it on your device.

## Release Automation

To build, install, and archive the APK on your phone's download folder in one command:
```bash
make release
```
This will:
- Build a fresh APK.
- Install it to your connected device.
- Push a timestamped version (e.g., `DownloadsOrganizer-v1.0-20260117.apk`) to your phone's `/sdcard/Download` folder.

## Usage

1.  Launch the app and grant the "All Files Access" permission.
2.  View the dashboard to see a count of files in each category.
3.  Click "Organize Files" to move them into category-specific folders within your Downloads directory.
4.  Navigate into categories to view and open specific files.

## Documentation

- [Architecture & Design](docs/ARCHITECTURE.md)
- [Changelog](CHANGELOG.md)
