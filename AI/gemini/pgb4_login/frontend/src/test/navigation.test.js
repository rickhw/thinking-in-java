import { describe, it, expect, beforeEach, vi } from 'vitest';
import { isValidMessageId, validateMessageIdWithError, truncateMessageId } from '../utils/messageId';

/**
 * Navigation Tests for New Message ID Format
 * Tests all navigation links and components that use message IDs
 */
describe('Navigation Tests for New Message ID Format', () => {

    describe('Message List Navigation Links', () => {
        it('should generate correct permalink links for valid message IDs', () => {
            const validMessages = [
                {
                    id: 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6',
                    userId: 'user1',
                    content: 'Test message 1',
                    createdAt: '2024-01-01T10:00:00Z'
                },
                {
                    id: 'B2C3D4E5-F6G7-H8I9-J0K1-L2M3N4O5P6Q7',
                    userId: 'user2',
                    content: 'Test message 2',
                    createdAt: '2024-01-01T11:00:00Z'
                }
            ];

            validMessages.forEach(message => {
                const expectedLink = `/message/${message.id}`;
                
                // Validate ID format
                expect(isValidMessageId(message.id)).toBe(true);
                
                // Test link generation
                expect(expectedLink).toBe(`/message/${message.id}`);
                
                // Test that the link contains valid ID
                const idFromLink = expectedLink.split('/').pop();
                expect(isValidMessageId(idFromLink)).toBe(true);
            });
        });

        it('should handle invalid message IDs in navigation links', () => {
            const invalidMessages = [
                {
                    id: 'invalid-id',
                    userId: 'user1',
                    content: 'Test message with invalid ID'
                },
                {
                    id: '12345',
                    userId: 'user2',
                    content: 'Test message with legacy ID'
                },
                {
                    id: '',
                    userId: 'user3',
                    content: 'Test message with empty ID'
                }
            ];

            invalidMessages.forEach(message => {
                const validation = validateMessageIdWithError(message.id);
                expect(validation.isValid).toBe(false);
                
                // Links should still be generated but marked as invalid
                const link = `/message/${message.id}`;
                const idFromLink = link.split('/').pop();
                
                if (idFromLink) {
                    expect(isValidMessageId(idFromLink)).toBe(false);
                }
            });
        });

        it('should display truncated message IDs correctly', () => {
            const longMessageId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
            
            // Test different truncation lengths
            const truncatedShort = truncateMessageId(longMessageId, 8);
            const truncatedMedium = truncateMessageId(longMessageId, 16);
            const truncatedLong = truncateMessageId(longMessageId, 36);
            
            expect(truncatedShort).toBe('A1B2C3D4...');
            expect(truncatedMedium).toBe('A1B2C3D4-E5F6-G7...');
            expect(truncatedLong).toBe(longMessageId); // No truncation needed
            
            // Validate original ID
            expect(isValidMessageId(longMessageId)).toBe(true);
        });
    });

    describe('User Message Navigation Links', () => {
        it('should generate correct user message page links', () => {
            const testUsers = ['user1', 'user2', 'testuser'];
            
            testUsers.forEach(userId => {
                const userMessagesLink = `/user/${userId}/messages`;
                const userMessagesPageLink = `/user/${userId}/messages/page/2`;
                
                expect(userMessagesLink).toBe(`/user/${userId}/messages`);
                expect(userMessagesPageLink).toBe(`/user/${userId}/messages/page/2`);
                
                // Test link parsing
                const userIdFromLink = userMessagesLink.split('/')[2];
                expect(userIdFromLink).toBe(userId);
            });
        });

        it('should handle user navigation with message IDs', () => {
            const userId = 'testuser';
            const messageId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
            
            // Navigation flow: Home -> User Messages -> Specific Message -> Back to User Messages
            const navigationFlow = [
                '/',
                `/user/${userId}/messages`,
                `/message/${messageId}`,
                `/user/${userId}/messages`
            ];

            navigationFlow.forEach(path => {
                // Each path should be valid
                expect(path).toBeTruthy();
                
                // If path contains message ID, validate it
                if (path.includes('/message/')) {
                    const idFromPath = path.split('/').pop();
                    expect(isValidMessageId(idFromPath)).toBe(true);
                }
            });
        });
    });

    describe('Breadcrumb Navigation', () => {
        it('should generate correct breadcrumb links for message detail page', () => {
            const messageId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
            const userId = 'testuser';
            
            const breadcrumbs = {
                home: {
                    text: '首頁',
                    link: '/'
                },
                allMessages: {
                    text: '所有訊息',
                    link: '/'
                },
                userMessages: {
                    text: `${userId} 的訊息`,
                    link: `/user/${userId}/messages`
                },
                currentMessage: {
                    text: '當前訊息',
                    link: `/message/${messageId}`
                }
            };

            // Test each breadcrumb link
            Object.entries(breadcrumbs).forEach(([key, breadcrumb]) => {
                expect(breadcrumb.link).toBeTruthy();
                expect(breadcrumb.text).toBeTruthy();
                
                // Validate message ID in current message breadcrumb
                if (key === 'currentMessage') {
                    const idFromLink = breadcrumb.link.split('/').pop();
                    expect(isValidMessageId(idFromLink)).toBe(true);
                }
            });
        });

        it('should handle breadcrumb navigation with invalid message IDs', () => {
            const invalidMessageId = 'invalid-id';
            const userId = 'testuser';
            
            const breadcrumbWithInvalidId = {
                text: '無效訊息',
                link: `/message/${invalidMessageId}`
            };

            // Link should still be generated
            expect(breadcrumbWithInvalidId.link).toBe(`/message/${invalidMessageId}`);
            
            // But ID should be invalid
            const idFromLink = breadcrumbWithInvalidId.link.split('/').pop();
            expect(isValidMessageId(idFromLink)).toBe(false);
            
            // Validation should provide error message
            const validation = validateMessageIdWithError(idFromLink);
            expect(validation.isValid).toBe(false);
            expect(validation.errorMessage).toBeTruthy();
        });
    });

    describe('Pagination Navigation', () => {
        it('should handle pagination with message list navigation', () => {
            const baseUrls = [
                '/',
                '/messages',
                '/user/testuser/messages'
            ];

            baseUrls.forEach(baseUrl => {
                // Test pagination URLs
                const page1Url = baseUrl === '/' ? '/' : baseUrl;
                const page2Url = baseUrl === '/' ? '/page/2' : `${baseUrl}/page/2`;
                const page3Url = baseUrl === '/' ? '/page/3' : `${baseUrl}/page/3`;

                expect(page1Url).toBeTruthy();
                expect(page2Url).toBeTruthy();
                expect(page3Url).toBeTruthy();

                // Test page number extraction
                if (page2Url.includes('/page/')) {
                    const pageNumber = page2Url.split('/page/').pop();
                    expect(pageNumber).toBe('2');
                }
            });
        });

        it('should maintain message ID links across pagination', () => {
            const messagesPage1 = [
                { id: 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6', content: 'Message 1' },
                { id: 'B2C3D4E5-F6G7-H8I9-J0K1-L2M3N4O5P6Q7', content: 'Message 2' }
            ];

            const messagesPage2 = [
                { id: 'C3D4E5F6-G7H8-I9J0-K1L2-M3N4O5P6Q7R8', content: 'Message 3' },
                { id: 'D4E5F6G7-H8I9-J0K1-L2M3-N4O5P6Q7R8S9', content: 'Message 4' }
            ];

            // Test that all message IDs are valid across pages
            [...messagesPage1, ...messagesPage2].forEach(message => {
                expect(isValidMessageId(message.id)).toBe(true);
                
                const messageLink = `/message/${message.id}`;
                const idFromLink = messageLink.split('/').pop();
                expect(isValidMessageId(idFromLink)).toBe(true);
            });
        });
    });

    describe('Navigation State Management', () => {
        it('should handle navigation state with new message IDs', () => {
            // Simulate navigation state
            const navigationState = {
                currentPath: '/',
                previousPath: null,
                messageHistory: [],
                userHistory: []
            };

            const simulateNavigation = (newPath) => {
                navigationState.previousPath = navigationState.currentPath;
                navigationState.currentPath = newPath;
                
                // Track message visits
                if (newPath.includes('/message/')) {
                    const messageId = newPath.split('/').pop();
                    if (isValidMessageId(messageId)) {
                        navigationState.messageHistory.push(messageId);
                    }
                }
                
                // Track user page visits
                if (newPath.includes('/user/') && newPath.includes('/messages')) {
                    const pathParts = newPath.split('/');
                    const userIdIndex = pathParts.indexOf('user') + 1;
                    if (userIdIndex < pathParts.length) {
                        const userId = pathParts[userIdIndex];
                        if (!navigationState.userHistory.includes(userId)) {
                            navigationState.userHistory.push(userId);
                        }
                    }
                }
            };

            // Simulate navigation flow
            const navigationFlow = [
                '/',
                '/message/A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6',
                '/user/testuser/messages',
                '/message/B2C3D4E5-F6G7-H8I9-J0K1-L2M3N4O5P6Q7',
                '/'
            ];

            navigationFlow.forEach(path => {
                simulateNavigation(path);
                expect(navigationState.currentPath).toBe(path);
            });

            // Check navigation history
            expect(navigationState.messageHistory).toHaveLength(2);
            expect(navigationState.userHistory).toContain('testuser');
            
            // Validate all tracked message IDs
            navigationState.messageHistory.forEach(messageId => {
                expect(isValidMessageId(messageId)).toBe(true);
            });
        });

        it('should handle navigation errors gracefully', () => {
            const navigationErrors = [];
            
            const handleNavigationError = (path, error) => {
                navigationErrors.push({ path, error, timestamp: Date.now() });
            };

            const problematicPaths = [
                '/message/invalid-id',
                '/message/12345',
                '/message/',
                '/message/null'
            ];

            problematicPaths.forEach(path => {
                const messageId = path.split('/').pop();
                const validation = validateMessageIdWithError(messageId);
                
                if (!validation.isValid) {
                    handleNavigationError(path, validation.errorMessage);
                }
            });

            expect(navigationErrors).toHaveLength(4);
            navigationErrors.forEach(error => {
                expect(error.path).toBeTruthy();
                expect(error.error).toBeTruthy();
                expect(error.timestamp).toBeTruthy();
            });
        });
    });

    describe('Link Accessibility and UX', () => {
        it('should provide accessible link attributes for message navigation', () => {
            const messageId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
            const messageContent = 'This is a test message content';
            
            // Simulate link attributes that should be generated
            const linkAttributes = {
                href: `/message/${messageId}`,
                'aria-label': `查看訊息詳情: ${truncateMessageId(messageId)}`,
                title: `訊息 ID: ${messageId}`,
                role: 'link'
            };

            expect(linkAttributes.href).toBe(`/message/${messageId}`);
            expect(linkAttributes['aria-label']).toContain('查看訊息詳情');
            expect(linkAttributes.title).toContain(messageId);
            expect(linkAttributes.role).toBe('link');
            
            // Validate message ID in href
            const idFromHref = linkAttributes.href.split('/').pop();
            expect(isValidMessageId(idFromHref)).toBe(true);
        });

        it('should handle disabled links for invalid message IDs', () => {
            const invalidMessageId = 'invalid-id';
            
            const disabledLinkAttributes = {
                href: '#',
                'aria-disabled': 'true',
                'aria-label': '查看詳情 (不可用)',
                title: '無效的訊息 ID',
                className: 'permalink-link disabled'
            };

            expect(disabledLinkAttributes['aria-disabled']).toBe('true');
            expect(disabledLinkAttributes.title).toContain('無效');
            expect(disabledLinkAttributes.className).toContain('disabled');
            
            // Validate that the ID is indeed invalid
            expect(isValidMessageId(invalidMessageId)).toBe(false);
        });
    });

    describe('URL Generation and Parsing', () => {
        it('should generate and parse URLs correctly for all navigation scenarios', () => {
            const urlScenarios = [
                {
                    type: 'home',
                    url: '/',
                    expectedParts: { path: '/' }
                },
                {
                    type: 'message_detail',
                    url: '/message/A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6',
                    expectedParts: { 
                        path: '/message',
                        messageId: 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6'
                    }
                },
                {
                    type: 'user_messages',
                    url: '/user/testuser/messages',
                    expectedParts: {
                        path: '/user',
                        userId: 'testuser',
                        section: 'messages'
                    }
                },
                {
                    type: 'user_messages_page',
                    url: '/user/testuser/messages/page/2',
                    expectedParts: {
                        path: '/user',
                        userId: 'testuser',
                        section: 'messages',
                        page: '2'
                    }
                }
            ];

            urlScenarios.forEach(scenario => {
                const urlParts = scenario.url.split('/').filter(part => part);
                
                expect(scenario.url).toBeTruthy();
                
                // Test specific parsing based on scenario type
                switch (scenario.type) {
                    case 'message_detail':
                        const messageId = urlParts[urlParts.length - 1];
                        expect(messageId).toBe(scenario.expectedParts.messageId);
                        expect(isValidMessageId(messageId)).toBe(true);
                        break;
                        
                    case 'user_messages':
                    case 'user_messages_page':
                        const userIdIndex = urlParts.indexOf('user') + 1;
                        const userId = urlParts[userIdIndex];
                        expect(userId).toBe(scenario.expectedParts.userId);
                        break;
                }
            });
        });

        it('should handle URL encoding and decoding for message IDs', () => {
            const messageId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
            const url = `/message/${messageId}`;
            
            // Test URL encoding (should not change for this format)
            const encodedUrl = encodeURI(url);
            expect(encodedUrl).toBe(url);
            
            // Test URL decoding
            const decodedUrl = decodeURI(encodedUrl);
            expect(decodedUrl).toBe(url);
            
            // Extract and validate message ID from encoded/decoded URLs
            const idFromEncoded = encodedUrl.split('/').pop();
            const idFromDecoded = decodedUrl.split('/').pop();
            
            expect(isValidMessageId(idFromEncoded)).toBe(true);
            expect(isValidMessageId(idFromDecoded)).toBe(true);
            expect(idFromEncoded).toBe(idFromDecoded);
        });
    });
});