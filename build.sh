#!/bin/bash

# Make the OS-specific build scripts executable
chmod +x build-macos.sh
chmod +x build-linux.sh

# Detect the operating system
OS="$(uname -s)"
case "${OS}" in
    Linux*)     
        echo "Detected Linux operating system"
        ./build-linux.sh
        ;;
    Darwin*)    
        echo "Detected macOS operating system"
        ./build-macos.sh
        ;;
    CYGWIN*|MINGW*|MSYS*)
        echo "Detected Windows operating system"
        ./build-windows.bat
        ;;
    *)          
        echo "Unknown operating system: ${OS}"
        echo "Please run one of the following scripts manually:"
        echo "  - build-windows.bat (for Windows)"
        echo "  - build-macos.sh (for macOS)"
        echo "  - build-linux.sh (for Linux)"
        exit 1
        ;;
esac
