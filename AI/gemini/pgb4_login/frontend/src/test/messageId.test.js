import { describe, it, expect } from 'vitest';
import { 
    isValidMessageId, 
    formatMessageIdForDisplay, 
    truncateMessageId, 
    validateMessageIdWithError,
    isLegacyNumericId 
} from '../utils/messageId';

describe('Message ID Utilities', () => {
    describe('isValidMessageId', () => {
        it('should validate correct 36-character ID format', () => {
            const validId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
            expect(isValidMessageId(validId)).toBe(true);
        });

        it('should validate ID with all uppercase letters', () => {
            const validId = 'ABCDEFGH-IJKL-MNOP-QRST-UVWXYZABCDEF';
            expect(isValidMessageId(validId)).toBe(true);
        });

        it('should validate ID with all numbers', () => {
            const validId = '12345678-9012-3456-7890-123456789012';
            expect(isValidMessageId(validId)).toBe(true);
        });

        it('should reject ID with lowercase letters', () => {
            const invalidId = 'a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6';
            expect(isValidMessageId(invalidId)).toBe(false);
        });

        it('should reject ID with wrong length', () => {
            const invalidId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5';
            expect(isValidMessageId(invalidId)).toBe(false);
        });

        it('should reject ID with wrong format (missing dashes)', () => {
            const invalidId = 'A1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6Q7R8';
            expect(isValidMessageId(invalidId)).toBe(false);
        });

        it('should reject ID with special characters', () => {
            const invalidId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P@';
            expect(isValidMessageId(invalidId)).toBe(false);
        });

        it('should reject null or undefined', () => {
            expect(isValidMessageId(null)).toBe(false);
            expect(isValidMessageId(undefined)).toBe(false);
        });

        it('should reject non-string values', () => {
            expect(isValidMessageId(123)).toBe(false);
            expect(isValidMessageId({})).toBe(false);
            expect(isValidMessageId([])).toBe(false);
        });

        it('should reject empty string', () => {
            expect(isValidMessageId('')).toBe(false);
        });
    });

    describe('formatMessageIdForDisplay', () => {
        it('should return valid ID as-is', () => {
            const validId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
            expect(formatMessageIdForDisplay(validId)).toBe(validId);
        });

        it('should return invalid ID as-is', () => {
            const invalidId = 'invalid-id';
            expect(formatMessageIdForDisplay(invalidId)).toBe(invalidId);
        });
    });

    describe('truncateMessageId', () => {
        it('should truncate long ID with default length', () => {
            const longId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
            expect(truncateMessageId(longId)).toBe('A1B2C3D4...');
        });

        it('should truncate with custom length', () => {
            const longId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
            expect(truncateMessageId(longId, 12)).toBe('A1B2C3D4-E5F...');
        });

        it('should return short ID as-is', () => {
            const shortId = 'ABC123';
            expect(truncateMessageId(shortId)).toBe(shortId);
        });

        it('should handle null/undefined gracefully', () => {
            expect(truncateMessageId(null)).toBe('');
            expect(truncateMessageId(undefined)).toBe('');
        });

        it('should handle non-string values', () => {
            expect(truncateMessageId(123)).toBe('');
        });
    });

    describe('validateMessageIdWithError', () => {
        it('should return valid result for correct ID', () => {
            const validId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
            const result = validateMessageIdWithError(validId);
            expect(result.isValid).toBe(true);
            expect(result.errorMessage).toBe(null);
        });

        it('should return error for empty ID', () => {
            const result = validateMessageIdWithError('');
            expect(result.isValid).toBe(false);
            expect(result.errorMessage).toBe('訊息 ID 不能為空');
        });

        it('should return error for null ID', () => {
            const result = validateMessageIdWithError(null);
            expect(result.isValid).toBe(false);
            expect(result.errorMessage).toBe('訊息 ID 不能為空');
        });

        it('should return error for non-string ID', () => {
            const result = validateMessageIdWithError(123);
            expect(result.isValid).toBe(false);
            expect(result.errorMessage).toBe('訊息 ID 必須是字符串格式');
        });

        it('should return error for invalid format', () => {
            const result = validateMessageIdWithError('invalid-id');
            expect(result.isValid).toBe(false);
            expect(result.errorMessage).toBe('訊息 ID 格式不正確，應為 36 位大寫字母和數字組成');
        });
    });

    describe('isLegacyNumericId', () => {
        it('should identify numeric IDs as legacy', () => {
            expect(isLegacyNumericId('123')).toBe(true);
            expect(isLegacyNumericId('456789')).toBe(true);
        });

        it('should not identify new format as legacy', () => {
            const newId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
            expect(isLegacyNumericId(newId)).toBe(false);
        });

        it('should not identify mixed alphanumeric as legacy', () => {
            expect(isLegacyNumericId('abc123')).toBe(false);
        });

        it('should handle null/undefined gracefully', () => {
            expect(isLegacyNumericId(null)).toBe(false);
            expect(isLegacyNumericId(undefined)).toBe(false);
        });

        it('should handle non-string values', () => {
            expect(isLegacyNumericId(123)).toBe(false);
        });
    });
});