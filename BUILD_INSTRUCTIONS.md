# PurseAI Build Instructions

This document provides instructions on how to build the PurseAI application as an executable for different operating systems.

## Prerequisites

- JDK 14 or later (with jpackage tool)
- Maven 3.6 or later
- For Windows: Windows 10 or later
- For macOS: macOS 10.15 (Catalina) or later
- For Linux: A modern Linux distribution (Ubuntu 20.04, Fedora 32, etc.)
- Optional: ImageMagick for icon conversion (if not available, a default icon will be used)

## Building the Application

### Using the Unified Build Script

The easiest way to build the application is to use the unified build script, which will detect your operating system and run the appropriate build script:

#### On macOS/Linux:

```bash
chmod +x build.sh
./build.sh
```

#### On Windows:

```batch
build.bat
```

### Building for Specific Platforms

If you want to build for a specific platform, you can use the platform-specific build scripts:

#### For Windows:

```batch
build-windows.bat
```

#### For macOS:

```bash
chmod +x build-macos.sh
./build-macos.sh
```

#### For Linux:

```bash
chmod +x build-linux.sh
./build-linux.sh
```

## Output

The built application will be available in the `target/installer` directory:

- Windows: `PurseAI-1.0.0.exe`
- macOS: `PurseAI-1.0.0.dmg`
- Linux: `purseai_1.0.0-1_amd64.deb` and/or `purseai-1.0.0-1.x86_64.rpm`

## Troubleshooting

### jpackage Not Found

If you get an error saying "jpackage not found", make sure you have JDK 14 or later installed and that it's in your PATH.

On macOS, you can install it with:
```bash
brew install openjdk@17
```

On Ubuntu/Debian:
```bash
sudo apt-get install openjdk-17-jdk
```

On Windows, download and install from the [AdoptOpenJDK](https://adoptopenjdk.net/) website.

### Missing Dependencies on Linux

On Linux, you might need to install additional packages for jpackage to work:

For Debian/Ubuntu:
```bash
sudo apt-get install fakeroot
```

For RPM-based distributions (Fedora, CentOS):
```bash
sudo dnf install rpm-build
```

### Icon Issues

If you encounter issues with the application icon, make sure you have the correct icon format for your platform:

- Windows: `.ico` file
- macOS: `.icns` file
- Linux: `.png` file

## Converting the SVG Icon

The application includes an SVG icon that needs to be converted to platform-specific formats:

### For Windows (.ico):

You can use online converters or tools like ImageMagick:
```bash
convert -background none -density 256x256 src/main/resources/images/app_icon.svg src/main/resources/images/app_icon.ico
```

### For macOS (.icns):

On macOS, you can use the `iconutil` command:
1. Create a temporary directory: `mkdir -p tmp.iconset`
2. Convert the SVG to PNG files of different sizes:
   ```bash
   for size in 16 32 64 128 256 512; do
     convert -background none -density ${size}x${size} src/main/resources/images/app_icon.svg tmp.iconset/icon_${size}x${size}.png
     convert -background none -density $((size*2))x$((size*2)) src/main/resources/images/app_icon.svg tmp.iconset/icon_${size}x${size}@2x.png
   done
   ```
3. Create the .icns file: `iconutil -c icns tmp.iconset -o src/main/resources/images/app_icon.icns`
4. Clean up: `rm -rf tmp.iconset`

### For Linux (.png):

```bash
convert -background none -density 512x512 src/main/resources/images/app_icon.svg src/main/resources/images/app_icon.png
```
