import { describe, it, expect, beforeEach, vi } from 'vitest'
import {
  isValidMessageId,
  getMessages,
  createMessage,
  getMessageById,
  updateMessage,
  deleteMessage,
  getMessagesByUserId,
  getTaskStatus
} from '../api.js'

describe('Message ID Validation', () => {
  describe('isValidMessageId', () => {
    it('should return true for valid message ID format', () => {
      const validIds = [
        'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6',
        '12345678-ABCD-EFGH-IJKL-MNOPQRSTUVWX',
        'AAAAAAAA-BBBB-CCCC-DDDD-EEEEEEEEEEEE'
      ]
      
      validIds.forEach(id => {
        expect(isValidMessageId(id)).toBe(true)
      })
    })

    it('should return false for invalid message ID format', () => {
      const invalidIds = [
        null,
        undefined,
        '',
        'invalid-id',
        'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6X', // too long
        'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P', // too short
        'a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6', // lowercase
        'A1B2C3D4_E5F6_G7H8_I9J0_K1L2M3N4O5P6', // wrong separator
        'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6-', // extra separator
        'A1B2C3D4-E5F6G7H8-I9J0-K1L2M3N4O5P6', // missing separator
        'A1B2C3D4-E5F6-G7H8-I9J0K1L2M3N4O5P6', // missing separator
        '12345678-ABCD-EFGH-IJKL-MNOPQRSTUVW@' // invalid character
      ]
      
      invalidIds.forEach(id => {
        expect(isValidMessageId(id)).toBe(false)
      })
    })

    it('should return false for non-string inputs', () => {
      expect(isValidMessageId(123)).toBe(false)
      expect(isValidMessageId({})).toBe(false)
      expect(isValidMessageId([])).toBe(false)
    })
  })
})

describe('API Functions', () => {
  beforeEach(() => {
    fetch.mockClear()
  })

  describe('getMessages', () => {
    it('should fetch messages successfully', async () => {
      const mockResponse = {
        content: [{ id: 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6', content: 'Test message' }],
        totalElements: 1
      }
      
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockResponse
      })

      const result = await getMessages(0, 10)
      
      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/v1/messages?page=0&size=10')
      expect(result).toEqual(mockResponse)
    })

    it('should handle API errors', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        status: 500,
        json: async () => ({ message: 'Internal server error' })
      })

      await expect(getMessages()).rejects.toThrow('Failed to get messages: Internal server error')
    })
  })

  describe('createMessage', () => {
    it('should create message successfully', async () => {
      const mockResponse = { taskId: 'task-123' }
      
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockResponse
      })

      const result = await createMessage('user-123', 'Test content')
      
      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/v1/messages', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId: 'user-123', content: 'Test content' })
      })
      expect(result).toBe('task-123')
    })

    it('should handle creation errors', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        status: 400,
        json: async () => ({ message: 'Invalid request' })
      })

      await expect(createMessage('user-123', 'Test content'))
        .rejects.toThrow('Failed to create message: Invalid request')
    })
  })

  describe('getMessageById', () => {
    const validMessageId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6'

    it('should fetch message by ID successfully', async () => {
      const mockMessage = { id: validMessageId, content: 'Test message' }
      
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockMessage
      })

      const result = await getMessageById(validMessageId)
      
      expect(fetch).toHaveBeenCalledWith(`http://localhost:8080/api/v1/messages/${validMessageId}`)
      expect(result).toEqual(mockMessage)
    })

    it('should throw error for invalid message ID format', async () => {
      await expect(getMessageById('invalid-id'))
        .rejects.toThrow('Invalid message ID format')
      
      expect(fetch).not.toHaveBeenCalled()
    })

    it('should handle 404 errors', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        status: 404,
        json: async () => ({})
      })

      await expect(getMessageById(validMessageId))
        .rejects.toThrow('Message not found')
    })

    it('should handle invalid message ID errors from API', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        status: 400,
        json: async () => ({ code: 'INVALID_MESSAGE_ID', message: 'Invalid ID format' })
      })

      await expect(getMessageById(validMessageId))
        .rejects.toThrow('Failed to get message: Invalid message ID format')
    })
  })

  describe('updateMessage', () => {
    const validMessageId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6'

    it('should update message successfully', async () => {
      const mockResponse = { taskId: 'task-456' }
      
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockResponse
      })

      const result = await updateMessage(validMessageId, 'Updated content')
      
      expect(fetch).toHaveBeenCalledWith(`http://localhost:8080/api/v1/messages/${validMessageId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ content: 'Updated content' })
      })
      expect(result).toBe('task-456')
    })

    it('should throw error for invalid message ID format', async () => {
      await expect(updateMessage('invalid-id', 'Updated content'))
        .rejects.toThrow('Invalid message ID format')
      
      expect(fetch).not.toHaveBeenCalled()
    })

    it('should handle update errors', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        status: 400,
        json: async () => ({ message: 'Update failed' })
      })

      await expect(updateMessage(validMessageId, 'Updated content'))
        .rejects.toThrow('Failed to update message: Update failed')
    })
  })

  describe('deleteMessage', () => {
    const validMessageId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6'

    it('should delete message successfully', async () => {
      const mockResponse = { taskId: 'task-789' }
      
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockResponse
      })

      const result = await deleteMessage(validMessageId)
      
      expect(fetch).toHaveBeenCalledWith(`http://localhost:8080/api/v1/messages/${validMessageId}`, {
        method: 'DELETE'
      })
      expect(result).toBe('task-789')
    })

    it('should throw error for invalid message ID format', async () => {
      await expect(deleteMessage('invalid-id'))
        .rejects.toThrow('Invalid message ID format')
      
      expect(fetch).not.toHaveBeenCalled()
    })

    it('should handle delete errors', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        status: 403,
        json: async () => ({ message: 'Forbidden' })
      })

      await expect(deleteMessage(validMessageId))
        .rejects.toThrow('Failed to delete message: Forbidden')
    })
  })

  describe('getMessagesByUserId', () => {
    it('should fetch messages by user ID successfully', async () => {
      const mockResponse = {
        content: [{ id: 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6', content: 'User message' }],
        totalElements: 1
      }
      
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockResponse
      })

      const result = await getMessagesByUserId('user-123', 0, 5)
      
      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/v1/messages/users/user-123?page=0&size=5')
      expect(result).toEqual(mockResponse)
    })

    it('should handle errors', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        status: 404,
        json: async () => ({ message: 'User not found' })
      })

      await expect(getMessagesByUserId('user-123'))
        .rejects.toThrow('Failed to get messages by user: User not found')
    })
  })

  describe('getTaskStatus', () => {
    it('should fetch task status successfully', async () => {
      const mockResponse = { taskId: 'task-123', status: 'COMPLETED' }
      
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockResponse
      })

      const result = await getTaskStatus('task-123')
      
      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/v1/tasks/task-123')
      expect(result).toEqual(mockResponse)
    })

    it('should handle task status errors', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        status: 404,
        json: async () => ({ message: 'Task not found' })
      })

      await expect(getTaskStatus('task-123'))
        .rejects.toThrow('Failed to get task status: Task not found')
    })
  })

  describe('Error Handling', () => {
    it('should handle responses without JSON error data', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        status: 500,
        json: async () => { throw new Error('Not JSON') }
      })

      await expect(getMessages())
        .rejects.toThrow('Failed to get messages: Network response was not ok (500)')
    })

    it('should handle specific INVALID_MESSAGE_ID error code', async () => {
      const validMessageId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6'
      
      fetch.mockResolvedValueOnce({
        ok: false,
        status: 400,
        json: async () => ({ code: 'INVALID_MESSAGE_ID' })
      })

      await expect(getMessageById(validMessageId))
        .rejects.toThrow('Failed to get message: Invalid message ID format')
    })

    it('should handle error responses with error field instead of message', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        status: 400,
        json: async () => ({ error: 'Bad request error' })
      })

      await expect(getMessages())
        .rejects.toThrow('Failed to get messages: Bad request error')
    })
  })
})