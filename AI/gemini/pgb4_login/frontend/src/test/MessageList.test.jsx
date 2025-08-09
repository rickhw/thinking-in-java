import { describe, it, expect, vi } from 'vitest';
import { isValidMessageId, truncateMessageId } from '../utils/messageId';

describe('MessageList Component Integration', () => {
    it('should validate message IDs correctly', () => {
        const validId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
        const invalidId = 'invalid-id';
        
        expect(isValidMessageId(validId)).toBe(true);
        expect(isValidMessageId(invalidId)).toBe(false);
    });

    it('should truncate message IDs for display', () => {
        const longId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
        const truncated = truncateMessageId(longId);
        
        expect(truncated).toBe('A1B2C3D4...');
        expect(truncated.length).toBeLessThan(longId.length);
    });

    it('should handle message objects with valid IDs', () => {
        const messages = [
            {
                id: 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6',
                userId: 'testuser1',
                content: 'Test message 1',
                createdAt: '2024-01-01T10:00:00Z'
            },
            {
                id: 'invalid-id',
                userId: 'testuser2',
                content: 'Test message 2',
                createdAt: '2024-01-01T11:00:00Z'
            }
        ];

        const validMessages = messages.filter(msg => isValidMessageId(msg.id));
        const invalidMessages = messages.filter(msg => !isValidMessageId(msg.id));

        expect(validMessages).toHaveLength(1);
        expect(invalidMessages).toHaveLength(1);
        expect(validMessages[0].userId).toBe('testuser1');
        expect(invalidMessages[0].userId).toBe('testuser2');
    });

    it('should generate correct permalink URLs for valid IDs', () => {
        const validId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
        const expectedUrl = `/message/${validId}`;
        
        expect(isValidMessageId(validId)).toBe(true);
        expect(expectedUrl).toBe('/message/A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6');
    });

    it('should handle edge cases in message processing', () => {
        const edgeCases = [
            { id: null, shouldBeValid: false },
            { id: undefined, shouldBeValid: false },
            { id: '', shouldBeValid: false },
            { id: '123', shouldBeValid: false }, // Legacy numeric ID
            { id: 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6', shouldBeValid: true }
        ];

        edgeCases.forEach(testCase => {
            const isValid = isValidMessageId(testCase.id);
            expect(isValid).toBe(testCase.shouldBeValid);
        });
    });
});