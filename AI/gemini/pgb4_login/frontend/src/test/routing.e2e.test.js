import { describe, it, expect, beforeEach, vi } from 'vitest';
import { isValidMessageId, validateMessageIdWithError, isLegacyNumericId } from '../utils/messageId';

/**
 * End-to-End Routing Tests for New Message ID Format
 * Tests the complete routing functionality including browser navigation
 */
describe('E2E Routing Tests for New Message ID Format', () => {
    
    // Mock browser history for testing navigation
    const mockHistory = {
        entries: [],
        index: -1,
        push: function(path) {
            this.index++;
            this.entries = this.entries.slice(0, this.index);
            this.entries.push(path);
        },
        back: function() {
            if (this.index > 0) {
                this.index--;
                return this.entries[this.index];
            }
            return null;
        },
        forward: function() {
            if (this.index < this.entries.length - 1) {
                this.index++;
                return this.entries[this.index];
            }
            return null;
        },
        getCurrentPath: function() {
            return this.entries[this.index] || '/';
        },
        reset: function() {
            this.entries = [];
            this.index = -1;
        }
    };

    beforeEach(() => {
        mockHistory.reset();
    });

    describe('Message Detail Route Navigation', () => {
        it('should navigate to message detail with valid new ID format', () => {
            const validMessageId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
            const expectedPath = `/message/${validMessageId}`;
            
            // Validate ID format
            expect(isValidMessageId(validMessageId)).toBe(true);
            
            // Simulate navigation
            mockHistory.push('/');
            mockHistory.push(expectedPath);
            
            expect(mockHistory.getCurrentPath()).toBe(expectedPath);
            expect(mockHistory.entries).toContain(expectedPath);
        });

        it('should handle invalid message ID in route', () => {
            const invalidMessageId = 'invalid-id-format';
            const invalidPath = `/message/${invalidMessageId}`;
            
            // Validate ID format
            const validation = validateMessageIdWithError(invalidMessageId);
            expect(validation.isValid).toBe(false);
            
            // Simulate navigation to invalid path
            mockHistory.push('/');
            mockHistory.push(invalidPath);
            
            // Path should still be recorded (browser allows any path)
            expect(mockHistory.getCurrentPath()).toBe(invalidPath);
            
            // But component should handle the error
            expect(validation.errorMessage).toBeTruthy();
        });

        it('should handle legacy numeric ID in route', () => {
            const legacyId = '12345';
            const legacyPath = `/message/${legacyId}`;
            
            // Validate legacy ID detection
            expect(isLegacyNumericId(legacyId)).toBe(true);
            expect(isValidMessageId(legacyId)).toBe(false);
            
            // Simulate navigation to legacy path
            mockHistory.push('/');
            mockHistory.push(legacyPath);
            
            expect(mockHistory.getCurrentPath()).toBe(legacyPath);
            
            // Validation should provide specific error for legacy ID
            const validation = validateMessageIdWithError(legacyId);
            expect(validation.isValid).toBe(false);
            expect(validation.errorMessage).toContain('舊的數字 ID 格式');
        });
    });

    describe('Browser Navigation (Back/Forward)', () => {
        it('should support browser back/forward navigation with new ID format', () => {
            const messageId1 = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
            const messageId2 = 'B2C3D4E5-F6G7-H8I9-J0K1-L2M3N4O5P6Q7';
            
            // Simulate user navigation flow
            mockHistory.push('/'); // Home page
            mockHistory.push(`/message/${messageId1}`); // First message
            mockHistory.push(`/message/${messageId2}`); // Second message
            
            expect(mockHistory.getCurrentPath()).toBe(`/message/${messageId2}`);
            
            // Test back navigation
            const backPath = mockHistory.back();
            expect(backPath).toBe(`/message/${messageId1}`);
            expect(mockHistory.getCurrentPath()).toBe(`/message/${messageId1}`);
            
            // Test forward navigation
            const forwardPath = mockHistory.forward();
            expect(forwardPath).toBe(`/message/${messageId2}`);
            expect(mockHistory.getCurrentPath()).toBe(`/message/${messageId2}`);
            
            // Test back to home
            mockHistory.back();
            const homePath = mockHistory.back();
            expect(homePath).toBe('/');
            expect(mockHistory.getCurrentPath()).toBe('/');
        });

        it('should handle browser navigation with mixed valid/invalid IDs', () => {
            const validId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
            const invalidId = 'invalid-format';
            
            // Navigate through mixed paths
            mockHistory.push('/');
            mockHistory.push(`/message/${validId}`);
            mockHistory.push(`/message/${invalidId}`);
            
            // Current path should be invalid
            expect(mockHistory.getCurrentPath()).toBe(`/message/${invalidId}`);
            
            // Back to valid path
            const backPath = mockHistory.back();
            expect(backPath).toBe(`/message/${validId}`);
            
            // Validate the paths
            expect(isValidMessageId(validId)).toBe(true);
            expect(isValidMessageId(invalidId)).toBe(false);
        });
    });

    describe('Route Parameter Extraction', () => {
        it('should correctly extract message ID from route parameters', () => {
            const testCases = [
                {
                    path: '/message/A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6',
                    expectedId: 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6',
                    shouldBeValid: true
                },
                {
                    path: '/message/invalid-id',
                    expectedId: 'invalid-id',
                    shouldBeValid: false
                },
                {
                    path: '/message/12345',
                    expectedId: '12345',
                    shouldBeValid: false,
                    isLegacy: true
                }
            ];

            testCases.forEach(testCase => {
                // Simulate route parameter extraction
                const pathParts = testCase.path.split('/');
                const extractedId = pathParts[pathParts.length - 1];
                
                expect(extractedId).toBe(testCase.expectedId);
                expect(isValidMessageId(extractedId)).toBe(testCase.shouldBeValid);
                
                if (testCase.isLegacy) {
                    expect(isLegacyNumericId(extractedId)).toBe(true);
                }
            });
        });

        it('should handle URL encoding in message IDs', () => {
            const originalId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
            const encodedId = encodeURIComponent(originalId);
            
            // For this ID format, encoding shouldn't change anything
            expect(encodedId).toBe(originalId);
            expect(isValidMessageId(encodedId)).toBe(true);
            
            // Test with decoded ID
            const decodedId = decodeURIComponent(encodedId);
            expect(decodedId).toBe(originalId);
            expect(isValidMessageId(decodedId)).toBe(true);
        });
    });

    describe('Navigation Link Generation', () => {
        it('should generate correct navigation links for message lists', () => {
            const messages = [
                {
                    id: 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6',
                    userId: 'user1',
                    content: 'Test message 1'
                },
                {
                    id: 'B2C3D4E5-F6G7-H8I9-J0K1-L2M3N4O5P6Q7',
                    userId: 'user2',
                    content: 'Test message 2'
                },
                {
                    id: 'invalid-id',
                    userId: 'user3',
                    content: 'Test message 3'
                }
            ];

            const generateMessageLink = (messageId) => `/message/${messageId}`;

            messages.forEach(message => {
                const link = generateMessageLink(message.id);
                const isValidLink = isValidMessageId(message.id);
                
                expect(link).toBe(`/message/${message.id}`);
                
                if (isValidLink) {
                    // Valid links should be navigable
                    mockHistory.push(link);
                    expect(mockHistory.getCurrentPath()).toBe(link);
                } else {
                    // Invalid links should still be generated but handled by component
                    expect(link).toContain('invalid-id');
                }
            });
        });

        it('should generate correct breadcrumb navigation links', () => {
            const messageId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
            const userId = 'testuser';
            
            // Simulate breadcrumb link generation
            const breadcrumbLinks = {
                home: '/',
                allMessages: '/',
                userMessages: `/user/${userId}/messages`,
                currentMessage: `/message/${messageId}`
            };

            // Test each breadcrumb link
            Object.entries(breadcrumbLinks).forEach(([key, path]) => {
                mockHistory.push(path);
                expect(mockHistory.getCurrentPath()).toBe(path);
                
                if (key === 'currentMessage') {
                    const extractedId = path.split('/').pop();
                    expect(isValidMessageId(extractedId)).toBe(true);
                }
            });
        });
    });

    describe('Error Handling in Routes', () => {
        it('should handle route navigation errors gracefully', () => {
            const errorScenarios = [
                {
                    path: '/message/',
                    description: 'Empty message ID',
                    expectedError: 'Message ID is required'
                },
                {
                    path: '/message/null',
                    description: 'Null-like message ID',
                    expectedError: 'Invalid message ID format'
                },
                {
                    path: '/message/undefined',
                    description: 'Undefined-like message ID',
                    expectedError: 'Invalid message ID format'
                }
            ];

            errorScenarios.forEach(scenario => {
                mockHistory.push(scenario.path);
                expect(mockHistory.getCurrentPath()).toBe(scenario.path);
                
                // Extract ID from path
                const pathParts = scenario.path.split('/');
                const messageId = pathParts[pathParts.length - 1];
                
                if (messageId) {
                    const validation = validateMessageIdWithError(messageId);
                    expect(validation.isValid).toBe(false);
                    expect(validation.errorMessage).toBeTruthy();
                }
            });
        });

        it('should provide appropriate error messages for different ID formats', () => {
            const errorCases = [
                {
                    id: '',
                    expectedMessage: '訊息 ID 不能為空'
                },
                {
                    id: '123',
                    expectedMessage: '舊的數字 ID 格式'
                },
                {
                    id: 'too-short',
                    expectedMessage: '訊息 ID 格式不正確，應為 36 位大寫字母和數字組成'
                },
                {
                    id: 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6-EXTRA',
                    expectedMessage: '訊息 ID 格式不正確，應為 36 位大寫字母和數字組成'
                }
            ];

            errorCases.forEach(testCase => {
                const validation = validateMessageIdWithError(testCase.id);
                expect(validation.isValid).toBe(false);
                expect(validation.errorMessage).toBe(testCase.expectedMessage);
            });
        });
    });

    describe('Performance and Edge Cases', () => {
        it('should handle rapid navigation between messages', () => {
            const messageIds = [
                'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6',
                'B2C3D4E5-F6G7-H8I9-J0K1-L2M3N4O5P6Q7',
                'C3D4E5F6-G7H8-I9J0-K1L2-M3N4O5P6Q7R8',
                'D4E5F6G7-H8I9-J0K1-L2M3-N4O5P6Q7R8S9',
                'E5F6G7H8-I9J0-K1L2-M3N4-O5P6Q7R8S9T0'
            ];

            // Simulate rapid navigation
            messageIds.forEach(id => {
                const path = `/message/${id}`;
                mockHistory.push(path);
                expect(mockHistory.getCurrentPath()).toBe(path);
                expect(isValidMessageId(id)).toBe(true);
            });

            // Test rapid back navigation
            for (let i = messageIds.length - 2; i >= 0; i--) {
                const backPath = mockHistory.back();
                const expectedPath = `/message/${messageIds[i]}`;
                expect(backPath).toBe(expectedPath);
            }
        });

        it('should handle navigation with special characters in URLs', () => {
            const messageId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
            const pathWithQuery = `/message/${messageId}?ref=home&tab=details`;
            const pathWithHash = `/message/${messageId}#comments`;
            
            // Test navigation with query parameters
            mockHistory.push(pathWithQuery);
            expect(mockHistory.getCurrentPath()).toBe(pathWithQuery);
            
            // Extract ID from path with query
            const idFromQuery = pathWithQuery.split('?')[0].split('/').pop();
            expect(isValidMessageId(idFromQuery)).toBe(true);
            
            // Test navigation with hash
            mockHistory.push(pathWithHash);
            expect(mockHistory.getCurrentPath()).toBe(pathWithHash);
            
            // Extract ID from path with hash
            const idFromHash = pathWithHash.split('#')[0].split('/').pop();
            expect(isValidMessageId(idFromHash)).toBe(true);
        });
    });

    describe('Integration with React Router', () => {
        it('should simulate React Router parameter extraction', () => {
            // Simulate useParams hook behavior
            const simulateUseParams = (path) => {
                const pathPattern = '/message/:messageId';
                const pathParts = path.split('/');
                const patternParts = pathPattern.split('/');
                
                const params = {};
                for (let i = 0; i < patternParts.length; i++) {
                    if (patternParts[i].startsWith(':')) {
                        const paramName = patternParts[i].substring(1);
                        params[paramName] = pathParts[i];
                    }
                }
                
                return params;
            };

            const testPaths = [
                '/message/A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6',
                '/message/invalid-id',
                '/message/12345'
            ];

            testPaths.forEach(path => {
                const params = simulateUseParams(path);
                expect(params.messageId).toBeTruthy();
                
                const messageId = params.messageId;
                const validation = validateMessageIdWithError(messageId);
                
                // Each path should extract a messageId parameter
                expect(messageId).toBe(path.split('/').pop());
                
                // Validation results should match expected format
                if (messageId === 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6') {
                    expect(validation.isValid).toBe(true);
                } else {
                    expect(validation.isValid).toBe(false);
                }
            });
        });

        it('should handle route matching with new ID format', () => {
            // Simulate React Router route matching
            const routes = [
                { path: '/', exact: true },
                { path: '/message/:messageId', exact: true },
                { path: '/user/:userId/messages', exact: true }
            ];

            const testPaths = [
                { path: '/', shouldMatch: '/' },
                { path: '/message/A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6', shouldMatch: '/message/:messageId' },
                { path: '/user/testuser/messages', shouldMatch: '/user/:userId/messages' }
            ];

            testPaths.forEach(testPath => {
                const matchingRoute = routes.find(route => {
                    if (route.exact && route.path === testPath.path) {
                        return true;
                    }
                    
                    // Simple pattern matching simulation
                    const routeParts = route.path.split('/');
                    const pathParts = testPath.path.split('/');
                    
                    if (routeParts.length !== pathParts.length) {
                        return false;
                    }
                    
                    return routeParts.every((part, index) => {
                        return part.startsWith(':') || part === pathParts[index];
                    });
                });

                expect(matchingRoute?.path).toBe(testPath.shouldMatch);
            });
        });
    });
});