Feature: Message Management
  As a user of the message board
  I want to be able to create, view, and manage messages
  So that I can share my thoughts and see others' messages

  Scenario: Successfully create a new message
    Given the message board is running
    When a user "testUser" creates a message with content "Hello BDD!"
    Then the message should be successfully created
    And the task status for message creation should be COMPLETED

  Scenario: Retrieve messages by user ID with pagination
    Given the message board has messages from "userA" and "userB"
      | userId | content         |
      | userA  | Message from A1 |
      | userA  | Message from A2 |
      | userB  | Message from B1 |
    When I request messages for user "userA" with page 0 and size 10
    Then I should receive 2 messages
    And the first message content should be "Message from A1"
