# MetaClean Android Project - Copilot Instructions

## Project Overview
MetaClean is a professional Android app for viewing and removing metadata from photos and videos. Focus on privacy, offline-first, and user-friendly design.

## Architecture
- Language: Kotlin
- UI Framework: Jetpack Compose with Material You
- Architecture: MVVM
- Build System: Gradle

## Key Features
- ✅ Metadata viewing (EXIF, IPTC, XMP)
- ✅ Metadata removal with batch processing
- ✅ Offline-only (no internet permissions)
- ✅ App lock with biometric authentication
- ✅ Share-target integration
- ✅ Preset profiles for different use cases
- ✅ RAW/HEIC/Video support
- ✅ History and undo functionality

## Development Guidelines
- Keep code clean and well-documented
- Follow Material Design 3 guidelines
- Ensure zero data leaves the device
- Preserve image quality (no recompression)
- Use ExifInterface and metadata-extractor libraries
