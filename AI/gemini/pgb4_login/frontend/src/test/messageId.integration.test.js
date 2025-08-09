import { describe, it, expect, beforeEach, vi } from 'vitest';
import { isValidMessageId as utilsIsValidMessageId, validateMessageIdWithError, isLegacyNumericId } from '../utils/messageId';
import { isValidMessageId as apiIsValidMessageId } from '../api';

/**
 * Integration tests for message ID validation and handling
 * These tests verify the integration between different parts of the frontend
 * without requiring a live backend connection
 */
describe('Message ID Integration Tests', () => {
    describe('ID Validation Consistency', () => {
        it('should have consistent validation between utils and API modules', () => {
            const testCases = [
                {
                    id: 'ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX',
                    expectedValid: true,
                    description: 'Valid new format ID'
                },
                {
                    id: 'abcd1234-efgh-5678-ijkl-mnopqrstuvwx',
                    expectedValid: false,
                    description: 'Lowercase letters (invalid)'
                },
                {
                    id: 'ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVW',
                    expectedValid: false,
                    description: '35 characters (too short)'
                },
                {
                    id: 'ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWXY',
                    expectedValid: false,
                    description: '37 characters (too long)'
                },
                {
                    id: 'ABCD1234EFGH5678IJKLMNOPQRSTUVWX',
                    expectedValid: false,
                    description: 'Missing dashes'
                },
                {
                    id: 'ABCD@#$%-EFGH-5678-IJKL-MNOPQRSTUVWX',
                    expectedValid: false,
                    description: 'Special characters'
                },
                {
                    id: '123',
                    expectedValid: false,
                    description: 'Legacy numeric ID'
                },
                {
                    id: 'invalid-id',
                    expectedValid: false,
                    description: 'Invalid format'
                },
                {
                    id: '',
                    expectedValid: false,
                    description: 'Empty string'
                },
                {
                    id: null,
                    expectedValid: false,
                    description: 'Null value'
                },
                {
                    id: undefined,
                    expectedValid: false,
                    description: 'Undefined value'
                }
            ];

            testCases.forEach(testCase => {
                const utilsResult = utilsIsValidMessageId(testCase.id);
                const apiResult = apiIsValidMessageId(testCase.id);
                
                expect(utilsResult).toBe(testCase.expectedValid);
                expect(apiResult).toBe(testCase.expectedValid);
                expect(utilsResult).toBe(apiResult);
            });
        });

        it('should provide detailed error messages for invalid IDs', () => {
            const errorTestCases = [
                {
                    id: null,
                    expectedError: '訊息 ID 不能為空'
                },
                {
                    id: '',
                    expectedError: '訊息 ID 不能為空'
                },
                {
                    id: 123,
                    expectedError: '訊息 ID 必須是字符串格式'
                },
                {
                    id: '123',
                    expectedError: '舊的數字 ID 格式'
                },
                {
                    id: 'invalid-format',
                    expectedError: '訊息 ID 格式不正確，應為 36 位大寫字母和數字組成'
                },
                {
                    id: 'abcd1234-efgh-5678-ijkl-mnopqrstuvwx',
                    expectedError: '訊息 ID 格式不正確，應為 36 位大寫字母和數字組成'
                }
            ];

            errorTestCases.forEach(testCase => {
                const validation = validateMessageIdWithError(testCase.id);
                expect(validation.isValid).toBe(false);
                expect(validation.errorMessage).toBe(testCase.expectedError);
            });
        });

        it('should correctly identify legacy numeric IDs', () => {
            const legacyTestCases = [
                { id: '123', expectedLegacy: true },
                { id: '0', expectedLegacy: true },
                { id: '999999', expectedLegacy: true },
                { id: 'ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX', expectedLegacy: false },
                { id: 'invalid-id', expectedLegacy: false },
                { id: '', expectedLegacy: false },
                { id: null, expectedLegacy: false }
            ];

            legacyTestCases.forEach(testCase => {
                const result = isLegacyNumericId(testCase.id);
                expect(result).toBe(testCase.expectedLegacy);
            });
        });
    });

    describe('Component Integration Scenarios', () => {
        it('should handle MessageList component data processing', () => {
            const mockMessages = [
                {
                    id: 'ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX',
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

            // Simulate MessageList component processing
            const processedMessages = mockMessages.map(message => {
                const validation = validateMessageIdWithError(message.id);
                return {
                    ...message,
                    hasValidId: validation.isValid,
                    isLegacyId: isLegacyNumericId(message.id),
                    validationError: validation.isValid ? null : validation.errorMessage,
                    shouldShowWarning: !validation.isValid,
                    canNavigate: validation.isValid
                };
            });

            // Verify processing results
            expect(processedMessages[0].hasValidId).toBe(true);
            expect(processedMessages[0].isLegacyId).toBe(false);
            expect(processedMessages[0].validationError).toBe(null);
            expect(processedMessages[0].shouldShowWarning).toBe(false);
            expect(processedMessages[0].canNavigate).toBe(true);

            expect(processedMessages[1].hasValidId).toBe(false);
            expect(processedMessages[1].isLegacyId).toBe(false);
            expect(processedMessages[1].validationError).toBeTruthy();
            expect(processedMessages[1].shouldShowWarning).toBe(true);
            expect(processedMessages[1].canNavigate).toBe(false);

            expect(processedMessages[2].hasValidId).toBe(false);
            expect(processedMessages[2].isLegacyId).toBe(true);
            expect(processedMessages[2].validationError).toBe('舊的數字 ID 格式');
            expect(processedMessages[2].shouldShowWarning).toBe(true);
            expect(processedMessages[2].canNavigate).toBe(false);
        });

        it('should handle SingleMessage component routing scenarios', () => {
            const routingTestCases = [
                {
                    messageId: 'ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX',
                    expectedShouldLoad: true,
                    expectedErrorType: null,
                    description: 'Valid ID should load'
                },
                {
                    messageId: 'invalid-id',
                    expectedShouldLoad: false,
                    expectedErrorType: 'invalid_format',
                    description: 'Invalid format should show error'
                },
                {
                    messageId: '123',
                    expectedShouldLoad: false,
                    expectedErrorType: 'legacy_format',
                    description: 'Legacy ID should show migration message'
                },
                {
                    messageId: '',
                    expectedShouldLoad: false,
                    expectedErrorType: 'missing_id',
                    description: 'Empty ID should show error'
                },
                {
                    messageId: null,
                    expectedShouldLoad: false,
                    expectedErrorType: 'missing_id',
                    description: 'Null ID should show error'
                }
            ];

            routingTestCases.forEach(testCase => {
                // Simulate SingleMessage component logic
                const processRouteParam = (messageId) => {
                    if (!messageId) {
                        return {
                            shouldLoad: false,
                            errorType: 'missing_id',
                            errorMessage: '訊息 ID 缺失',
                            userGuidance: '請檢查 URL 是否正確'
                        };
                    }

                    const validation = validateMessageIdWithError(messageId);
                    if (!validation.isValid) {
                        if (isLegacyNumericId(messageId)) {
                            return {
                                shouldLoad: false,
                                errorType: 'legacy_format',
                                errorMessage: '舊的 ID 格式',
                                userGuidance: '此訊息使用舊的數字 ID 格式，系統已升級為新的 36 位字符 ID 格式。'
                            };
                        }
                        
                        return {
                            shouldLoad: false,
                            errorType: 'invalid_format',
                            errorMessage: validation.errorMessage,
                            userGuidance: '請檢查 URL 中的訊息 ID 是否正確'
                        };
                    }

                    return {
                        shouldLoad: true,
                        errorType: null,
                        errorMessage: null,
                        userGuidance: null
                    };
                };

                const result = processRouteParam(testCase.messageId);
                expect(result.shouldLoad).toBe(testCase.expectedShouldLoad);
                expect(result.errorType).toBe(testCase.expectedErrorType);
            });
        });

        it('should handle URL generation for navigation', () => {
            const navigationTestCases = [
                {
                    messageId: 'ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX',
                    expectedUrl: '/message/ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX',
                    expectedCanNavigate: true
                },
                {
                    messageId: 'invalid-id',
                    expectedUrl: null,
                    expectedCanNavigate: false
                },
                {
                    messageId: '123',
                    expectedUrl: null,
                    expectedCanNavigate: false
                },
                {
                    messageId: '',
                    expectedUrl: null,
                    expectedCanNavigate: false
                },
                {
                    messageId: null,
                    expectedUrl: null,
                    expectedCanNavigate: false
                }
            ];

            navigationTestCases.forEach(testCase => {
                // Simulate navigation URL generation
                const generateMessageUrl = (messageId) => {
                    if (!utilsIsValidMessageId(messageId)) {
                        return null;
                    }
                    return `/message/${messageId}`;
                };

                const canNavigateToMessage = (messageId) => {
                    return utilsIsValidMessageId(messageId);
                };

                const url = generateMessageUrl(testCase.messageId);
                const canNavigate = canNavigateToMessage(testCase.messageId);

                expect(url).toBe(testCase.expectedUrl);
                expect(canNavigate).toBe(testCase.expectedCanNavigate);
            });
        });
    });

    describe('Error Handling Integration', () => {
        it('should provide consistent error handling across components', () => {
            const errorScenarios = [
                {
                    input: null,
                    context: 'MessageList',
                    expectedHandling: 'skip_item'
                },
                {
                    input: '',
                    context: 'SingleMessage',
                    expectedHandling: 'show_error'
                },
                {
                    input: '123',
                    context: 'Navigation',
                    expectedHandling: 'show_migration_notice'
                },
                {
                    input: 'invalid-format',
                    context: 'API_call',
                    expectedHandling: 'validation_error'
                }
            ];

            errorScenarios.forEach(scenario => {
                const validation = validateMessageIdWithError(scenario.input);
                const isLegacy = isLegacyNumericId(scenario.input);

                // Simulate different component error handling strategies
                let actualHandling;
                
                if (scenario.context === 'MessageList' && !scenario.input) {
                    actualHandling = 'skip_item';
                } else if (scenario.context === 'SingleMessage' && !validation.isValid) {
                    actualHandling = 'show_error';
                } else if (scenario.context === 'Navigation' && isLegacy) {
                    actualHandling = 'show_migration_notice';
                } else if (scenario.context === 'API_call' && !validation.isValid) {
                    actualHandling = 'validation_error';
                } else {
                    actualHandling = 'proceed_normally';
                }

                expect(actualHandling).toBe(scenario.expectedHandling);
            });
        });

        it('should handle edge cases gracefully', () => {
            const edgeCases = [
                { input: undefined, expectedGraceful: true },
                { input: {}, expectedGraceful: true },
                { input: [], expectedGraceful: true },
                { input: 123, expectedGraceful: true },
                { input: true, expectedGraceful: true },
                { input: false, expectedGraceful: true }
            ];

            edgeCases.forEach(testCase => {
                // Should not throw errors for any input type
                expect(() => {
                    utilsIsValidMessageId(testCase.input);
                    validateMessageIdWithError(testCase.input);
                    isLegacyNumericId(testCase.input);
                }).not.toThrow();

                // All should return false for non-string inputs
                expect(utilsIsValidMessageId(testCase.input)).toBe(false);
                
                const validation = validateMessageIdWithError(testCase.input);
                expect(validation.isValid).toBe(false);
                expect(typeof validation.errorMessage).toBe('string');
            });
        });
    });

    describe('Performance Integration', () => {
        it('should handle large datasets efficiently', () => {
            const generateTestData = (count) => {
                const data = [];
                for (let i = 0; i < count; i++) {
                    if (i % 4 === 0) {
                        data.push('ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX'); // Valid
                    } else if (i % 4 === 1) {
                        data.push('invalid-id'); // Invalid
                    } else if (i % 4 === 2) {
                        data.push(String(i)); // Legacy numeric
                    } else {
                        data.push(''); // Empty
                    }
                }
                return data;
            };

            const testData = generateTestData(1000);
            const startTime = performance.now();

            // Process all data
            const results = testData.map(id => ({
                id,
                isValid: utilsIsValidMessageId(id),
                isLegacy: isLegacyNumericId(id),
                validation: validateMessageIdWithError(id)
            }));

            const endTime = performance.now();
            const duration = endTime - startTime;

            // Should process 1000 items quickly
            expect(duration).toBeLessThan(100); // Less than 100ms
            expect(results).toHaveLength(1000);

            // Verify correct distribution
            const validCount = results.filter(r => r.isValid).length;
            const legacyCount = results.filter(r => r.isLegacy).length;
            
            expect(validCount).toBeGreaterThan(200); // ~25% should be valid
            expect(validCount).toBeLessThan(300);
            expect(legacyCount).toBeGreaterThan(200); // ~25% should be legacy
            expect(legacyCount).toBeLessThan(300);
        });

        it('should maintain consistent performance across multiple calls', () => {
            const validId = 'ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX';
            const invalidId = 'invalid-id';
            const iterations = 1000;

            const startTime = performance.now();

            for (let i = 0; i < iterations; i++) {
                utilsIsValidMessageId(validId);
                utilsIsValidMessageId(invalidId);
                validateMessageIdWithError(validId);
                validateMessageIdWithError(invalidId);
            }

            const endTime = performance.now();
            const duration = endTime - startTime;

            // Should complete many validations quickly
            expect(duration).toBeLessThan(50); // Less than 50ms for 4000 operations
        });
    });

    describe('Data Flow Integration', () => {
        it('should maintain data integrity through component chain', () => {
            // Simulate data flow: API -> MessageList -> SingleMessage
            const mockApiResponse = {
                content: [
                    {
                        id: 'ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX',
                        userId: 'user1',
                        content: 'Test message',
                        createdAt: '2024-01-01T10:00:00Z',
                        updatedAt: '2024-01-01T10:00:00Z'
                    }
                ]
            };

            // MessageList processing
            const processedForList = mockApiResponse.content.map(message => ({
                ...message,
                isValidId: utilsIsValidMessageId(message.id),
                canNavigate: utilsIsValidMessageId(message.id)
            }));

            expect(processedForList[0].isValidId).toBe(true);
            expect(processedForList[0].canNavigate).toBe(true);

            // SingleMessage processing (when navigating)
            const messageId = processedForList[0].id;
            const singleMessageValidation = validateMessageIdWithError(messageId);
            
            expect(singleMessageValidation.isValid).toBe(true);
            expect(singleMessageValidation.errorMessage).toBe(null);

            // URL generation
            const messageUrl = `/message/${messageId}`;
            expect(messageUrl).toBe('/message/ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX');

            // URL parsing (reverse direction)
            const parsedId = messageUrl.split('/message/')[1];
            expect(parsedId).toBe(messageId);
            expect(utilsIsValidMessageId(parsedId)).toBe(true);
        });

        it('should handle mixed ID formats in data sets', () => {
            const mixedData = [
                { id: 'ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX', content: 'New format' },
                { id: '123', content: 'Legacy format' },
                { id: 'invalid-id', content: 'Invalid format' },
                { id: '', content: 'Empty ID' },
                { id: null, content: 'Null ID' }
            ];

            const processedData = mixedData.map(item => {
                const validation = validateMessageIdWithError(item.id);
                return {
                    ...item,
                    validation,
                    displayStrategy: validation.isValid ? 'normal' : 
                                   isLegacyNumericId(item.id) ? 'legacy_warning' : 'error',
                    allowNavigation: validation.isValid
                };
            });

            // Verify processing results
            expect(processedData[0].displayStrategy).toBe('normal');
            expect(processedData[0].allowNavigation).toBe(true);

            expect(processedData[1].displayStrategy).toBe('legacy_warning');
            expect(processedData[1].allowNavigation).toBe(false);

            expect(processedData[2].displayStrategy).toBe('error');
            expect(processedData[2].allowNavigation).toBe(false);

            expect(processedData[3].displayStrategy).toBe('error');
            expect(processedData[3].allowNavigation).toBe(false);

            expect(processedData[4].displayStrategy).toBe('error');
            expect(processedData[4].allowNavigation).toBe(false);
        });
    });
});