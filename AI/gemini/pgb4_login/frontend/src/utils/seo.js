/**
 * SEO utility functions for page title and meta tag management
 */

/**
 * Truncate text to specified length and add ellipsis if needed
 * @param {string} text - The text to truncate
 * @param {number} maxLength - Maximum length of the text
 * @returns {string} Truncated text with ellipsis if needed
 */
export const truncateText = (text, maxLength) => {
    if (!text || typeof text !== 'string') {
        return '';
    }
    
    if (text.length <= maxLength) {
        return text;
    }
    
    return text.substring(0, maxLength).trim() + '...';
};

/**
 * Clean text for SEO purposes by removing extra whitespace and line breaks
 * @param {string} text - The text to clean
 * @returns {string} Cleaned text
 */
export const cleanTextForSEO = (text) => {
    if (!text || typeof text !== 'string') {
        return '';
    }
    
    return text
        .replace(/\s+/g, ' ') // Replace multiple whitespace with single space
        .replace(/\n/g, ' ')  // Replace line breaks with space
        .trim();              // Remove leading/trailing whitespace
};

/**
 * Generate page title for a message
 * @param {Object} message - The message object
 * @param {string} message.content - The message content
 * @param {number} maxLength - Maximum length for the title preview (default: 50)
 * @returns {string} Generated page title
 */
export const generateMessageTitle = (message, maxLength = 50) => {
    if (!message || !message.content) {
        return 'Message Board';
    }
    
    const cleanContent = cleanTextForSEO(message.content);
    const preview = truncateText(cleanContent, maxLength);
    
    return `${preview} - Message Board`;
};

/**
 * Generate meta description for a message
 * @param {Object} message - The message object
 * @param {string} message.content - The message content
 * @param {number} maxLength - Maximum length for the description (default: 150)
 * @returns {string} Generated meta description
 */
export const generateMessageDescription = (message, maxLength = 150) => {
    if (!message || !message.content) {
        return 'Message Board - 分享您的想法和訊息';
    }
    
    const cleanContent = cleanTextForSEO(message.content);
    return truncateText(cleanContent, maxLength);
};

/**
 * Generate structured data for a message
 * @param {Object} message - The message object
 * @param {string} title - The page title
 * @param {string} description - The page description
 * @returns {Object} Structured data object
 */
export const generateMessageStructuredData = (message, title, description) => {
    if (!message) {
        return null;
    }
    
    return {
        "@context": "https://schema.org",
        "@type": "SocialMediaPosting",
        "headline": title,
        "author": {
            "@type": "Person",
            "name": message.userId
        },
        "datePublished": message.createdAt,
        "dateModified": message.updatedAt || message.createdAt,
        "text": description,
        "url": window.location.href
    };
};

/**
 * Predefined page titles for different states
 */
export const PAGE_TITLES = {
    LOADING: '載入中... - Message Board',
    NOT_FOUND: '訊息不存在 - Message Board',
    NETWORK_ERROR: '網路連線錯誤 - Message Board',
    GENERAL_ERROR: '載入錯誤 - Message Board',
    DEFAULT: 'Message Board'
};

/**
 * Predefined page descriptions for different states
 */
export const PAGE_DESCRIPTIONS = {
    LOADING: '正在載入訊息內容...',
    NOT_FOUND: '您要查看的訊息可能已被刪除或不存在',
    NETWORK_ERROR: '無法載入訊息內容，請檢查您的網路連線',
    GENERAL_ERROR: '載入訊息時發生錯誤',
    DEFAULT: 'Message Board - 分享您的想法和訊息'
};