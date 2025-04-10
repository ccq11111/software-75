@echo off
echo Building PurseAI application...

REM Detect the operating system
ver | find "Windows" > nul
if %ERRORLEVEL% equ 0 (
    echo Detected Windows operating system
    call build-windows.bat
) else (
    echo Unable to detect operating system.
    echo Please run one of the following scripts manually:
    echo   - build-windows.bat (for Windows)
    echo   - build-macos.sh (for macOS)
    echo   - build-linux.sh (for Linux)
    exit /b 1
)
