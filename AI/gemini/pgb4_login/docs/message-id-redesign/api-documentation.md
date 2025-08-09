# Message ID Redesign - API Documentation

## Overview

This document describes the API changes required for the Message ID redesign feature. The system has been updated to use 36-character string IDs instead of Long integer IDs for messages.

## API Changes Summary

### Message ID Format Change

**Before:**
- Message IDs were Long integers (e.g., `1`, `2`, `123`)
- Auto-incremented by database

**After:**
- Message IDs are 36-character strings (e.g., `A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6`)
- Generated using custom algorithm with format: `XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX`
- Character set: A-Z, 0-9 (uppercase letters and digits only)

### Affected API Endpoints

#### 1. GET /api/v1/messages/{messageId}

**Path Parameter Changes:**
- `messageId`: Changed from `integer (int64)` to `string (36 characters)`

**Example Request:**
```
GET /api/v1/messages/A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6
```

**Response Format (unchanged):**
```json
{
  "id": "A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6",
  "userId": "user123",
  "content": "Hello world!",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

#### 2. PUT /api/v1/messages/{messageId}

**Path Parameter Changes:**
- `messageId`: Changed from `integer (int64)` to `string (36 characters)`

**Example Request:**
```
PUT /api/v1/messages/A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6
Content-Type: application/json

{
  "content": "Updated message content"
}
```

#### 3. DELETE /api/v1/messages/{messageId}

**Path Parameter Changes:**
- `messageId`: Changed from `integer (int64)` to `string (36 characters)`

**Example Request:**
```
DELETE /api/v1/messages/A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6
```

#### 4. GET /api/v1/messages (List all messages)

**Response Changes:**
- Message objects in the response now contain string IDs instead of integer IDs

**Example Response:**
```json
{
  "content": [
    {
      "id": "A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6",
      "userId": "user123",
      "content": "First message",
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-01-15T10:30:00Z"
    },
    {
      "id": "B2C3D4E5-F6G7-H8I9-J0K1-L2M3N4O5P6Q7",
      "userId": "user456",
      "content": "Second message",
      "createdAt": "2024-01-15T09:15:00Z",
      "updatedAt": "2024-01-15T09:15:00Z"
    }
  ],
  "totalElements": 2,
  "totalPages": 1,
  "number": 0,
  "size": 10,
  "first": true,
  "last": true,
  "empty": false
}
```

#### 5. GET /api/v1/users/{userId}/messages

**Response Changes:**
- Message objects in the response now contain string IDs instead of integer IDs
- Same format as the general message list endpoint

### Error Handling

#### New Error Response: Invalid Message ID Format

**HTTP Status Code:** 400 Bad Request

**Response Body:**
```json
{
  "error": "INVALID_MESSAGE_ID",
  "message": "Invalid message ID format: {provided_id}",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

**Triggered When:**
- Message ID doesn't match the expected 36-character format
- Message ID contains invalid characters (not A-Z or 0-9)
- Message ID doesn't follow the pattern: `XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX`

### ID Validation Rules

1. **Length:** Exactly 36 characters
2. **Format:** `XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX` (8-4-4-4-12 pattern with hyphens)
3. **Character Set:** Only uppercase letters (A-Z) and digits (0-9)
4. **Uniqueness:** Each ID is globally unique across the system

### Backward Compatibility

**Important:** This is a breaking change. Old integer-based message IDs will no longer work after the migration. Clients must be updated to handle the new string-based ID format.

### Client Implementation Notes

1. **URL Construction:** When constructing URLs with message IDs, ensure proper URL encoding if necessary
2. **Storage:** Store message IDs as strings in client-side storage
3. **Validation:** Implement client-side validation using the regex pattern: `^[A-Z0-9]{8}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{12}$`
4. **Error Handling:** Handle the new `INVALID_MESSAGE_ID` error response appropriately

### Testing Considerations

1. **Valid ID Examples:**
   - `A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6`
   - `Z9Y8X7W6-V5U4-T3S2-R1Q0-P9O8N7M6L5K4`

2. **Invalid ID Examples:**
   - `123` (old format)
   - `a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6` (lowercase)
   - `A1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6Q7R8` (no hyphens)
   - `A1B2C3D4-E5F6-G7H8-I9J0` (too short)