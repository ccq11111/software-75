#!/bin/bash

echo "Building PurseAI for macOS..."

# Set variables
APP_NAME="PurseAI"
APP_VERSION="1.0.0"
MAIN_CLASS="com.example.loginapp.Launcher"
VENDOR="PurseAI"
ICON_PATH="src/main/resources/images/app_icon.png"
OUTPUT_DIR="target/installer"

# Create SVG icon if it doesn't exist
if [ ! -f "src/main/resources/images/app_icon.png" ]; then
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

# Create the macOS application
echo "Creating macOS application..."

# Build the jpackage command
JPACKAGE_CMD="jpackage --type dmg \
  --name \"$APP_NAME\" \
  --app-version \"$APP_VERSION\" \
  --vendor \"$VENDOR\" \
  --description \"PurseAI Financial Management Application\" \
  --input target/classes \
  --dest \"$OUTPUT_DIR\" \
  --main-jar ../login-register-app-1.0-SNAPSHOT.jar \
  --main-class \"$MAIN_CLASS\" \
  --mac-package-name \"$APP_NAME\" \
  --mac-package-identifier \"com.purseai.app\""

# Add icon if it exists
if [ -f "$ICON_PATH" ]; then
    JPACKAGE_CMD="$JPACKAGE_CMD \
  --icon \"$ICON_PATH\""
fi

# Execute the command
eval "$JPACKAGE_CMD"

if [ $? -ne 0 ]; then
    echo "Error: Failed to create macOS application."
    exit 1
fi

echo "macOS application created successfully at $OUTPUT_DIR/$APP_NAME-$APP_VERSION.dmg"
