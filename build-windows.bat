@echo off
echo Building PurseAI for Windows...

REM Set variables
set APP_NAME=PurseAI
set APP_VERSION=1.0.0
set MAIN_CLASS=com.example.loginapp.Launcher
set VENDOR=PurseAI
set ICON_PATH=src/main/resources/images/app_icon.png
set OUTPUT_DIR=target/installer

REM Check if icon exists, if not, we'll proceed without an icon
if not exist "%ICON_PATH%" (
    echo Warning: Icon file not found at %ICON_PATH%
    echo Will proceed without an icon.
    set "ICON_OPTION="
) else (
    set "ICON_OPTION=--icon "%ICON_PATH%""
)

REM Clean and package the application
echo Building JAR file...
call mvn clean package

REM Create output directory if it doesn't exist
if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

REM Check if jpackage is available
where jpackage >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo Error: jpackage not found. Make sure you have JDK 14 or later installed and in your PATH.
    exit /b 1
)

REM Create the Windows executable
echo Creating Windows executable...

REM Build the jpackage command
set JPACKAGE_CMD=jpackage --type exe ^
  --name "%APP_NAME%" ^
  --app-version "%APP_VERSION%" ^
  --vendor "%VENDOR%" ^
  --description "PurseAI Financial Management Application" ^
  --input target/classes ^
  --dest "%OUTPUT_DIR%" ^
  --main-jar ../login-register-app-1.0-SNAPSHOT.jar ^
  --main-class "%MAIN_CLASS%" ^
  --win-dir-chooser ^
  --win-shortcut ^
  --win-menu ^
  --win-menu-group "%APP_NAME%"

REM Add icon if it exists
if defined ICON_OPTION (
    set JPACKAGE_CMD=%JPACKAGE_CMD% %ICON_OPTION%
)

REM Execute the command
%JPACKAGE_CMD%

if %ERRORLEVEL% neq 0 (
    echo Error: Failed to create Windows executable.
    exit /b 1
)

echo Windows executable created successfully at %OUTPUT_DIR%\%APP_NAME%-%APP_VERSION%.exe
