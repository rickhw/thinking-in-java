import { describe, it, expect, vi, beforeEach } from 'vitest';
import { isValidMessageId, validateMessageIdWithError, isLegacyNumericId } from '../utils/messageId';

describe('SingleMessage Component Integration', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('should validate message IDs correctly', () => {
        const validId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
        const invalidId = 'invalid-id';
        const numericId = '123';
        
        expect(isValidMessageId(validId)).toBe(true);
        expect(isValidMessageId(invalidId)).toBe(false);
        expect(isValidMessageId(numericId)).toBe(false);
    });

    it('should provide detailed validation errors', () => {
        const validId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
        const invalidId = 'invalid-id';
        const numericId = '123';
        
        const validResult = validateMessageIdWithError(validId);
        expect(validResult.isValid).toBe(true);
        expect(validResult.errorMessage).toBe(null);
        
        const invalidResult = validateMessageIdWithError(invalidId);
        expect(invalidResult.isValid).toBe(false);
        expect(invalidResult.errorMessage).toBe('訊息 ID 格式不正確，應為 36 位大寫字母和數字組成');
        
        const numericResult = validateMessageIdWithError(numericId);
        expect(numericResult.isValid).toBe(false);
        expect(numericResult.errorMessage).toBe('舊的數字 ID 格式');
    });

    it('should identify legacy numeric IDs', () => {
        expect(isLegacyNumericId('123')).toBe(true);
        expect(isLegacyNumericId('456789')).toBe(true);
        expect(isLegacyNumericId('A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6')).toBe(false);
        expect(isLegacyNumericId('abc123')).toBe(false);
    });

    it('should handle URL parameter validation scenarios', () => {
        const testCases = [
            {
                messageId: 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6',
                expectedValid: true,
                expectedErrorType: null
            },
            {
                messageId: 'invalid-id',
                expectedValid: false,
                expectedErrorType: 'invalid_format'
            },
            {
                messageId: '123',
                expectedValid: false,
                expectedErrorType: 'legacy_numeric'
            },
            {
                messageId: '',
                expectedValid: false,
                expectedErrorType: 'empty'
            },
            {
                messageId: null,
                expectedValid: false,
                expectedErrorType: 'empty'
            }
        ];

        testCases.forEach(testCase => {
            const validation = validateMessageIdWithError(testCase.messageId);
            expect(validation.isValid).toBe(testCase.expectedValid);
            
            if (!testCase.expectedValid) {
                expect(validation.errorMessage).toBeTruthy();
                
                if (testCase.expectedErrorType === 'legacy_numeric') {
                    expect(isLegacyNumericId(testCase.messageId)).toBe(true);
                }
            }
        });
    });

    it('should handle component error states correctly', () => {
        // Test error handling logic that would be used in the component
        const handleMessageIdError = (messageId) => {
            const validation = validateMessageIdWithError(messageId);
            
            if (!validation.isValid) {
                let userGuidance = validation.errorMessage;
                
                if (isLegacyNumericId(messageId)) {
                    userGuidance = '此訊息使用舊的數字 ID 格式，系統已升級為新的 36 位字符 ID 格式。請聯繫管理員或查看最新的訊息列表。';
                }
                
                return {
                    type: 'invalid_id_format',
                    message: '訊息 ID 格式不正確',
                    canRetry: false,
                    userGuidance: userGuidance
                };
            }
            
            return null;
        };

        // Test valid ID
        expect(handleMessageIdError('A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6')).toBe(null);
        
        // Test invalid ID
        const invalidError = handleMessageIdError('invalid-id');
        expect(invalidError).toBeTruthy();
        expect(invalidError.type).toBe('invalid_id_format');
        expect(invalidError.canRetry).toBe(false);
        
        // Test legacy numeric ID
        const legacyError = handleMessageIdError('123');
        expect(legacyError).toBeTruthy();
        expect(legacyError.userGuidance).toContain('舊的數字 ID 格式');
    });

    it('should generate correct API call parameters', () => {
        const validId = 'A1B2C3D4-E5F6-G7H8-I9J0-K1L2M3N4O5P6';
        
        // Simulate the logic that would be used to call the API
        const shouldCallApi = isValidMessageId(validId);
        expect(shouldCallApi).toBe(true);
        
        // If we were to call the API, it would be with this ID
        const apiCallId = shouldCallApi ? validId : null;
        expect(apiCallId).toBe(validId);
    });
});