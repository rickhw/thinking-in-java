import { describe, it, expect } from 'vitest';
import {
    ROUTES,
    generateMessageUrl,
    generateUserMessagesUrl,
    generateHomeUrl,
    generateMyMessagesUrl,
    parseMessageIdFromPath,
    parseUserIdFromPath,
    parsePageNumberFromPath,
    validateNavigationPath,
    generateBreadcrumbs,
    handleNavigationError,
    isSafeNavigationPath,
    normalizeNavigationPath
} from '../utils/navigation';

describe('Navigation Utilities', () => {
    
    describe('Route Constants', () => {
        it('should have all required route constants', () => {
            expect(ROUTES.HOME).toBe('/');
            expect(ROUTES.MESSAGE_DETAIL).toBe('/message/:messageId');
            expect(ROUTES.USER_MESSAGES).toBe('/user/:userId/messages');
            expect(ROUTES.MY_MESSAGES).toBe('/messages');
            expect(ROUTES.LOGIN).toBe('/login');
            expect(ROUTES.REGISTER).toBe('/register');
            expect(ROUTES.CREATE_MESSAGE).toBe('/create');
            expect(ROUTES.MY_PROFILE).toBe('/profile');
        });
    });

    describe('URL Generation', () => {
        describe('generateMessageUrl', () => {
            it('should generate correct URL for valid message ID', () => {
                const messageId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
                const url = generateMessageUrl(messageId);
                expect(url).toBe(`/message/${messageId}`);
            });

            it('should throw error for missing message ID', () => {
                expect(() => generateMessageUrl()).toThrow('Message ID is required');
                expect(() => generateMessageUrl('')).toThrow('Message ID is required');
                expect(() => generateMessageUrl(null)).toThrow('Message ID is required');
            });

            it('should generate URL but warn for invalid message ID', () => {
                const consoleSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});
                const invalidId = 'invalid-id';
                const url = generateMessageUrl(invalidId);
                
                expect(url).toBe(`/message/${invalidId}`);
                expect(consoleSpy).toHaveBeenCalledWith(
                    expect.stringContaining('Invalid message ID format')
                );
                
                consoleSpy.mockRestore();
            });
        });

        describe('generateUserMessagesUrl', () => {
            it('should generate correct URL for user messages', () => {
                const userId = 'testuser';
                const url = generateUserMessagesUrl(userId);
                expect(url).toBe(`/user/${userId}/messages`);
            });

            it('should generate correct URL with page number', () => {
                const userId = 'testuser';
                const url = generateUserMessagesUrl(userId, 2);
                expect(url).toBe(`/user/${userId}/messages/page/2`);
            });

            it('should ignore page number 1', () => {
                const userId = 'testuser';
                const url = generateUserMessagesUrl(userId, 1);
                expect(url).toBe(`/user/${userId}/messages`);
            });

            it('should throw error for missing user ID', () => {
                expect(() => generateUserMessagesUrl()).toThrow('User ID is required');
                expect(() => generateUserMessagesUrl('')).toThrow('User ID is required');
            });
        });

        describe('generateHomeUrl', () => {
            it('should generate home URL without page number', () => {
                const url = generateHomeUrl();
                expect(url).toBe('/');
            });

            it('should generate home URL with page number', () => {
                const url = generateHomeUrl(2);
                expect(url).toBe('/page/2');
            });

            it('should ignore page number 1', () => {
                const url = generateHomeUrl(1);
                expect(url).toBe('/');
            });
        });

        describe('generateMyMessagesUrl', () => {
            it('should generate my messages URL without page number', () => {
                const url = generateMyMessagesUrl();
                expect(url).toBe('/messages');
            });

            it('should generate my messages URL with page number', () => {
                const url = generateMyMessagesUrl(3);
                expect(url).toBe('/messages/page/3');
            });

            it('should ignore page number 1', () => {
                const url = generateMyMessagesUrl(1);
                expect(url).toBe('/messages');
            });
        });
    });

    describe('Path Parsing', () => {
        describe('parseMessageIdFromPath', () => {
            it('should parse valid message ID from path', () => {
                const messageId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
                const path = `/message/${messageId}`;
                const result = parseMessageIdFromPath(path);
                
                expect(result.messageId).toBe(messageId);
                expect(result.isValid).toBe(true);
                expect(result.error).toBeNull();
            });

            it('should parse invalid message ID from path', () => {
                const invalidId = 'invalid-id';
                const path = `/message/${invalidId}`;
                const result = parseMessageIdFromPath(path);
                
                expect(result.messageId).toBe(invalidId);
                expect(result.isValid).toBe(false);
                expect(result.error).toBeTruthy();
            });

            it('should handle path without message ID', () => {
                const result = parseMessageIdFromPath('/message/');
                
                expect(result.messageId).toBeNull();
                expect(result.isValid).toBe(false);
                expect(result.error).toBe('Message ID not found in path');
            });

            it('should handle invalid paths', () => {
                const testCases = [
                    null,
                    undefined,
                    '',
                    '/home',
                    '/user/test/messages'
                ];

                testCases.forEach(path => {
                    const result = parseMessageIdFromPath(path);
                    expect(result.messageId).toBeNull();
                    expect(result.isValid).toBe(false);
                    expect(result.error).toBeTruthy();
                });
            });
        });

        describe('parseUserIdFromPath', () => {
            it('should parse user ID from path', () => {
                const userId = 'testuser';
                const path = `/user/${userId}/messages`;
                const result = parseUserIdFromPath(path);
                
                expect(result.userId).toBe(userId);
                expect(result.error).toBeNull();
            });

            it('should handle path without user ID', () => {
                const result = parseUserIdFromPath('/user/');
                
                expect(result.userId).toBeNull();
                expect(result.error).toBe('User ID not found in path');
            });

            it('should handle invalid paths', () => {
                const testCases = [null, undefined, '', '/home', '/message/123'];

                testCases.forEach(path => {
                    const result = parseUserIdFromPath(path);
                    expect(result.userId).toBeNull();
                    expect(result.error).toBeTruthy();
                });
            });
        });

        describe('parsePageNumberFromPath', () => {
            it('should parse page number from path', () => {
                const testCases = [
                    { path: '/page/2', expected: 2 },
                    { path: '/messages/page/3', expected: 3 },
                    { path: '/user/test/messages/page/5', expected: 5 }
                ];

                testCases.forEach(testCase => {
                    const result = parsePageNumberFromPath(testCase.path);
                    expect(result.pageNumber).toBe(testCase.expected);
                    expect(result.error).toBeNull();
                });
            });

            it('should default to page 1 for paths without page number', () => {
                const testCases = ['/', '/messages', '/user/test/messages'];

                testCases.forEach(path => {
                    const result = parsePageNumberFromPath(path);
                    expect(result.pageNumber).toBe(1);
                    expect(result.error).toBeNull();
                });
            });

            it('should handle invalid page numbers', () => {
                const testCases = [
                    '/page/abc',
                    '/page/0',
                    '/page/-1',
                    '/page/'
                ];

                testCases.forEach(path => {
                    const result = parsePageNumberFromPath(path);
                    expect(result.pageNumber).toBe(1);
                    expect(result.error).toBe('Invalid page number');
                });
            });
        });
    });

    describe('Path Validation', () => {
        describe('validateNavigationPath', () => {
            it('should validate valid message paths', () => {
                const validId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
                const path = `/message/${validId}`;
                const result = validateNavigationPath(path);
                
                expect(result.isValid).toBe(true);
                expect(result.error).toBeNull();
                expect(result.suggestions).toHaveLength(0);
            });

            it('should invalidate paths with invalid message IDs', () => {
                const invalidPath = '/message/invalid-id';
                const result = validateNavigationPath(invalidPath);
                
                expect(result.isValid).toBe(false);
                expect(result.error).toBeTruthy();
                expect(result.suggestions).toContain('Check the message ID format');
            });

            it('should validate user message paths', () => {
                const path = '/user/testuser/messages';
                const result = validateNavigationPath(path);
                
                expect(result.isValid).toBe(true);
                expect(result.error).toBeNull();
            });

            it('should validate paginated paths', () => {
                const path = '/messages/page/2';
                const result = validateNavigationPath(path);
                
                expect(result.isValid).toBe(true);
                expect(result.error).toBeNull();
            });

            it('should handle invalid paths', () => {
                const testCases = [null, undefined, ''];

                testCases.forEach(path => {
                    const result = validateNavigationPath(path);
                    expect(result.isValid).toBe(false);
                    expect(result.error).toBe('Invalid path');
                });
            });
        });
    });

    describe('Breadcrumb Generation', () => {
        describe('generateBreadcrumbs', () => {
            it('should generate breadcrumbs for home page', () => {
                const breadcrumbs = generateBreadcrumbs('/');
                
                expect(breadcrumbs).toHaveLength(1);
                expect(breadcrumbs[0].text).toBe('首頁');
                expect(breadcrumbs[0].url).toBe('/');
                expect(breadcrumbs[0].isActive).toBe(true);
            });

            it('should generate breadcrumbs for message detail page', () => {
                const messageId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
                const path = `/message/${messageId}`;
                const breadcrumbs = generateBreadcrumbs(path);
                
                expect(breadcrumbs).toHaveLength(2);
                expect(breadcrumbs[0].text).toBe('首頁');
                expect(breadcrumbs[0].isActive).toBe(false);
                expect(breadcrumbs[1].text).toBe('訊息詳情');
                expect(breadcrumbs[1].isActive).toBe(true);
            });

            it('should generate breadcrumbs for user messages page', () => {
                const path = '/user/testuser/messages';
                const breadcrumbs = generateBreadcrumbs(path);
                
                expect(breadcrumbs).toHaveLength(2);
                expect(breadcrumbs[0].text).toBe('首頁');
                expect(breadcrumbs[1].text).toBe('testuser 的訊息');
                expect(breadcrumbs[1].isActive).toBe(true);
            });

            it('should generate breadcrumbs for my messages page', () => {
                const breadcrumbs = generateBreadcrumbs('/messages');
                
                expect(breadcrumbs).toHaveLength(2);
                expect(breadcrumbs[0].text).toBe('首頁');
                expect(breadcrumbs[1].text).toBe('我的訊息');
                expect(breadcrumbs[1].isActive).toBe(true);
            });

            it('should handle invalid message paths in breadcrumbs', () => {
                const path = '/message/invalid-id';
                const breadcrumbs = generateBreadcrumbs(path);
                
                expect(breadcrumbs).toHaveLength(2);
                expect(breadcrumbs[1].text).toBe('無效訊息');
                expect(breadcrumbs[1].hasError).toBe(true);
            });
        });
    });

    describe('Error Handling', () => {
        describe('handleNavigationError', () => {
            it('should handle message navigation errors', () => {
                const error = new Error('Network error');
                const path = '/message/invalid-id';
                const result = handleNavigationError(error, path);
                
                expect(result.userMessage).toBe('無效的訊息連結');
                expect(result.suggestions).toContain('檢查連結是否正確');
                expect(result.canRetry).toBe(false);
            });

            it('should handle network errors', () => {
                const error = new Error('network timeout');
                const path = '/messages';
                const result = handleNavigationError(error, path);
                
                expect(result.userMessage).toBe('網路連線錯誤');
                expect(result.suggestions).toContain('檢查網路連線');
                expect(result.canRetry).toBe(true);
            });

            it('should handle valid message path with error', () => {
                const error = new Error('Server error');
                const validId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
                const path = `/message/${validId}`;
                const result = handleNavigationError(error, path);
                
                expect(result.userMessage).toBe('載入訊息時發生錯誤');
                expect(result.canRetry).toBe(true);
            });
        });
    });

    describe('Safety Checks', () => {
        describe('isSafeNavigationPath', () => {
            it('should allow safe paths', () => {
                const safePaths = [
                    '/',
                    '/messages',
                    '/message/A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6',
                    '/user/testuser/messages',
                    '/page/2'
                ];

                safePaths.forEach(path => {
                    expect(isSafeNavigationPath(path)).toBe(true);
                });
            });

            it('should block dangerous paths', () => {
                const dangerousPaths = [
                    'javascript:alert(1)',
                    'data:text/html,<script>alert(1)</script>',
                    'vbscript:msgbox(1)',
                    '<script>alert(1)</script>',
                    'onclick=alert(1)'
                ];

                dangerousPaths.forEach(path => {
                    expect(isSafeNavigationPath(path)).toBe(false);
                });
            });

            it('should handle invalid inputs', () => {
                const invalidInputs = [null, undefined, '', 123, {}];

                invalidInputs.forEach(input => {
                    expect(isSafeNavigationPath(input)).toBe(false);
                });
            });
        });
    });

    describe('Path Normalization', () => {
        describe('normalizeNavigationPath', () => {
            it('should normalize valid paths', () => {
                const testCases = [
                    { input: '/', expected: '/' },
                    { input: '/messages', expected: '/messages' },
                    { input: '/messages/', expected: '/messages' },
                    { input: '/messages//', expected: '/messages' },
                    { input: '//messages//', expected: '/messages' },
                    { input: 'messages', expected: '/messages' }
                ];

                testCases.forEach(testCase => {
                    const result = normalizeNavigationPath(testCase.input);
                    expect(result).toBe(testCase.expected);
                });
            });

            it('should handle invalid inputs', () => {
                const invalidInputs = [null, undefined, '', 123, {}];

                invalidInputs.forEach(input => {
                    const result = normalizeNavigationPath(input);
                    expect(result).toBe('/');
                });
            });

            it('should preserve root path', () => {
                const testCases = ['/', '//', '///'];

                testCases.forEach(input => {
                    const result = normalizeNavigationPath(input);
                    expect(result).toBe('/');
                });
            });
        });
    });
});