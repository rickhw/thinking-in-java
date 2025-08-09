const API_BASE_URL = 'http://localhost:8080/api/v1';

// ID format validation function for new 36-character message IDs
export const isValidMessageId = (id) => {
    if (!id || typeof id !== 'string') {
        return false;
    }
    // Format: XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX (8-4-4-4-12)
    // Character set: A-Z, 0-9
    const pattern = /^[A-Z0-9]{8}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{12}$/;
    return pattern.test(id);
};

// Enhanced error handling for API responses
const handleApiError = async (response, context = '') => {
    let errorMessage = `Network response was not ok (${response.status})`;
    
    try {
        const errorData = await response.json();
        if (errorData.message) {
            errorMessage = errorData.message;
        } else if (errorData.error) {
            errorMessage = errorData.error;
        }
        
        // Handle specific error types for message ID validation
        if (response.status === 400 && errorData.code === 'INVALID_MESSAGE_ID') {
            errorMessage = 'Invalid message ID format';
        }
    } catch (e) {
        // If response doesn't contain JSON, use default message
    }
    
    if (context) {
        errorMessage = `${context}: ${errorMessage}`;
    }
    
    throw new Error(errorMessage);
};

export const getMessages = async (page = 0, size = 10) => {
    const response = await fetch(`${API_BASE_URL}/messages?page=${page}&size=${size}`);
    if (!response.ok) {
        await handleApiError(response, 'Failed to get messages');
    }
    return response.json();
};

export const createMessage = async (userId, content) => {
    const message = { userId, content };
    const response = await fetch(`${API_BASE_URL}/messages`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(message),
    });
    if (!response.ok) {
        await handleApiError(response, 'Failed to create message');
    }
    // 根據設計文件，非同步操作會返回 202 狀態碼和任務 ID
    const result = await response.json();
    return result.taskId;
};

export const getTaskStatus = async (taskId) => {
    const response = await fetch(`${API_BASE_URL}/tasks/${taskId}`);
    if (!response.ok) {
        await handleApiError(response, 'Failed to get task status');
    }
    return response.json();
};

export const getMessagesByUserId = async (userId, page = 0, size = 10) => {
    const response = await fetch(`${API_BASE_URL}/messages/users/${userId}?page=${page}&size=${size}`);
    if (!response.ok) {
        await handleApiError(response, 'Failed to get messages by user');
    }
    return response.json();
};

export const getMessageById = async (messageId) => {
    // Validate message ID format before making API call
    if (!isValidMessageId(messageId)) {
        throw new Error('Invalid message ID format');
    }
    
    const response = await fetch(`${API_BASE_URL}/messages/${messageId}`);
    if (!response.ok) {
        if (response.status === 404) {
            throw new Error('Message not found');
        }
        await handleApiError(response, 'Failed to get message');
    }
    return response.json();
};

export const updateMessage = async (messageId, content) => {
    // Validate message ID format before making API call
    if (!isValidMessageId(messageId)) {
        throw new Error('Invalid message ID format');
    }
    
    const response = await fetch(`${API_BASE_URL}/messages/${messageId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ content }),
    });
    if (!response.ok) {
        await handleApiError(response, 'Failed to update message');
    }
    // 非同步操作返回任務 ID
    const result = await response.json();
    return result.taskId;
};

export const deleteMessage = async (messageId) => {
    // Validate message ID format before making API call
    if (!isValidMessageId(messageId)) {
        throw new Error('Invalid message ID format');
    }
    
    const response = await fetch(`${API_BASE_URL}/messages/${messageId}`, {
        method: 'DELETE',
    });
    if (!response.ok) {
        await handleApiError(response, 'Failed to delete message');
    }
    // 非同步操作返回任務 ID
    const result = await response.json();
    return result.taskId;
};

// User API functions
export const getUserById = async (userId) => {
    const response = await fetch(`${API_BASE_URL}/users/${userId}`);
    if (!response.ok) {
        throw new Error('Network response was not ok');
    }
    return response.json();
};

export const updateUser = async (userId, userData) => {
    const response = await fetch(`${API_BASE_URL}/users/${userId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(userData),
    });
    if (!response.ok) {
        throw new Error('Network response was not ok');
    }
    return response.json();
};

export const registerUser = async (userData) => {
    const response = await fetch(`${API_BASE_URL}/users/register`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(userData),
    });
    if (!response.ok) {
        throw new Error('Network response was not ok');
    }
    return response.json();
};

export const loginUser = async (username, password) => {
    const response = await fetch(`${API_BASE_URL}/users/login`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ username, password }),
    });
    const result = await response.json();
    if (!response.ok) {
        throw new Error(result.message || 'Login failed');
    }
    return result;
};