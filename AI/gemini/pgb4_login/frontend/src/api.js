const API_BASE_URL = 'http://localhost:8080/api/v1';

export const getMessages = async (page = 0, size = 10) => {
    const response = await fetch(`${API_BASE_URL}/messages?page=${page}&size=${size}`);
    if (!response.ok) {
        throw new Error('Network response was not ok');
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
        throw new Error('Network response was not ok');
    }
    // 根據設計文件，非同步操作會返回 202 狀態碼和任務 ID
    const result = await response.json();
    return result.taskId;
};

export const getTaskStatus = async (taskId) => {
    const response = await fetch(`${API_BASE_URL}/tasks/${taskId}`);
    if (!response.ok) {
        throw new Error('Network response was not ok');
    }
    return response.json();
};

export const getMessagesByUserId = async (userId, page = 0, size = 10) => {
    const response = await fetch(`${API_BASE_URL}/messages/users/${userId}?page=${page}&size=${size}`);
    if (!response.ok) {
        throw new Error('Network response was not ok');
    }
    return response.json();
};

export const getMessageById = async (messageId) => {
    const response = await fetch(`${API_BASE_URL}/messages/${messageId}`);
    if (!response.ok) {
        if (response.status === 404) {
            throw new Error('Message not found (404)');
        }
        throw new Error(`Network response was not ok (${response.status})`);
    }
    return response.json();
};

export const updateMessage = async (messageId, content) => {
    const response = await fetch(`${API_BASE_URL}/messages/${messageId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ content }),
    });
    if (!response.ok) {
        throw new Error('Network response was not ok');
    }
    // 非同步操作返回任務 ID
    const result = await response.json();
    return result.taskId;
};

export const deleteMessage = async (messageId) => {
    const response = await fetch(`${API_BASE_URL}/messages/${messageId}`, {
        method: 'DELETE',
    });
    if (!response.ok) {
        throw new Error('Network response was not ok');
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