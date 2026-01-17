# Architecture & Design

## Package Structure

The project follows a standard Android MVVM architecture with Jetpack Compose:

- `com/canh0chua/downloadsorganizer`
    - `MainActivity.kt`: Entry point and navigation host.
    - `model/`: Data classes and enums (e.g., `FileItem`, `FileType`).
    - `ui/`: Compose screens and components.
        - `theme/`: Theme definitions (Material 3).
        - `DashboardScreen.kt`: The main overview screen.
        - `DetailScreen.kt`: Category-specific file listing.
    - `viewmodel/`: Business logic and state management for file operations.
    - `DownloadsOrganizerApplication.kt`: Custom application class for global Coil configuration.

## Key Design Decisions

### Package Renaming
The project was refactored from `com.example.downloadsorganizer` to `com.canh0chua.downloadsorganizer` to ensure a unique namespace and resolve `ClassNotFoundException` during deployment.

### File Operations
- **Categorization**: Files are categorized by extension (see `FileViewModel.getFileType`). Support includes standard types plus HEIC and various APK package formats. Files in the root of `Downloads` or `Quick Share` are specially marked as "Unsorted".
- **Scanning**: Recursive scan of `Downloads` and `Quick Share`. `scanFiles()` in `FileViewModel` returns a `Job` to allow synchronization with other operations.
- **Organization (Repair)**: The `organizeFiles()` function can "repair" the organization by moving files that are already in a category folder (like `Other`) to their correct home if they are miscategorized. This ensures disk contents sync with the dashboard.
- **Sorting**: User-selectable sort order (Name, Date, Size) implemented in `FileViewModel`, defaulting to Date Descending.
- **Lifecycle Management**: The app automatically finishes its activity when the user leaves (`onStop`), ensuring zero background execution and better privacy.
- **Improved Thumbnails**:
    - **Images**: Standard Coil `AsyncImage` loading.
    - **Videos**: Powered by `coil-video` with `VideoFrameDecoder` configured in `DownloadsOrganizerApplication`.
    - **APKs**: Custom `ApkIconFetcher` implemented to extract and display the actual application icon from the archive.
- **File Management**:
    - **Rename**: Uses standard `java.io.File.renameTo`.
    - **Multi-Delete**: Logic in `FileViewModel` handles batch deletion of `FileItem` objects.
    - **Cleanup**: Iterates through categorized files and filters by `lastModified` timestamp compared to the current system time minus the user-defined threshold.

## Work Profile Limitations

Android's work profile is designed to isolate work data from personal data.
- **Scope**: The app can ONLY access and organize files within the profile it is installed in (Personal or Work).
- **Security**: Cross-profile file access is restricted by the Android OS.

## Permissions

The app requires `MANAGE_EXTERNAL_STORAGE` on Android 11+ (API 30+) to perform file moves across the Downloads directory.
