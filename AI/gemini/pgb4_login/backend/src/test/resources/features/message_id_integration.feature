Feature: Message ID Integration with New Format
  As a user of the message board system
  I want to work with messages using the new 36-character ID format
  So that I can reliably create, access, and manage messages with improved security and scalability

  Background:
    Given the message board system is running with new ID format support

  Scenario: Create message with new ID format
    When I create a message with user "testUser" and content "Hello New ID Format!"
    Then the message should be created successfully
    And the message ID should be in the new 36-character format
    And the message ID should contain only uppercase letters and numbers
    And the message ID should follow the pattern "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX"

  Scenario: Retrieve message using new ID format
    Given I have created a message with user "retrieveUser" and content "Message to retrieve"
    When I retrieve the message using its new format ID
    Then I should get the correct message content
    And the response should contain the new format ID
    And all timestamps should be properly formatted

  Scenario: Update message using new ID format
    Given I have created a message with user "updateUser" and content "Original content"
    When I update the message content to "Updated content" using its new format ID
    Then the message should be updated successfully
    And the message ID should remain unchanged
    And the updated content should be reflected when retrieving the message

  Scenario: Delete message using new ID format
    Given I have created a message with user "deleteUser" and content "Message to delete"
    When I delete the message using its new format ID
    Then the message should be deleted successfully
    And retrieving the message should return not found
    And the message should not exist in the database

  Scenario: Handle invalid ID formats gracefully
    When I try to retrieve a message with invalid ID "invalid-id-format"
    Then I should receive a bad request error
    And the error message should indicate invalid ID format
    And the error code should be "INVALID_MESSAGE_ID"

  Scenario Outline: Validate various invalid ID formats
    When I try to retrieve a message with invalid ID "<invalidId>"
    Then I should receive a bad request error
    And the error message should indicate invalid ID format

    Examples:
      | invalidId                                    |
      | 123                                         |
      | invalid-id                                  |
      | abcd1234-efgh-5678-ijkl-mnopqrstuvwx       |
      | ABCD@#$%-EFGH-5678-IJKL-MNOPQRSTUVWX       |
      | ABCD1234-EFGH-5678-IJKL                    |
      | ABCD1234EFGH5678IJKLMNOPQRSTUVWX           |
      | ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWXYZ-EXTRA |

  Scenario: List messages with new ID format
    Given I have created multiple messages:
      | userId | content          |
      | user1  | First message    |
      | user1  | Second message   |
      | user2  | Third message    |
    When I request all messages with pagination
    Then I should receive all messages
    And each message should have a valid new format ID
    And the messages should be ordered by creation time

  Scenario: Filter messages by user with new ID format
    Given I have created multiple messages:
      | userId | content              |
      | alice  | Alice's first message |
      | alice  | Alice's second message|
      | bob    | Bob's message        |
    When I request messages for user "alice"
    Then I should receive 2 messages
    And all messages should belong to user "alice"
    And each message should have a valid new format ID

  Scenario: Concurrent message creation should generate unique IDs
    When I create 10 messages concurrently with different users
    Then all messages should be created successfully
    And all message IDs should be unique
    And all message IDs should be in the new format

  Scenario: ID uniqueness across system restarts
    Given I have created a message and noted its ID
    When the system processes multiple new messages
    Then no new message should have the same ID as the original
    And all IDs should maintain the correct format

  Scenario: Performance test for ID operations
    When I perform 100 ID generation and validation operations
    Then all operations should complete within acceptable time limits
    And all generated IDs should be valid and unique

  Scenario: Error handling consistency across endpoints
    When I perform invalid ID operations on different endpoints:
      | endpoint | method | invalidId    |
      | messages | GET    | invalid-get  |
      | messages | PUT    | invalid-put  |
      | messages | DELETE | invalid-del  |
    Then all endpoints should return consistent error responses
    And all error responses should have the same structure

  Scenario: Message content integrity with new ID format
    Given I create a message with special characters in content "Hello! @#$%^&*()_+ 中文 🚀"
    When I retrieve the message using its new format ID
    Then the content should be exactly preserved
    And the ID should still be in valid format

  Scenario: Large content handling with new ID format
    Given I create a message with very long content (10000 characters)
    When I retrieve the message using its new format ID
    Then the full content should be preserved
    And the ID should be in valid format
    And the operation should complete successfully

  Scenario: Boundary testing for ID validation
    When I test ID validation with edge cases:
      | testCase              | expectedResult |
      | exactly 36 chars      | valid          |
      | 35 chars              | invalid        |
      | 37 chars              | invalid        |
      | correct pattern       | valid          |
      | missing dashes        | invalid        |
      | extra dashes          | invalid        |
      | wrong dash positions  | invalid        |
    Then the validation results should match expectations