import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

const api = axios.create({
  baseURL: API_BASE_URL,
});

export const createMessage = async (userId, content) => {
  try {
    const response = await api.post('/messages', { userId, content });
    return response.data; // Returns taskId
  } catch (error) {
    console.error('Error creating message:', error);
    throw error;
  }
};

export const getMessageById = async (messageId) => {
  try {
    const response = await api.get(`/messages/${messageId}`);
    return response.data;
  } catch (error) {
    console.error('Error fetching message by ID:', error);
    throw error;
  }
};

export const updateMessage = async (messageId, content) => {
  try {
    const response = await api.put(`/messages/${messageId}`, { content });
    return response.data; // Returns taskId
  } catch (error) {
    console.error('Error updating message:', error);
    throw error;
  }
};

export const deleteMessage = async (messageId) => {
  try {
    const response = await api.delete(`/messages/${messageId}`);
    return response.data; // Returns taskId
  } catch (error) {
    console.error('Error deleting message:', error);
    throw error;
  }
};

export const getAllMessages = async (page = 0, size = 10) => {
  try {
    const response = await api.get('/messages', {
      params: { page, size },
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching all messages:', error);
    throw error;
  }
};

export const getMessagesByUserId = async (userId, page = 0, size = 10) => {
  try {
    const response = await api.get(`/users/${userId}/messages`, {
      params: { page, size },
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching messages by user ID:', error);
    throw error;
  }
};

export const getTaskStatus = async (taskId) => {
  try {
    const response = await api.get(`/tasks/${taskId}`);
    return response.data;
  } catch (error) {
    console.error('Error fetching task status:', error);
    throw error;
  }
};
