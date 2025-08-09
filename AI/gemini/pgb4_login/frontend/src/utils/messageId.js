/**
 * Utility functions for handling message ID validation and formatting
 * Supports the new 36-character ID format: XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX
 * Character set: A-Z, 0-9
 */

/**
 * Validates if a message ID has the correct format
 * @param {string} id - The message ID to validate
 * @returns {boolean} - True if the ID format is valid
 */
export const isValidMessageId = (id) => {
    if (!id || typeof id !== 'string') {
        return false;
    }
    // Format: XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX (8-4-4-4-12)
    // Character set: A-Z, 0-9
    const pattern = /^[A-Z0-9]{8}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{12}$/;
    return pattern.test(id);
};

/**
 * Formats a message ID for display (adds visual separation if needed)
 * @param {string} id - The message ID to format
 * @returns {string} - The formatted ID
 */
export const formatMessageIdForDisplay = (id) => {
    if (!isValidMessageId(id)) {
        return id; // Return as-is if invalid
    }
    return id; // Already formatted with dashes
};

/**
 * Truncates a message ID for compact display
 * @param {string} id - The message ID to truncate
 * @param {number} length - The desired length (default: 8)
 * @returns {string} - The truncated ID with ellipsis
 */
export const truncateMessageId = (id, length = 8) => {
    if (!id || typeof id !== 'string') {
        return '';
    }
    if (id.length <= length) {
        return id;
    }
    return id.substring(0, length) + '...';
};

/**
 * Validates message ID and provides user-friendly error messages
 * @param {string} id - The message ID to validate
 * @returns {object} - Validation result with isValid and errorMessage
 */
export const validateMessageIdWithError = (id) => {
    if (!id) {
        return {
            isValid: false,
            errorMessage: '訊息 ID 不能為空'
        };
    }
    
    if (typeof id !== 'string') {
        return {
            isValid: false,
            errorMessage: '訊息 ID 必須是字符串格式'
        };
    }
    
    // Check for legacy numeric ID first
    if (isLegacyNumericId(id)) {
        return {
            isValid: false,
            errorMessage: '舊的數字 ID 格式'
        };
    }
    
    if (!isValidMessageId(id)) {
        return {
            isValid: false,
            errorMessage: '訊息 ID 格式不正確，應為 36 位大寫字母和數字組成'
        };
    }
    
    return {
        isValid: true,
        errorMessage: null
    };
};

/**
 * Checks if an ID might be in the old numeric format
 * @param {string} id - The ID to check
 * @returns {boolean} - True if it looks like an old numeric ID
 */
export const isLegacyNumericId = (id) => {
    if (!id || typeof id !== 'string') {
        return false;
    }
    // Check if it's a pure number (old format)
    return /^\d+$/.test(id);
};