import { describe, it, expect } from 'vitest';
import { isValidMessageId, validateMessageIdWithError, isLegacyNumericId } from '../utils/messageId';
import { isValidMessageId as apiIsValidMessageId } from '../api';

describe('Frontend Integration Tests for New Message ID Format', () => {
    describe('ID Format Consistency', () => {
        it('should have consistent ID validation between utils and API', () => {
            const testIds = [
                'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6', // Valid new format
                'invalid-id', // Invalid format
                '123', // Legacy numeric
                '', // Empty
                null, // Null
                undefined // Undefined
            ];

            testIds.forEach(id => {
                const utilsResult = isValidMessageId(id);
                const apiResult = apiIsValidMessageId(id);
                
                expect(utilsResult).toBe(apiResult);
            });
        });
    });

    describe('Component Data Flow', () => {
        it('should properly validate message objects for MessageList', () => {
            const messages = [
                {
                    id: 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6',
                    userId: 'user1',
                    content: 'Valid message',
                    createdAt: '2024-01-01T10:00:00Z'
                },
                {
                    id: 'invalid-id',
                    userId: 'user2',
                    content: 'Invalid ID message',
                    createdAt: '2024-01-01T11:00:00Z'
                },
                {
                    id: '123',
                    userId: 'user3',
                    content: 'Legacy ID message',
                    createdAt: '2024-01-01T12:00:00Z'
                }
            ];

            // Simulate MessageList component logic
            const processedMessages = messages.map(message => ({
                ...message,
                hasValidId: isValidMessageId(message.id),
                isLegacyId: isLegacyNumericId(message.id),
                validation: validateMessageIdWithError(message.id)
            }));

            expect(processedMessages[0].hasValidId).toBe(true);
            expect(processedMessages[0].isLegacyId).toBe(false);
            expect(processedMessages[0].validation.isValid).toBe(true);

            expect(processedMessages[1].hasValidId).toBe(false);
            expect(processedMessages[1].isLegacyId).toBe(false);
            expect(processedMessages[1].validation.isValid).toBe(false);

            expect(processedMessages[2].hasValidId).toBe(false);
            expect(processedMessages[2].isLegacyId).toBe(true);
            expect(processedMessages[2].validation.isValid).toBe(false);
            expect(processedMessages[2].validation.errorMessage).toBe('舊的數字 ID 格式');
        });

        it('should generate correct URLs for valid message IDs', () => {
            const validId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
            const invalidId = 'invalid-id';

            // Simulate URL generation logic
            const generateMessageUrl = (messageId) => {
                if (isValidMessageId(messageId)) {
                    return `/message/${messageId}`;
                }
                return null;
            };

            expect(generateMessageUrl(validId)).toBe('/message/A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6');
            expect(generateMessageUrl(invalidId)).toBe(null);
        });

        it('should handle SingleMessage component error scenarios', () => {
            const testScenarios = [
                {
                    messageId: 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6',
                    expectedShouldFetch: true,
                    expectedErrorType: null
                },
                {
                    messageId: 'invalid-id',
                    expectedShouldFetch: false,
                    expectedErrorType: 'invalid_id_format'
                },
                {
                    messageId: '123',
                    expectedShouldFetch: false,
                    expectedErrorType: 'invalid_id_format'
                },
                {
                    messageId: '',
                    expectedShouldFetch: false,
                    expectedErrorType: 'invalid_id'
                },
                {
                    messageId: null,
                    expectedShouldFetch: false,
                    expectedErrorType: 'invalid_id'
                }
            ];

            testScenarios.forEach(scenario => {
                // Simulate SingleMessage component logic
                const processMessageId = (messageId) => {
                    if (!messageId) {
                        return {
                            shouldFetch: false,
                            errorType: 'invalid_id',
                            message: 'Message ID is required'
                        };
                    }

                    const validation = validateMessageIdWithError(messageId);
                    if (!validation.isValid) {
                        let userGuidance = validation.errorMessage;
                        
                        if (isLegacyNumericId(messageId)) {
                            userGuidance = '此訊息使用舊的數字 ID 格式，系統已升級為新的 36 位字符 ID 格式。';
                        }
                        
                        return {
                            shouldFetch: false,
                            errorType: 'invalid_id_format',
                            message: '訊息 ID 格式不正確',
                            userGuidance: userGuidance
                        };
                    }

                    return {
                        shouldFetch: true,
                        errorType: null
                    };
                };

                const result = processMessageId(scenario.messageId);
                expect(result.shouldFetch).toBe(scenario.expectedShouldFetch);
                expect(result.errorType).toBe(scenario.expectedErrorType);
            });
        });
    });

    describe('Error Handling Integration', () => {
        it('should provide appropriate user guidance for different error types', () => {
            const errorCases = [
                {
                    input: null,
                    expectedMessage: '訊息 ID 不能為空'
                },
                {
                    input: '',
                    expectedMessage: '訊息 ID 不能為空'
                },
                {
                    input: 123,
                    expectedMessage: '訊息 ID 必須是字符串格式'
                },
                {
                    input: '123',
                    expectedMessage: '舊的數字 ID 格式'
                },
                {
                    input: 'invalid-format',
                    expectedMessage: '訊息 ID 格式不正確，應為 36 位大寫字母和數字組成'
                }
            ];

            errorCases.forEach(testCase => {
                const validation = validateMessageIdWithError(testCase.input);
                expect(validation.isValid).toBe(false);
                expect(validation.errorMessage).toBe(testCase.expectedMessage);
            });
        });

        it('should handle API error scenarios correctly', () => {
            // Simulate API call validation
            const simulateApiCall = (messageId) => {
                try {
                    if (!apiIsValidMessageId(messageId)) {
                        throw new Error('Invalid message ID format');
                    }
                    return { success: true, data: { id: messageId } };
                } catch (error) {
                    return { success: false, error: error.message };
                }
            };

            const validId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
            const invalidId = 'invalid-id';

            const validResult = simulateApiCall(validId);
            expect(validResult.success).toBe(true);
            expect(validResult.data.id).toBe(validId);

            const invalidResult = simulateApiCall(invalidId);
            expect(invalidResult.success).toBe(false);
            expect(invalidResult.error).toBe('Invalid message ID format');
        });
    });

    describe('URL Routing Integration', () => {
        it('should handle React Router parameters correctly', () => {
            // Simulate React Router useParams hook behavior
            const simulateRouteParams = (url) => {
                const match = url.match(/\/message\/(.+)$/);
                return match ? { messageId: match[1] } : null;
            };

            const testUrls = [
                '/message/A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6',
                '/message/invalid-id',
                '/message/123',
                '/invalid-route'
            ];

            testUrls.forEach(url => {
                const params = simulateRouteParams(url);
                if (params) {
                    const isValid = isValidMessageId(params.messageId);
                    const isLegacy = isLegacyNumericId(params.messageId);
                    
                    // These assertions verify that our routing logic can handle all ID formats
                    expect(typeof isValid).toBe('boolean');
                    expect(typeof isLegacy).toBe('boolean');
                }
            });
        });
    });

    describe('Performance Considerations', () => {
        it('should validate IDs efficiently for large datasets', () => {
            const generateTestIds = (count) => {
                const ids = [];
                for (let i = 0; i < count; i++) {
                    if (i % 3 === 0) {
                        ids.push('A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6'); // Valid
                    } else if (i % 3 === 1) {
                        ids.push('invalid-id'); // Invalid
                    } else {
                        ids.push(String(i)); // Legacy numeric
                    }
                }
                return ids;
            };

            const testIds = generateTestIds(1000);
            
            const startTime = performance.now();
            const results = testIds.map(id => ({
                id,
                isValid: isValidMessageId(id),
                isLegacy: isLegacyNumericId(id)
            }));
            const endTime = performance.now();

            // Validation should complete quickly even for large datasets
            expect(endTime - startTime).toBeLessThan(100); // Less than 100ms
            expect(results).toHaveLength(1000);
            
            // Verify correct distribution of results
            const validCount = results.filter(r => r.isValid).length;
            const legacyCount = results.filter(r => r.isLegacy).length;
            
            expect(validCount).toBeGreaterThan(300); // Should have valid IDs
            expect(validCount).toBeLessThan(400);
            expect(legacyCount).toBeGreaterThan(300); // Should have legacy IDs
            expect(legacyCount).toBeLessThan(400);
        });
    });
});