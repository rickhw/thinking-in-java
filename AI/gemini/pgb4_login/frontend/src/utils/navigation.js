/**
 * Navigation utilities for handling routes with new message ID format
 * Provides centralized navigation logic and URL generation
 */

import { isValidMessageId, validateMessageIdWithError } from './messageId';

/**
 * Navigation route constants
 */
export const ROUTES = {
    HOME: '/',
    LOGIN: '/login',
    REGISTER: '/register',
    CREATE_MESSAGE: '/create',
    MY_MESSAGES: '/messages',
    MY_PROFILE: '/profile',
    MESSAGE_DETAIL: '/message/:messageId',
    USER_MESSAGES: '/user/:userId/messages',
    PAGE: '/page/:pageNumber',
    MY_MESSAGES_PAGE: '/messages/page/:pageNumber',
    USER_MESSAGES_PAGE: '/user/:userId/messages/page/:pageNumber'
};

/**
 * Generate URL for message detail page
 * @param {string} messageId - The message ID
 * @returns {string} - The generated URL
 */
export const generateMessageUrl = (messageId) => {
    if (!messageId) {
        throw new Error('Message ID is required');
    }
    
    const validation = validateMessageIdWithError(messageId);
    if (!validation.isValid) {
        console.warn(`Invalid message ID format: ${messageId}. Error: ${validation.errorMessage}`);
        // Still generate URL but mark it as potentially problematic
        return `/message/${messageId}`;
    }
    
    return `/message/${messageId}`;
};

/**
 * Generate URL for user messages page
 * @param {string} userId - The user ID
 * @param {number} pageNumber - Optional page number (1-based)
 * @returns {string} - The generated URL
 */
export const generateUserMessagesUrl = (userId, pageNumber = null) => {
    if (!userId) {
        throw new Error('User ID is required');
    }
    
    const baseUrl = `/user/${userId}/messages`;
    
    if (pageNumber && pageNumber > 1) {
        return `${baseUrl}/page/${pageNumber}`;
    }
    
    return baseUrl;
};

/**
 * Generate URL for home page with pagination
 * @param {number} pageNumber - Optional page number (1-based)
 * @returns {string} - The generated URL
 */
export const generateHomeUrl = (pageNumber = null) => {
    if (pageNumber && pageNumber > 1) {
        return `/page/${pageNumber}`;
    }
    
    return '/';
};

/**
 * Generate URL for my messages page with pagination
 * @param {number} pageNumber - Optional page number (1-based)
 * @returns {string} - The generated URL
 */
export const generateMyMessagesUrl = (pageNumber = null) => {
    const baseUrl = '/messages';
    
    if (pageNumber && pageNumber > 1) {
        return `${baseUrl}/page/${pageNumber}`;
    }
    
    return baseUrl;
};

/**
 * Parse message ID from URL path
 * @param {string} path - The URL path
 * @returns {object} - Parsed result with messageId and validation
 */
export const parseMessageIdFromPath = (path) => {
    if (!path || typeof path !== 'string') {
        return {
            messageId: null,
            isValid: false,
            error: 'Invalid path'
        };
    }
    
    // Extract message ID from path like "/message/A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6"
    const pathParts = path.split('/');
    const messageIndex = pathParts.indexOf('message');
    
    if (messageIndex === -1 || messageIndex + 1 >= pathParts.length) {
        return {
            messageId: null,
            isValid: false,
            error: 'Message ID not found in path'
        };
    }
    
    const messageId = pathParts[messageIndex + 1];
    
    // Check if messageId is empty or undefined
    if (!messageId) {
        return {
            messageId: null,
            isValid: false,
            error: 'Message ID not found in path'
        };
    }
    
    const validation = validateMessageIdWithError(messageId);
    
    return {
        messageId,
        isValid: validation.isValid,
        error: validation.errorMessage
    };
};

/**
 * Parse user ID from URL path
 * @param {string} path - The URL path
 * @returns {object} - Parsed result with userId
 */
export const parseUserIdFromPath = (path) => {
    if (!path || typeof path !== 'string') {
        return {
            userId: null,
            error: 'Invalid path'
        };
    }
    
    // Extract user ID from path like "/user/testuser/messages"
    const pathParts = path.split('/');
    const userIndex = pathParts.indexOf('user');
    
    if (userIndex === -1 || userIndex + 1 >= pathParts.length) {
        return {
            userId: null,
            error: 'User ID not found in path'
        };
    }
    
    const userId = pathParts[userIndex + 1];
    
    // Check if userId is empty or undefined
    if (!userId) {
        return {
            userId: null,
            error: 'User ID not found in path'
        };
    }
    
    return {
        userId,
        error: null
    };
};

/**
 * Parse page number from URL path
 * @param {string} path - The URL path
 * @returns {object} - Parsed result with pageNumber
 */
export const parsePageNumberFromPath = (path) => {
    if (!path || typeof path !== 'string') {
        return {
            pageNumber: 1,
            error: 'Invalid path'
        };
    }
    
    // Extract page number from path like "/page/2" or "/messages/page/3"
    const pathParts = path.split('/');
    const pageIndex = pathParts.indexOf('page');
    
    if (pageIndex === -1 || pageIndex + 1 >= pathParts.length) {
        return {
            pageNumber: 1,
            error: null // No error, just default to page 1
        };
    }
    
    const pageNumberStr = pathParts[pageIndex + 1];
    const pageNumber = parseInt(pageNumberStr, 10);
    
    if (isNaN(pageNumber) || pageNumber < 1) {
        return {
            pageNumber: 1,
            error: 'Invalid page number'
        };
    }
    
    return {
        pageNumber,
        error: null
    };
};

/**
 * Validate navigation path for message-related routes
 * @param {string} path - The URL path to validate
 * @returns {object} - Validation result
 */
export const validateNavigationPath = (path) => {
    if (!path || typeof path !== 'string') {
        return {
            isValid: false,
            error: 'Invalid path',
            suggestions: ['Navigate to home page']
        };
    }
    
    // Check if it's a message detail path
    if (path.includes('/message/')) {
        const parseResult = parseMessageIdFromPath(path);
        if (!parseResult.isValid) {
            return {
                isValid: false,
                error: parseResult.error,
                suggestions: [
                    'Check the message ID format',
                    'Navigate to message list',
                    'Contact support if the link was provided by the system'
                ]
            };
        }
    }
    
    // Check if it's a user messages path
    if (path.includes('/user/') && path.includes('/messages')) {
        const userResult = parseUserIdFromPath(path);
        if (userResult.error) {
            return {
                isValid: false,
                error: userResult.error,
                suggestions: [
                    'Check the user ID in the URL',
                    'Navigate to home page'
                ]
            };
        }
    }
    
    // Check if it's a paginated path
    if (path.includes('/page/')) {
        const pageResult = parsePageNumberFromPath(path);
        if (pageResult.error && pageResult.pageNumber === 1) {
            return {
                isValid: false,
                error: pageResult.error,
                suggestions: [
                    'Check the page number in the URL',
                    'Navigate to first page'
                ]
            };
        }
    }
    
    return {
        isValid: true,
        error: null,
        suggestions: []
    };
};

/**
 * Generate breadcrumb navigation items
 * @param {string} currentPath - The current URL path
 * @param {object} context - Additional context (userId, messageId, etc.)
 * @returns {Array} - Array of breadcrumb items
 */
export const generateBreadcrumbs = (currentPath, context = {}) => {
    const breadcrumbs = [];
    
    // Always start with home
    breadcrumbs.push({
        text: '首頁',
        url: '/',
        isActive: currentPath === '/'
    });
    
    if (currentPath.includes('/message/')) {
        const parseResult = parseMessageIdFromPath(currentPath);
        if (parseResult.isValid) {
            breadcrumbs.push({
                text: '訊息詳情',
                url: currentPath,
                isActive: true
            });
        } else {
            breadcrumbs.push({
                text: '無效訊息',
                url: currentPath,
                isActive: true,
                hasError: true
            });
        }
    } else if (currentPath.includes('/user/') && currentPath.includes('/messages')) {
        const userResult = parseUserIdFromPath(currentPath);
        if (!userResult.error) {
            breadcrumbs.push({
                text: `${userResult.userId} 的訊息`,
                url: generateUserMessagesUrl(userResult.userId),
                isActive: true
            });
        }
    } else if (currentPath.includes('/messages')) {
        breadcrumbs.push({
            text: '我的訊息',
            url: '/messages',
            isActive: true
        });
    } else if (currentPath.includes('/create')) {
        breadcrumbs.push({
            text: '發布訊息',
            url: '/create',
            isActive: true
        });
    } else if (currentPath.includes('/profile')) {
        breadcrumbs.push({
            text: '個人資料',
            url: '/profile',
            isActive: true
        });
    }
    
    return breadcrumbs;
};

/**
 * Handle navigation errors and provide user-friendly messages
 * @param {Error} error - The navigation error
 * @param {string} attemptedPath - The path that caused the error
 * @returns {object} - Error handling result
 */
export const handleNavigationError = (error, attemptedPath) => {
    let userMessage = '導航錯誤';
    let suggestions = ['返回首頁'];
    let canRetry = false;
    
    if (attemptedPath && attemptedPath.includes('/message/')) {
        const parseResult = parseMessageIdFromPath(attemptedPath);
        if (!parseResult.isValid) {
            userMessage = '無效的訊息連結';
            suggestions = [
                '檢查連結是否正確',
                '返回訊息列表',
                '聯繫技術支援'
            ];
        } else {
            userMessage = '載入訊息時發生錯誤';
            suggestions = [
                '重新載入頁面',
                '檢查網路連線',
                '返回訊息列表'
            ];
            canRetry = true;
        }
    } else if (error.message && error.message.includes('network')) {
        userMessage = '網路連線錯誤';
        suggestions = [
            '檢查網路連線',
            '重新載入頁面',
            '稍後再試'
        ];
        canRetry = true;
    }
    
    return {
        userMessage,
        suggestions,
        canRetry,
        originalError: error.message
    };
};

/**
 * Check if a navigation path is safe to navigate to
 * @param {string} path - The path to check
 * @returns {boolean} - True if safe to navigate
 */
export const isSafeNavigationPath = (path) => {
    if (!path || typeof path !== 'string') {
        return false;
    }
    
    // Check for potentially dangerous paths
    const dangerousPatterns = [
        /javascript:/i,
        /data:/i,
        /vbscript:/i,
        /<script/i,
        /on\w+=/i
    ];
    
    return !dangerousPatterns.some(pattern => pattern.test(path));
};

/**
 * Normalize navigation path for consistent handling
 * @param {string} path - The path to normalize
 * @returns {string} - The normalized path
 */
export const normalizeNavigationPath = (path) => {
    if (!path || typeof path !== 'string') {
        return '/';
    }
    
    // Remove trailing slashes except for root
    let normalized = path.replace(/\/+$/, '') || '/';
    
    // Ensure it starts with /
    if (!normalized.startsWith('/')) {
        normalized = '/' + normalized;
    }
    
    // Remove double slashes
    normalized = normalized.replace(/\/+/g, '/');
    
    return normalized;
};