# Changelog

## [1.0.0] - 2026-01-17
### Added
- Initial implementation of Downloads Organizer app.
- File scanning for Downloads and Quick Share folders.
- Categorization of files (Image, Video, Audio, Doc, PDF, APK, Other).
- Organization feature to move files into sub-folders.
- Dashboard with Material 3 Grid layout.
- Detail view with Coil image loading and FileProvider integration.
- MANAGE_EXTERNAL_STORAGE permission handling.
- Jetpack Compose Navigation.
- **Advanced Thumbnails**:
    - Extracted video frames using `coil-video`.
    - Real-time APK icon fetching for app packages.
- **Categorization Refinements**:
    - Added support for `.heic` image files.
    - Added support for various APK formats (`.xapk`, `.apkm`, `.xapkm`, `.eapk`).
- **Scanning & Organization Improvements**:
    - Restored full recursive scanning for accurate file counting.
    - Added "Repair Organization" to re-categorize files already in sub-folders.
    - Synchronized scan and organization operations via coroutines.
    - **Fully automated Quick Share sorting**: Files in the `Quick Share` folder and the root of `Downloads` are now automatically sorted into their respective category sub-folders during organization.
- **Improved UX**:
    - Automatic navigation back to dashboard after permission grant.
    - Prominent file counts on dashboard with adjusted typography.
    - Contextual top bar actions (Rename, Multi-Delete) in detail view.
    - Age-based cleanup dialog for bulk file removal.
- **Automation**:
    - Added `Makefile` for standard build/install tasks.
    - Added ` release.sh` for automated APK release and archival on-device.
- **Enhanced File Detail**:
    - Added human-readable timestamps to file list items.
    - Implemented a sorting menu (Name, Date, Size) with Date as the default.
- **Improved Dashboard**:
    - Re-ordered tiles to pin "Downloads" and "Quick Share" (incoming) to the top.
    - Redesigned tiles with a badge-style file count in the top-right corner.
    - Optimized typography for a cleaner, premium fit.
- **Privacy & Lifecycle**:
    - App now terminates itself on stop to prevent background execution.
