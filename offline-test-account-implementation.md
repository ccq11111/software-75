# Offline Test Account Implementation

## Overview

We've implemented a fully offline test account functionality for the PurseAI application. This allows users to log in with the test account (username: "test", password: "test") without requiring any network connection to the backend.

## Key Changes

1. **LoginRegisterController**
   - Added special handling for the test account login
   - Created a separate method for handling successful login
   - Bypasses the API call completely for the test account

2. **ApiServiceFactory**
   - Updated documentation to clarify the behavior for different account types
   - Maintains the existing logic for returning mock implementations for the test account

3. **Mock Service Implementations**
   - Updated documentation to clarify that they work completely offline
   - All mock implementations use in-memory data structures and don't make any network calls

4. **API Base URL**
   - Updated all real API service implementations to use "http://127.0.0.1:8080/v1" as the base URL

## Testing Instructions

To test the offline functionality:
1. Run the application
2. Enter username "test" and password "test"
3. The application should log in successfully without attempting to connect to any backend
4. All features should work with mock data

For regular accounts, the application will attempt to connect to the backend at http://127.0.0.1:8080/v1.

## Benefits

- Allows for easy testing and demonstration of the frontend without requiring a running backend
- Provides a consistent test environment with predefined mock data
- Ensures the application can be used for demos even without network connectivity
- Maintains the ability to connect to a real backend for non-test accounts

## Future Improvements

- Add more comprehensive mock data for a better testing experience
- Implement caching for regular accounts to improve offline capabilities
- Add visual indicators to show when the application is in offline/test mode
