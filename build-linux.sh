#!/bin/bash

echo "Building PurseAI for Linux..."

# Set variables
APP_NAME="PurseAI"
APP_VERSION="1.0.0"
MAIN_CLASS="com.example.loginapp.Launcher"
VENDOR="PurseAI"
ICON_PATH="src/main/resources/images/app_icon.png"
OUTPUT_DIR="target/installer"

# Create SVG icon if it doesn't exist
if [ ! -f "$ICON_PATH" ]; then
    echo "Creating app icon from SVG..."
    if command -v convert &> /dev/null; then
        convert -background none -density 512x512 src/main/resources/images/app_icon.svg src/main/resources/images/app_icon.png
        echo "Icon created successfully."
    else
        echo "Warning: ImageMagick not found. Using default icon."
        # Create a simple colored square as a fallback icon
        mkdir -p src/main/resources/images
        echo "<svg width='512' height='512'><rect width='512' height='512' fill='#FFD700'/></svg>" > src/main/resources/images/app_icon.svg
        # We'll proceed without an icon
        ICON_PATH=""
    fi
fi

# Clean and package the application
echo "Building JAR file..."
mvn clean package

# Create output directory if it doesn't exist
mkdir -p "$OUTPUT_DIR"

# Check if jpackage is available
if ! command -v jpackage &> /dev/null; then
    echo "Error: jpackage not found. Make sure you have JDK 14 or later installed and in your PATH."
    exit 1
fi

# Build the base jpackage command
BASE_CMD="jpackage \
  --name \"$APP_NAME\" \
  --app-version \"$APP_VERSION\" \
  --vendor \"$VENDOR\" \
  --description \"PurseAI Financial Management Application\" \
  --input target/classes \
  --dest \"$OUTPUT_DIR\" \
  --main-jar ../login-register-app-1.0-SNAPSHOT.jar \
  --main-class \"$MAIN_CLASS\" \
  --linux-shortcut \
  --linux-menu-group \"Finance\""

# Add icon if it exists
if [ -f "$ICON_PATH" ]; then
    ICON_OPTION="--icon \"$ICON_PATH\""
else
    ICON_OPTION=""
fi

# Create the Linux package (DEB format)
echo "Creating Linux DEB package..."
DEB_CMD="$BASE_CMD --type deb $ICON_OPTION"
eval "$DEB_CMD"

if [ $? -ne 0 ]; then
    echo "Error: Failed to create Linux DEB package."
    exit 1
fi

# Create the Linux package (RPM format)
echo "Creating Linux RPM package..."
RPM_CMD="$BASE_CMD --type rpm $ICON_OPTION"
eval "$RPM_CMD"

if [ $? -ne 0 ]; then
    echo "Warning: Failed to create Linux RPM package. This is expected if RPM tools are not installed."
fi

echo "Linux packages created successfully at $OUTPUT_DIR"
