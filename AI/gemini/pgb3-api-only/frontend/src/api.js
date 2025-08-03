import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/v1';

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

export const getAllMessages = async () => {
  try {
    const response = await api.get('/messages');
    return response.data;
  } catch (error) {
    console.error('Error fetching all messages:', error);
    throw error;
  }
};

export const getMessagesByUserId = async (userId) => {
  try {
    const response = await api.get(`/users/${userId}/messages`);
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
