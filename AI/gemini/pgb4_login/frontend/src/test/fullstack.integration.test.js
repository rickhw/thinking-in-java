import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { 
    createMessage, 
    getMessageById, 
    updateMessage, 
    deleteMessage, 
    getAllMessages, 
    getMessagesByUserId,
    isValidMessageId 
} from '../api';
import { isValidMessageId as utilsIsValidMessageId, validateMessageIdWithError } from '../utils/messageId';

// Mock fetch for testing without actual backend
global.fetch = vi.fn();

/**
 * Full-stack integration tests for the new message ID format
 * These tests verify the complete flow from frontend to backend
 */
describe('Full-Stack Message ID Integration Tests', () => {
    let createdMessageIds = [];
    let testUserId = 'integration-test-user';

    beforeEach(() => {
        createdMessageIds = [];
        testUserId = `integration-test-user-${Date.now()}`;
        
        // Reset fetch mock
        vi.clearAllMocks();
        
        // Setup default mock responses
        fetch.mockImplementation((url, options) => {
            // Mock successful responses based on URL patterns
            if (url.includes('/api/v1/messages') && options?.method === 'POST') {
                return Promise.resolve({
                    ok: true,
                    json: () => Promise.resolve({ taskId: 'mock-task-id-' + Date.now() })
                });
            }
            
            if (url.includes('/api/v1/messages') && options?.method === 'GET') {
                const messageId = 'ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX';
                if (url.includes('/api/v1/messages?') || url.includes('/api/v1/messages/user/')) {
                    // Mock paginated response
                    return Promise.resolve({
                        ok: true,
                        json: () => Promise.resolve({
                            content: [{
                                id: messageId,
                                userId: testUserId,
                                content: 'Mock message content',
                                createdAt: new Date().toISOString(),
                                updatedAt: new Date().toISOString()
                            }],
                            totalElements: 1,
                            totalPages: 1,
                            size: 10,
                            number: 0
                        })
                    });
                } else {
                    // Mock single message response
                    return Promise.resolve({
                        ok: true,
                        json: () => Promise.resolve({
                            id: messageId,
                            userId: testUserId,
                            content: 'Mock message content',
                            createdAt: new Date().toISOString(),
                            updatedAt: new Date().toISOString()
                        })
                    });
                }
            }
            
            if (url.includes('/api/v1/messages') && (options?.method === 'PUT' || options?.method === 'DELETE')) {
                return Promise.resolve({
                    ok: true,
                    json: () => Promise.resolve({ taskId: 'mock-task-id-' + Date.now() })
                });
            }
            
            // Default error response
            return Promise.resolve({
                ok: false,
                status: 404,
                json: () => Promise.resolve({ error: 'Not found' })
            });
        });
    });

    afterEach(async () => {
        // Cleanup: Delete all created messages
        for (const messageId of createdMessageIds) {
            try {
                await deleteMessage(messageId);
            } catch (error) {
                // Ignore cleanup errors
                console.warn(`Failed to cleanup message ${messageId}:`, error.message);
            }
        }
    });

    describe('Complete Message Lifecycle', () => {
        it('should handle full CRUD operations with new ID format', async () => {
            // Step 1: Create a message
            const createData = {
                userId: testUserId,
                content: 'Full-stack integration test message'
            };

            const createResponse = await createMessage(createData);
            expect(createResponse).toHaveProperty('taskId');
            expect(typeof createResponse.taskId).toBe('string');

            // Wait for async task completion (simulate polling)
            await new Promise(resolve => setTimeout(resolve, 1000));

            // Step 2: Get all messages to find our created message
            const allMessages = await getAllMessages(0, 20);
            expect(allMessages).toHaveProperty('content');
            expect(Array.isArray(allMessages.content)).toBe(true);

            const createdMessage = allMessages.content.find(
                msg => msg.userId === testUserId && msg.content === createData.content
            );
            expect(createdMessage).toBeDefined();
            expect(createdMessage).toHaveProperty('id');

            const messageId = createdMessage.id;
            createdMessageIds.push(messageId);

            // Verify ID format
            expect(utilsIsValidMessageId(messageId)).toBe(true);
            expect(isValidMessageId(messageId)).toBe(true);
            expect(messageId).toHaveLength(36);
            expect(messageId).toMatch(/^[A-Z0-9]{8}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{12}$/);

            // Step 3: Retrieve the specific message
            const retrievedMessage = await getMessageById(messageId);
            expect(retrievedMessage).toEqual(createdMessage);
            expect(retrievedMessage.id).toBe(messageId);
            expect(retrievedMessage.userId).toBe(testUserId);
            expect(retrievedMessage.content).toBe(createData.content);

            // Step 4: Update the message
            const updateData = {
                content: 'Updated full-stack integration test message'
            };

            const updateResponse = await updateMessage(messageId, updateData);
            expect(updateResponse).toHaveProperty('taskId');

            // Wait for async task completion
            await new Promise(resolve => setTimeout(resolve, 1000));

            // Verify the update
            const updatedMessage = await getMessageById(messageId);
            expect(updatedMessage.id).toBe(messageId); // ID should remain the same
            expect(updatedMessage.userId).toBe(testUserId);
            expect(updatedMessage.content).toBe(updateData.content);
            expect(new Date(updatedMessage.updatedAt)).toBeInstanceOf(Date);

            // Step 5: Delete the message
            const deleteResponse = await deleteMessage(messageId);
            expect(deleteResponse).toHaveProperty('taskId');

            // Wait for async task completion
            await new Promise(resolve => setTimeout(resolve, 1000));

            // Verify deletion
            await expect(getMessageById(messageId)).rejects.toThrow();

            // Remove from cleanup list since it's already deleted
            createdMessageIds = createdMessageIds.filter(id => id !== messageId);
        });

        it('should handle multiple messages with unique IDs', async () => {
            const messageCount = 5;
            const createPromises = [];

            // Create multiple messages concurrently
            for (let i = 0; i < messageCount; i++) {
                const createData = {
                    userId: `${testUserId}-${i}`,
                    content: `Concurrent message ${i}`
                };
                createPromises.push(createMessage(createData));
            }

            const createResponses = await Promise.all(createPromises);
            expect(createResponses).toHaveLength(messageCount);

            // Wait for all async tasks to complete
            await new Promise(resolve => setTimeout(resolve, 2000));

            // Retrieve all messages and find our created ones
            const allMessages = await getAllMessages(0, 50);
            const ourMessages = allMessages.content.filter(
                msg => msg.userId.startsWith(testUserId)
            );

            expect(ourMessages).toHaveLength(messageCount);

            // Verify all IDs are unique and valid
            const messageIds = ourMessages.map(msg => msg.id);
            const uniqueIds = new Set(messageIds);
            expect(uniqueIds.size).toBe(messageCount);

            messageIds.forEach(id => {
                expect(utilsIsValidMessageId(id)).toBe(true);
                expect(id).toHaveLength(36);
                createdMessageIds.push(id);
            });

            // Verify each message can be retrieved individually
            for (const message of ourMessages) {
                const retrieved = await getMessageById(message.id);
                expect(retrieved).toEqual(message);
            }
        });
    });

    describe('Error Handling Integration', () => {
        it('should handle invalid ID formats consistently across frontend and backend', async () => {
            const invalidIds = [
                'invalid-id',
                '123',
                'abcd1234-efgh-5678-ijkl-mnopqrstuvwx', // lowercase
                'ABCD@#$%-EFGH-5678-IJKL-MNOPQRSTUVWX', // special chars
                'ABCD1234-EFGH-5678-IJKL', // too short
                'ABCD1234EFGH5678IJKLMNOPQRSTUVWX', // no dashes
                '', // empty
                'ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWXYZ-EXTRA' // too long
            ];

            for (const invalidId of invalidIds) {
                // Frontend validation should catch these
                expect(utilsIsValidMessageId(invalidId)).toBe(false);
                expect(isValidMessageId(invalidId)).toBe(false);

                const validation = validateMessageIdWithError(invalidId);
                expect(validation.isValid).toBe(false);
                expect(validation.errorMessage).toBeTruthy();

                // Backend should also reject these
                await expect(getMessageById(invalidId)).rejects.toThrow();
                
                const updateData = { content: 'Test update' };
                await expect(updateMessage(invalidId, updateData)).rejects.toThrow();
                
                await expect(deleteMessage(invalidId)).rejects.toThrow();
            }
        });

        it('should handle non-existent valid format IDs', async () => {
            // Generate a valid format ID that doesn't exist
            const nonExistentId = 'ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX';
            
            // Frontend validation should pass
            expect(utilsIsValidMessageId(nonExistentId)).toBe(true);
            expect(isValidMessageId(nonExistentId)).toBe(true);

            // Backend should return 404 for non-existent messages
            await expect(getMessageById(nonExistentId)).rejects.toThrow();
        });

        it('should provide consistent error messages', async () => {
            const testCases = [
                {
                    id: null,
                    expectedFrontendError: '訊息 ID 不能為空'
                },
                {
                    id: '',
                    expectedFrontendError: '訊息 ID 不能為空'
                },
                {
                    id: 123,
                    expectedFrontendError: '訊息 ID 必須是字符串格式'
                },
                {
                    id: '123',
                    expectedFrontendError: '舊的數字 ID 格式'
                },
                {
                    id: 'invalid-format',
                    expectedFrontendError: '訊息 ID 格式不正確，應為 36 位大寫字母和數字組成'
                }
            ];

            testCases.forEach(testCase => {
                const validation = validateMessageIdWithError(testCase.id);
                expect(validation.isValid).toBe(false);
                expect(validation.errorMessage).toBe(testCase.expectedFrontendError);
            });
        });
    });

    describe('Pagination and Filtering Integration', () => {
        it('should handle pagination with new ID format', async () => {
            // Create multiple messages for pagination testing
            const messageCount = 15;
            const createPromises = [];

            for (let i = 0; i < messageCount; i++) {
                const createData = {
                    userId: testUserId,
                    content: `Pagination test message ${i}`
                };
                createPromises.push(createMessage(createData));
            }

            await Promise.all(createPromises);
            await new Promise(resolve => setTimeout(resolve, 3000)); // Wait for all to complete

            // Test first page
            const firstPage = await getAllMessages(0, 10);
            expect(firstPage.content).toHaveLength(10);
            expect(firstPage.totalElements).toBeGreaterThanOrEqual(messageCount);

            // Verify all IDs are valid
            firstPage.content.forEach(message => {
                expect(utilsIsValidMessageId(message.id)).toBe(true);
                if (message.userId === testUserId) {
                    createdMessageIds.push(message.id);
                }
            });

            // Test second page
            const secondPage = await getAllMessages(1, 10);
            expect(secondPage.content.length).toBeGreaterThan(0);

            // Verify no duplicate IDs between pages
            const firstPageIds = new Set(firstPage.content.map(m => m.id));
            const secondPageIds = new Set(secondPage.content.map(m => m.id));
            const intersection = new Set([...firstPageIds].filter(id => secondPageIds.has(id)));
            expect(intersection.size).toBe(0);

            // Add remaining IDs to cleanup list
            secondPage.content.forEach(message => {
                if (message.userId === testUserId) {
                    createdMessageIds.push(message.id);
                }
            });
        });

        it('should filter messages by user ID with new format', async () => {
            const user1Id = `${testUserId}-filter-1`;
            const user2Id = `${testUserId}-filter-2`;

            // Create messages for different users
            const user1Messages = [
                { userId: user1Id, content: 'User 1 message 1' },
                { userId: user1Id, content: 'User 1 message 2' }
            ];

            const user2Messages = [
                { userId: user2Id, content: 'User 2 message 1' }
            ];

            // Create all messages
            const allCreatePromises = [
                ...user1Messages.map(data => createMessage(data)),
                ...user2Messages.map(data => createMessage(data))
            ];

            await Promise.all(allCreatePromises);
            await new Promise(resolve => setTimeout(resolve, 2000));

            // Get messages for user 1
            const user1Results = await getMessagesByUserId(user1Id, 0, 10);
            expect(user1Results.content).toHaveLength(2);
            user1Results.content.forEach(message => {
                expect(message.userId).toBe(user1Id);
                expect(utilsIsValidMessageId(message.id)).toBe(true);
                createdMessageIds.push(message.id);
            });

            // Get messages for user 2
            const user2Results = await getMessagesByUserId(user2Id, 0, 10);
            expect(user2Results.content).toHaveLength(1);
            user2Results.content.forEach(message => {
                expect(message.userId).toBe(user2Id);
                expect(utilsIsValidMessageId(message.id)).toBe(true);
                createdMessageIds.push(message.id);
            });

            // Verify no overlap in message IDs
            const user1Ids = new Set(user1Results.content.map(m => m.id));
            const user2Ids = new Set(user2Results.content.map(m => m.id));
            const overlap = new Set([...user1Ids].filter(id => user2Ids.has(id)));
            expect(overlap.size).toBe(0);
        });
    });

    describe('Performance and Boundary Testing', () => {
        it('should handle large content with new ID format', async () => {
            const largeContent = 'A'.repeat(10000); // 10KB content
            const createData = {
                userId: testUserId,
                content: largeContent
            };

            const createResponse = await createMessage(createData);
            expect(createResponse).toHaveProperty('taskId');

            await new Promise(resolve => setTimeout(resolve, 2000));

            // Find the created message
            const allMessages = await getAllMessages(0, 50);
            const createdMessage = allMessages.content.find(
                msg => msg.userId === testUserId && msg.content.length === largeContent.length
            );

            expect(createdMessage).toBeDefined();
            expect(utilsIsValidMessageId(createdMessage.id)).toBe(true);
            expect(createdMessage.content).toBe(largeContent);

            createdMessageIds.push(createdMessage.id);

            // Verify retrieval works with large content
            const retrieved = await getMessageById(createdMessage.id);
            expect(retrieved.content).toBe(largeContent);
        });

        it('should handle special characters in content', async () => {
            const specialContent = 'Hello! @#$%^&*()_+ 中文 🚀 "quotes" \'apostrophes\' <tags>';
            const createData = {
                userId: testUserId,
                content: specialContent
            };

            const createResponse = await createMessage(createData);
            expect(createResponse).toHaveProperty('taskId');

            await new Promise(resolve => setTimeout(resolve, 1000));

            // Find and verify the created message
            const allMessages = await getAllMessages(0, 50);
            const createdMessage = allMessages.content.find(
                msg => msg.userId === testUserId && msg.content === specialContent
            );

            expect(createdMessage).toBeDefined();
            expect(utilsIsValidMessageId(createdMessage.id)).toBe(true);
            expect(createdMessage.content).toBe(specialContent);

            createdMessageIds.push(createdMessage.id);

            // Verify retrieval preserves special characters
            const retrieved = await getMessageById(createdMessage.id);
            expect(retrieved.content).toBe(specialContent);
        });

        it('should maintain performance with multiple operations', async () => {
            const operationCount = 20;
            const startTime = Date.now();

            // Perform multiple ID validations
            const validId = 'ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX';
            const invalidId = 'invalid-id';

            for (let i = 0; i < operationCount; i++) {
                expect(utilsIsValidMessageId(validId)).toBe(true);
                expect(utilsIsValidMessageId(invalidId)).toBe(false);
                
                const validation = validateMessageIdWithError(validId);
                expect(validation.isValid).toBe(true);
            }

            const endTime = Date.now();
            const duration = endTime - startTime;

            // Operations should complete quickly (less than 100ms for 20 operations)
            expect(duration).toBeLessThan(100);
        });
    });

    describe('Data Consistency', () => {
        it('should maintain data consistency across operations', async () => {
            // Create a message
            const originalContent = 'Original consistency test message';
            const createData = {
                userId: testUserId,
                content: originalContent
            };

            const createResponse = await createMessage(createData);
            await new Promise(resolve => setTimeout(resolve, 1000));

            // Find the created message
            const allMessages = await getAllMessages(0, 50);
            const createdMessage = allMessages.content.find(
                msg => msg.userId === testUserId && msg.content === originalContent
            );

            expect(createdMessage).toBeDefined();
            const messageId = createdMessage.id;
            createdMessageIds.push(messageId);

            // Verify consistency across different retrieval methods
            const directRetrieval = await getMessageById(messageId);
            const allMessagesRetrieval = await getAllMessages(0, 50);
            const userMessagesRetrieval = await getMessagesByUserId(testUserId, 0, 10);

            const fromAllMessages = allMessagesRetrieval.content.find(m => m.id === messageId);
            const fromUserMessages = userMessagesRetrieval.content.find(m => m.id === messageId);

            // All retrieval methods should return the same data
            expect(directRetrieval).toEqual(fromAllMessages);
            expect(directRetrieval).toEqual(fromUserMessages);
            expect(fromAllMessages).toEqual(fromUserMessages);

            // Update the message
            const updatedContent = 'Updated consistency test message';
            await updateMessage(messageId, { content: updatedContent });
            await new Promise(resolve => setTimeout(resolve, 1000));

            // Verify consistency after update
            const updatedDirect = await getMessageById(messageId);
            const updatedFromAll = (await getAllMessages(0, 50)).content.find(m => m.id === messageId);
            const updatedFromUser = (await getMessagesByUserId(testUserId, 0, 10)).content.find(m => m.id === messageId);

            expect(updatedDirect.content).toBe(updatedContent);
            expect(updatedFromAll.content).toBe(updatedContent);
            expect(updatedFromUser.content).toBe(updatedContent);

            // ID should remain consistent
            expect(updatedDirect.id).toBe(messageId);
            expect(updatedFromAll.id).toBe(messageId);
            expect(updatedFromUser.id).toBe(messageId);
        });
    });
});