# PurseAI Backend API Interface Document

This document defines the backend APIs required to support the PurseAI application's frontend functionality. The APIs are organized by feature area.

## Base URL

All API endpoints should be prefixed with:

```
https://api.purseai.com/v1
```

## Authentication

### User Registration

**Endpoint:** `POST /auth/register`

**Description:** Register a new user account

**Request Body:**
```json
{
  "username": "string",
  "password": "string",
  "email": "string",
  "phone": "string"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Registration successful",
  "userId": "string",
  "token": "string"
}
```

**Error Responses:**
- 400: Bad Request - Missing required fields or validation errors
- 409: Conflict - Account already exists

### User Login

**Endpoint:** `POST /auth/login`

**Description:** Authenticate a user and get access token

**Request Body:**
```json
{
  "username": "string",
  "password": "string"
}
```

**Response:**
```json
{
  "success": true,
  "userId": "string",
  "username": "string",
  "token": "string",
  "expiresIn": "number"
}
```

**Error Responses:**
- 400: Bad Request - Missing required fields
- 401: Unauthorized - Invalid credentials

### Verification Code

**Endpoint:** `POST /auth/verification`

**Description:** Verify a user's email or phone using a verification code

**Request Body:**
```json
{
  "userId": "string",
  "code": "string",
  "type": "email|phone"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Verification successful"
}
```

**Error Responses:**
- 400: Bad Request - Missing required fields
- 401: Unauthorized - Invalid verification code

## Saving Plans

### Create Saving Plan

**Endpoint:** `POST /savings/plans`

**Description:** Create a new saving plan

**Request Headers:**
- Authorization: Bearer {token}

**Request Body:**
```json
{
  "name": "string",
  "startDate": "string (ISO date)",
  "cycle": "string (Daily|Weekly|Monthly|Quarterly|Yearly)",
  "cycleTimes": "number",
  "amount": "number",
  "currency": "string"
}
```

**Response:**
```json
{
  "success": true,
  "planId": "string",
  "name": "string",
  "startDate": "string (ISO date)",
  "endDate": "string (ISO date)",
  "cycle": "string",
  "cycleTimes": "number",
  "amount": "number",
  "totalAmount": "number",
  "currency": "string",
  "savedAmount": "number"
}
```

**Error Responses:**
- 400: Bad Request - Missing required fields or validation errors
- 401: Unauthorized - Invalid or missing token

### Get Saving Plans

**Endpoint:** `GET /savings/plans`

**Description:** Get all saving plans for the authenticated user

**Request Headers:**
- Authorization: Bearer {token}

**Response:**
```json
{
  "success": true,
  "plans": [
    {
      "planId": "string",
      "name": "string",
      "startDate": "string (ISO date)",
      "endDate": "string (ISO date)",
      "cycle": "string",
      "cycleTimes": "number",
      "amount": "number",
      "totalAmount": "number",
      "currency": "string",
      "savedAmount": "number"
    }
  ]
}
```

**Error Responses:**
- 401: Unauthorized - Invalid or missing token

### Update Saving Plan

**Endpoint:** `PUT /savings/plans/{planId}`

**Description:** Update an existing saving plan

**Request Headers:**
- Authorization: Bearer {token}

**Request Body:**
```json
{
  "name": "string",
  "startDate": "string (ISO date)",
  "cycle": "string",
  "cycleTimes": "number",
  "amount": "number",
  "currency": "string",
  "savedAmount": "number"
}
```

**Response:**
```json
{
  "success": true,
  "planId": "string",
  "name": "string",
  "startDate": "string (ISO date)",
  "endDate": "string (ISO date)",
  "cycle": "string",
  "cycleTimes": "number",
  "amount": "number",
  "totalAmount": "number",
  "currency": "string",
  "savedAmount": "number"
}
```

**Error Responses:**
- 400: Bad Request - Missing required fields or validation errors
- 401: Unauthorized - Invalid or missing token
- 404: Not Found - Plan not found

### Delete Saving Plan

**Endpoint:** `DELETE /savings/plans/{planId}`

**Description:** Delete a saving plan

**Request Headers:**
- Authorization: Bearer {token}

**Response:**
```json
{
  "success": true,
  "message": "Plan deleted successfully"
}
```

**Error Responses:**
- 401: Unauthorized - Invalid or missing token
- 404: Not Found - Plan not found

## Billing

### Create Billing Entry

**Endpoint:** `POST /billing/entries`

**Description:** Create a new billing entry

**Request Headers:**
- Authorization: Bearer {token}

**Request Body:**
```json
{
  "category": "string",
  "product": "string",
  "price": "number",
  "date": "string (ISO date)",
  "time": "string (HH:MM)",
  "remark": "string"
}
```

**Response:**
```json
{
  "success": true,
  "entryId": "string",
  "category": "string",
  "product": "string",
  "price": "number",
  "date": "string (ISO date)",
  "time": "string (HH:MM)",
  "formattedTime": "string",
  "remark": "string"
}
```

**Error Responses:**
- 400: Bad Request - Missing required fields or validation errors
- 401: Unauthorized - Invalid or missing token

### Get Billing Entries

**Endpoint:** `GET /billing/entries`

**Description:** Get all billing entries for the authenticated user

**Request Headers:**
- Authorization: Bearer {token}

**Query Parameters:**
- startDate: string (ISO date, optional)
- endDate: string (ISO date, optional)
- category: string (optional)
- searchTerm: string (optional)

**Response:**
```json
{
  "success": true,
  "entries": [
    {
      "entryId": "string",
      "category": "string",
      "product": "string",
      "price": "number",
      "date": "string (ISO date)",
      "time": "string (HH:MM)",
      "formattedTime": "string",
      "remark": "string"
    }
  ]
}
```

**Error Responses:**
- 401: Unauthorized - Invalid or missing token

### Update Billing Entry

**Endpoint:** `PUT /billing/entries/{entryId}`

**Description:** Update an existing billing entry

**Request Headers:**
- Authorization: Bearer {token}

**Request Body:**
```json
{
  "category": "string",
  "product": "string",
  "price": "number",
  "date": "string (ISO date)",
  "time": "string (HH:MM)",
  "remark": "string"
}
```

**Response:**
```json
{
  "success": true,
  "entryId": "string",
  "category": "string",
  "product": "string",
  "price": "number",
  "date": "string (ISO date)",
  "time": "string (HH:MM)",
  "formattedTime": "string",
  "remark": "string"
}
```

**Error Responses:**
- 400: Bad Request - Missing required fields or validation errors
- 401: Unauthorized - Invalid or missing token
- 404: Not Found - Entry not found

### Delete Billing Entry

**Endpoint:** `DELETE /billing/entries/{entryId}`

**Description:** Delete a billing entry

**Request Headers:**
- Authorization: Bearer {token}

**Response:**
```json
{
  "success": true,
  "message": "Entry deleted successfully"
}
```

**Error Responses:**
- 401: Unauthorized - Invalid or missing token
- 404: Not Found - Entry not found

### Import Billing Entries from CSV

**Endpoint:** `POST /billing/import/csv`

**Description:** Import billing entries from a CSV file

**Request Headers:**
- Authorization: Bearer {token}
- Content-Type: multipart/form-data

**Request Body:**
- file: CSV file

**Response:**
```json
{
  "success": true,
  "message": "Import successful",
  "entriesImported": "number",
  "entriesSkipped": "number"
}
```

**Error Responses:**
- 400: Bad Request - Invalid file format
- 401: Unauthorized - Invalid or missing token

## Summary

### Get Expenditure Summary

**Endpoint:** `GET /summary/expenditure`

**Description:** Get expenditure summary for the authenticated user

**Request Headers:**
- Authorization: Bearer {token}

**Query Parameters:**
- period: string (Week|Month|Year)

**Response:**
```json
{
  "success": true,
  "period": "string",
  "total": "number",
  "categories": [
    {
      "category": "string",
      "amount": "number",
      "percentage": "number"
    }
  ]
}
```

**Error Responses:**
- 400: Bad Request - Invalid period
- 401: Unauthorized - Invalid or missing token

### Get Income Summary

**Endpoint:** `GET /summary/income`

**Description:** Get income summary for the authenticated user

**Request Headers:**
- Authorization: Bearer {token}

**Query Parameters:**
- period: string (Week|Month|Year)

**Response:**
```json
{
  "success": true,
  "period": "string",
  "total": "number",
  "categories": [
    {
      "category": "string",
      "amount": "number",
      "percentage": "number"
    }
  ]
}
```

**Error Responses:**
- 400: Bad Request - Invalid period
- 401: Unauthorized - Invalid or missing token

## AI Assistant

### Get AI Advice

**Endpoint:** `POST /ai/advice`

**Description:** Get financial advice from the AI assistant

**Request Headers:**
- Authorization: Bearer {token}

**Request Body:**
```json
{
  "message": "string",
  "context": {
    "includeTransactions": "boolean",
    "includeSavings": "boolean",
    "includeSummary": "boolean"
  }
}
```

**Response:**
```json
{
  "success": true,
  "message": "string",
  "suggestions": [
    {
      "type": "string",
      "text": "string"
    }
  ]
}
```

**Error Responses:**
- 400: Bad Request - Missing required fields
- 401: Unauthorized - Invalid or missing token

### Get Quick Actions

**Endpoint:** `GET /ai/quick-actions`

**Description:** Get available quick actions for the AI assistant

**Request Headers:**
- Authorization: Bearer {token}

**Response:**
```json
{
  "success": true,
  "actions": [
    {
      "id": "string",
      "text": "string"
    }
  ]
}
```

**Error Responses:**
- 401: Unauthorized - Invalid or missing token

## User Settings

### Get User Settings

**Endpoint:** `GET /users/settings`

**Description:** Get user settings

**Request Headers:**
- Authorization: Bearer {token}

**Response:**
```json
{
  "success": true,
  "settings": {
    "username": "string",
    "email": "string",
    "phone": "string",
    "currency": "string",
    "language": "string",
    "notifications": {
      "email": "boolean",
      "push": "boolean"
    }
  }
}
```

**Error Responses:**
- 401: Unauthorized - Invalid or missing token

### Update User Settings

**Endpoint:** `PUT /users/settings`

**Description:** Update user settings

**Request Headers:**
- Authorization: Bearer {token}

**Request Body:**
```json
{
  "email": "string",
  "phone": "string",
  "currency": "string",
  "language": "string",
  "notifications": {
    "email": "boolean",
    "push": "boolean"
  }
}
```

**Response:**
```json
{
  "success": true,
  "settings": {
    "username": "string",
    "email": "string",
    "phone": "string",
    "currency": "string",
    "language": "string",
    "notifications": {
      "email": "boolean",
      "push": "boolean"
    }
  }
}
```

**Error Responses:**
- 400: Bad Request - Invalid settings
- 401: Unauthorized - Invalid or missing token

## Error Handling

All API responses should follow a consistent error format:

```json
{
  "success": false,
  "error": {
    "code": "string",
    "message": "string",
    "details": "object (optional)"
  }
}
```

Common error codes:
- `INVALID_REQUEST`: Missing or invalid parameters
- `UNAUTHORIZED`: Authentication required or invalid token
- `FORBIDDEN`: Insufficient permissions
- `NOT_FOUND`: Resource not found
- `INTERNAL_ERROR`: Server error

## Data Models

### User
- userId: string
- username: string
- email: string
- phone: string
- password: string (hashed, never returned)
- settings: object

### SavingPlan
- planId: string
- userId: string
- name: string
- startDate: date
- cycle: string
- cycleTimes: number
- amount: number
- currency: string
- savedAmount: number

### BillingEntry
- entryId: string
- userId: string
- category: string
- product: string
- price: number
- date: date
- time: string
- remark: string

## Security Considerations

1. All API endpoints must be accessed over HTTPS
2. Authentication is required for all endpoints except registration and login
3. JWT tokens should be used for authentication
4. Tokens should expire after a reasonable time (e.g., 24 hours)
5. Sensitive data should be encrypted in transit and at rest
6. Input validation should be performed on all request parameters
7. Rate limiting should be implemented to prevent abuse
