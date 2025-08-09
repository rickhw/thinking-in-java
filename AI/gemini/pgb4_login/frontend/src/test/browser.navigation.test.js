import { describe, it, expect, beforeEach, vi } from 'vitest';
import { isValidMessageId } from '../utils/messageId';
import { 
    generateMessageUrl, 
    generateUserMessagesUrl, 
    generateHomeUrl,
    parseMessageIdFromPath,
    validateNavigationPath
} from '../utils/navigation';

/**
 * Browser Navigation Tests for New Message ID Format
 * Tests browser back/forward functionality and URL handling
 */
describe('Browser Navigation Tests', () => {
    
    // Mock browser history API
    let mockHistory;
    let mockLocation;
    
    beforeEach(() => {
        // Reset mock history
        mockHistory = {
            entries: ['/'],
            currentIndex: 0,
            
            pushState: function(state, title, url) {
                this.currentIndex++;
                this.entries = this.entries.slice(0, this.currentIndex);
                this.entries.push(url);
                mockLocation.pathname = url;
            },
            
            replaceState: function(state, title, url) {
                this.entries[this.currentIndex] = url;
                mockLocation.pathname = url;
            },
            
            back: function() {
                if (this.currentIndex > 0) {
                    this.currentIndex--;
                    mockLocation.pathname = this.entries[this.currentIndex];
                    return this.entries[this.currentIndex];
                }
                return null;
            },
            
            forward: function() {
                if (this.currentIndex < this.entries.length - 1) {
                    this.currentIndex++;
                    mockLocation.pathname = this.entries[this.currentIndex];
                    return this.entries[this.currentIndex];
                }
                return null;
            },
            
            go: function(delta) {
                const newIndex = this.currentIndex + delta;
                if (newIndex >= 0 && newIndex < this.entries.length) {
                    this.currentIndex = newIndex;
                    mockLocation.pathname = this.entries[this.currentIndex];
                    return this.entries[this.currentIndex];
                }
                return null;
            },
            
            getCurrentPath: function() {
                return this.entries[this.currentIndex];
            },
            
            getLength: function() {
                return this.entries.length;
            }
        };
        
        mockLocation = {
            pathname: '/',
            search: '',
            hash: '',
            href: 'http://localhost:3000/'
        };
    });

    describe('Message Detail Navigation', () => {
        it('should handle navigation to message detail with valid ID', () => {
            const messageId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
            const messageUrl = generateMessageUrl(messageId);
            
            // Navigate to message
            mockHistory.pushState(null, '', messageUrl);
            
            expect(mockHistory.getCurrentPath()).toBe(messageUrl);
            expect(isValidMessageId(messageId)).toBe(true);
            
            // Validate the path
            const validation = validateNavigationPath(messageUrl);
            expect(validation.isValid).toBe(true);
        });

        it('should handle browser back from message detail', () => {
            const messageId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
            const messageUrl = generateMessageUrl(messageId);
            
            // Navigate: Home -> Message
            mockHistory.pushState(null, '', messageUrl);
            expect(mockHistory.getCurrentPath()).toBe(messageUrl);
            
            // Go back to home
            const backPath = mockHistory.back();
            expect(backPath).toBe('/');
            expect(mockHistory.getCurrentPath()).toBe('/');
        });

        it('should handle browser forward to message detail', () => {
            const messageId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
            const messageUrl = generateMessageUrl(messageId);
            
            // Navigate: Home -> Message -> Back -> Forward
            mockHistory.pushState(null, '', messageUrl);
            mockHistory.back();
            
            const forwardPath = mockHistory.forward();
            expect(forwardPath).toBe(messageUrl);
            expect(mockHistory.getCurrentPath()).toBe(messageUrl);
        });

        it('should handle navigation with invalid message ID', () => {
            const invalidId = 'invalid-id';
            const invalidUrl = `/message/${invalidId}`;
            
            // Navigate to invalid message URL
            mockHistory.pushState(null, '', invalidUrl);
            
            expect(mockHistory.getCurrentPath()).toBe(invalidUrl);
            
            // Validate the path (should be invalid)
            const validation = validateNavigationPath(invalidUrl);
            expect(validation.isValid).toBe(false);
            expect(validation.error).toBeTruthy();
        });
    });

    describe('Complex Navigation Flows', () => {
        it('should handle complete user journey with back/forward', () => {
            const messageId1 = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
            const messageId2 = 'B2C3D4E5-F6G7-H8I9-J0K1-L2M3N4O5P6Q7';
            const userId = 'testuser';
            
            // User journey: Home -> Message1 -> UserMessages -> Message2
            const journey = [
                '/',
                generateMessageUrl(messageId1),
                generateUserMessagesUrl(userId),
                generateMessageUrl(messageId2)
            ];
            
            // Navigate through the journey
            journey.slice(1).forEach(path => {
                mockHistory.pushState(null, '', path);
            });
            
            expect(mockHistory.getCurrentPath()).toBe(generateMessageUrl(messageId2));
            expect(mockHistory.getLength()).toBe(4);
            
            // Navigate back through history
            let backPath = mockHistory.back(); // Back to user messages
            expect(backPath).toBe(generateUserMessagesUrl(userId));
            
            backPath = mockHistory.back(); // Back to message1
            expect(backPath).toBe(generateMessageUrl(messageId1));
            
            backPath = mockHistory.back(); // Back to home
            expect(backPath).toBe('/');
            
            // Navigate forward again
            let forwardPath = mockHistory.forward(); // Forward to message1
            expect(forwardPath).toBe(generateMessageUrl(messageId1));
            
            forwardPath = mockHistory.forward(); // Forward to user messages
            expect(forwardPath).toBe(generateUserMessagesUrl(userId));
        });

        it('should handle navigation with pagination', () => {
            const userId = 'testuser';
            
            // Navigate through paginated user messages
            const paginatedJourney = [
                '/',
                generateUserMessagesUrl(userId), // Page 1
                generateUserMessagesUrl(userId, 2), // Page 2
                generateUserMessagesUrl(userId, 3), // Page 3
            ];
            
            paginatedJourney.slice(1).forEach(path => {
                mockHistory.pushState(null, '', path);
            });
            
            expect(mockHistory.getCurrentPath()).toBe(generateUserMessagesUrl(userId, 3));
            
            // Navigate back through pages
            let backPath = mockHistory.back(); // Back to page 2
            expect(backPath).toBe(generateUserMessagesUrl(userId, 2));
            
            backPath = mockHistory.back(); // Back to page 1
            expect(backPath).toBe(generateUserMessagesUrl(userId));
            
            backPath = mockHistory.back(); // Back to home
            expect(backPath).toBe('/');
        });

        it('should handle mixed valid and invalid navigation', () => {
            const validMessageId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
            const invalidMessageId = 'invalid-id';
            
            const mixedJourney = [
                '/',
                generateMessageUrl(validMessageId), // Valid
                `/message/${invalidMessageId}`, // Invalid
                generateHomeUrl(2) // Valid page 2
            ];
            
            mixedJourney.slice(1).forEach(path => {
                mockHistory.pushState(null, '', path);
            });
            
            // Validate each path in history
            const validationResults = mockHistory.entries.map(path => ({
                path,
                validation: validateNavigationPath(path)
            }));
            
            expect(validationResults[0].validation.isValid).toBe(true); // Home
            expect(validationResults[1].validation.isValid).toBe(true); // Valid message
            expect(validationResults[2].validation.isValid).toBe(false); // Invalid message
            expect(validationResults[3].validation.isValid).toBe(true); // Page 2
            
            // Navigate back to valid paths
            mockHistory.back(); // Back to invalid message (still in history)
            expect(mockHistory.getCurrentPath()).toBe(`/message/${invalidMessageId}`);
            
            mockHistory.back(); // Back to valid message
            expect(mockHistory.getCurrentPath()).toBe(generateMessageUrl(validMessageId));
            
            // Validate current path
            const currentValidation = validateNavigationPath(mockHistory.getCurrentPath());
            expect(currentValidation.isValid).toBe(true);
        });
    });

    describe('URL State Management', () => {
        it('should handle URL parameters and fragments', () => {
            const messageId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
            const baseUrl = generateMessageUrl(messageId);
            
            // Test URL with query parameters
            const urlWithQuery = `${baseUrl}?ref=home&tab=details`;
            mockHistory.pushState(null, '', urlWithQuery);
            
            expect(mockHistory.getCurrentPath()).toBe(urlWithQuery);
            
            // Extract message ID from URL with query
            const parseResult = parseMessageIdFromPath(urlWithQuery.split('?')[0]);
            expect(parseResult.isValid).toBe(true);
            expect(parseResult.messageId).toBe(messageId);
            
            // Test URL with fragment
            const urlWithFragment = `${baseUrl}#comments`;
            mockHistory.pushState(null, '', urlWithFragment);
            
            expect(mockHistory.getCurrentPath()).toBe(urlWithFragment);
            
            // Extract message ID from URL with fragment
            const parseFragmentResult = parseMessageIdFromPath(urlWithFragment.split('#')[0]);
            expect(parseFragmentResult.isValid).toBe(true);
            expect(parseFragmentResult.messageId).toBe(messageId);
        });

        it('should handle URL encoding and decoding', () => {
            const messageId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
            const originalUrl = generateMessageUrl(messageId);
            
            // For this ID format, encoding shouldn't change the URL
            const encodedUrl = encodeURI(originalUrl);
            expect(encodedUrl).toBe(originalUrl);
            
            mockHistory.pushState(null, '', encodedUrl);
            expect(mockHistory.getCurrentPath()).toBe(originalUrl);
            
            // Decode and validate
            const decodedUrl = decodeURI(mockHistory.getCurrentPath());
            expect(decodedUrl).toBe(originalUrl);
            
            const parseResult = parseMessageIdFromPath(decodedUrl);
            expect(parseResult.isValid).toBe(true);
            expect(parseResult.messageId).toBe(messageId);
        });

        it('should handle history state replacement', () => {
            const messageId1 = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
            const messageId2 = 'B2C3D4E5-F6G7-H8I9-J0K1-L2M3N4O5P6Q7';
            
            // Navigate to first message
            const url1 = generateMessageUrl(messageId1);
            mockHistory.pushState(null, '', url1);
            
            expect(mockHistory.getCurrentPath()).toBe(url1);
            expect(mockHistory.getLength()).toBe(2); // Home + Message1
            
            // Replace current state with second message
            const url2 = generateMessageUrl(messageId2);
            mockHistory.replaceState(null, '', url2);
            
            expect(mockHistory.getCurrentPath()).toBe(url2);
            expect(mockHistory.getLength()).toBe(2); // Home + Message2 (replaced)
            
            // Go back should go to home, not message1
            const backPath = mockHistory.back();
            expect(backPath).toBe('/');
        });
    });

    describe('Error Handling in Navigation', () => {
        it('should handle navigation bounds correctly', () => {
            const messageId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
            
            // Try to go back when at the beginning
            const backResult = mockHistory.back();
            expect(backResult).toBeNull();
            expect(mockHistory.getCurrentPath()).toBe('/');
            
            // Navigate forward
            mockHistory.pushState(null, '', generateMessageUrl(messageId));
            
            // Try to go forward when at the end
            const forwardResult = mockHistory.forward();
            expect(forwardResult).toBeNull();
            expect(mockHistory.getCurrentPath()).toBe(generateMessageUrl(messageId));
        });

        it('should handle history.go() with various deltas', () => {
            const messageIds = [
                'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6',
                'B2C3D4E5-F6G7-H8I9-J0K1-L2M3N4O5P6Q7',
                'C3D4E5F6-G7H8-I9J0-K1L2-M3N4O5P6Q7R8'
            ];
            
            // Build history: Home -> Message1 -> Message2 -> Message3
            messageIds.forEach(id => {
                mockHistory.pushState(null, '', generateMessageUrl(id));
            });
            
            expect(mockHistory.currentIndex).toBe(3); // At Message3
            
            // Go back 2 steps
            const goResult1 = mockHistory.go(-2);
            expect(goResult1).toBe(generateMessageUrl(messageIds[0])); // At Message1
            expect(mockHistory.currentIndex).toBe(1);
            
            // Go forward 1 step
            const goResult2 = mockHistory.go(1);
            expect(goResult2).toBe(generateMessageUrl(messageIds[1])); // At Message2
            expect(mockHistory.currentIndex).toBe(2);
            
            // Try to go beyond bounds
            const goResult3 = mockHistory.go(5);
            expect(goResult3).toBeNull();
            expect(mockHistory.currentIndex).toBe(2); // Should stay at Message2
            
            const goResult4 = mockHistory.go(-10);
            expect(goResult4).toBeNull();
            expect(mockHistory.currentIndex).toBe(2); // Should stay at Message2
        });

        it('should handle rapid navigation changes', () => {
            const messageIds = [
                'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6',
                'B2C3D4E5-F6G7-H8I9-J0K1-L2M3N4O5P6Q7',
                'C3D4E5F6-G7H8-I9J0-K1L2-M3N4O5P6Q7R8'
            ];
            
            // Simulate rapid navigation
            messageIds.forEach(id => {
                mockHistory.pushState(null, '', generateMessageUrl(id));
            });
            
            // Rapid back and forward
            for (let i = 0; i < 5; i++) {
                mockHistory.back();
                mockHistory.forward();
            }
            
            // Should end up at the last message
            expect(mockHistory.getCurrentPath()).toBe(generateMessageUrl(messageIds[2]));
            
            // Validate all entries are still valid
            mockHistory.entries.forEach(path => {
                if (path !== '/') {
                    const parseResult = parseMessageIdFromPath(path);
                    expect(parseResult.isValid).toBe(true);
                }
            });
        });
    });

    describe('Performance Considerations', () => {
        it('should handle large navigation history efficiently', () => {
            const startTime = performance.now();
            
            // Create a large history (simulate user browsing many messages)
            const baseId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
            for (let i = 0; i < 100; i++) {
                // Create valid variations by changing the last character
                const lastChar = String.fromCharCode(65 + (i % 26)); // A-Z
                const messageId = baseId.slice(0, -1) + lastChar;
                mockHistory.pushState(null, '', generateMessageUrl(messageId));
            }
            
            const endTime = performance.now();
            const duration = endTime - startTime;
            
            // Should complete within reasonable time (less than 100ms)
            expect(duration).toBeLessThan(100);
            expect(mockHistory.getLength()).toBe(101); // 100 messages + home
            
            // Test navigation performance
            const navStartTime = performance.now();
            
            // Navigate back 50 steps
            for (let i = 0; i < 50; i++) {
                mockHistory.back();
            }
            
            const navEndTime = performance.now();
            const navDuration = navEndTime - navStartTime;
            
            expect(navDuration).toBeLessThan(50);
            expect(mockHistory.currentIndex).toBe(50);
        });

        it('should validate navigation paths efficiently', () => {
            const messageIds = [];
            
            // Generate test message IDs with valid format
            const baseIds = [
                'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6',
                'B2C3D4E5-F6G7-H8I9-J0K1-L2M3N4O5P6Q7',
                'C3D4E5F6-G7H8-I9J0-K1L2-M3N4O5P6Q7R8',
                'D4E5F6G7-H8I9-J0K1-L2M3-N4O5P6Q7R8S9',
                'E5F6G7H8-I9J0-K1L2-M3N4-O5P6Q7R8S9T0'
            ];
            
            // Create 50 message IDs by repeating and modifying the base IDs
            for (let i = 0; i < 50; i++) {
                const baseId = baseIds[i % baseIds.length];
                // Modify the last character to create variations
                const lastChar = String.fromCharCode(65 + (i % 26)); // A-Z
                const modifiedId = baseId.slice(0, -1) + lastChar;
                messageIds.push(modifiedId);
            }
            
            const startTime = performance.now();
            
            // Validate all paths
            const validationResults = messageIds.map(id => {
                const path = generateMessageUrl(id);
                return validateNavigationPath(path);
            });
            
            const endTime = performance.now();
            const duration = endTime - startTime;
            
            // Should complete validation quickly
            expect(duration).toBeLessThan(50);
            
            // All should be valid
            validationResults.forEach(result => {
                expect(result.isValid).toBe(true);
            });
        });
    });
});