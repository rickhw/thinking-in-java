import React, { useState, useEffect, useCallback } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { getMessageById } from '../api';
import { useUser } from '../contexts/UserContext';
import { usePageTitle } from '../contexts/PageContext';
import { 
    generateMessageTitle, 
    generateMessageDescription,
    PAGE_TITLES,
    PAGE_DESCRIPTIONS
} from '../utils/seo';

const SingleMessage = () => {
    const { messageId } = useParams();
    const navigate = useNavigate();
    const { currentUser, isLoggedIn } = useUser();
    const { setPageMeta, resetPageMeta } = usePageTitle();
    const [message, setMessage] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [retryCount, setRetryCount] = useState(0);
    const [isRetrying, setIsRetrying] = useState(false);
    const [permissionError, setPermissionError] = useState(null);
    const [actionLoading, setActionLoading] = useState(false);
    const [actionError, setActionError] = useState(null);

    // Enhanced function to fetch message data with better error handling
    const fetchMessage = useCallback(async (isRetryAttempt = false) => {
        if (!messageId) {
            setError({ 
                type: 'invalid_id', 
                message: 'Message ID is required',
                canRetry: false,
                userGuidance: '請確認 URL 中包含有效的訊息 ID。'
            });
            setLoading(false);
            return;
        }

        if (isRetryAttempt) {
            setIsRetrying(true);
        } else {
            setLoading(true);
        }
        
        setError(null);
        setPermissionError(null);
        
        try {
            const messageData = await getMessageById(messageId);
            setMessage(messageData);
            setRetryCount(0); // Reset retry count on success
        } catch (err) {
            console.error('Error fetching message:', err);
            
            // Enhanced error type determination
            const errorMessage = err.message.toLowerCase();
            const statusCode = err.status || err.response?.status;
            
            let errorType, userMessage, canRetry = true, userGuidance;
            
            if (statusCode === 404 || errorMessage.includes('404') || errorMessage.includes('not found')) {
                errorType = 'not_found';
                userMessage = '訊息不存在或已被刪除';
                canRetry = false;
                userGuidance = '請檢查 URL 是否正確，或嘗試返回首頁查看其他訊息。';
            } else if (statusCode === 403 || errorMessage.includes('403') || errorMessage.includes('forbidden')) {
                errorType = 'permission';
                userMessage = '您沒有權限查看此訊息';
                canRetry = false;
                userGuidance = '此訊息可能為私人訊息或您的權限不足。請聯繫管理員或嘗試登入其他帳戶。';
            } else if (statusCode === 401 || errorMessage.includes('401') || errorMessage.includes('unauthorized')) {
                errorType = 'auth';
                userMessage = '需要登入才能查看此訊息';
                canRetry = false;
                userGuidance = '請先登入您的帳戶，然後再嘗試查看此訊息。';
            } else if (errorMessage.includes('network') || errorMessage.includes('fetch') || errorMessage.includes('timeout')) {
                errorType = 'network';
                userMessage = '網路連線錯誤';
                userGuidance = '請檢查您的網路連線，然後點擊重試按鈕。';
            } else if (statusCode >= 500) {
                errorType = 'server';
                userMessage = '伺服器錯誤';
                userGuidance = '伺服器暫時無法處理請求，請稍後再試。';
            } else {
                errorType = 'general';
                userMessage = err.message || '載入訊息時發生未知錯誤';
                userGuidance = '請嘗試重新載入頁面，如果問題持續發生，請聯繫技術支援。';
            }
            
            setError({ 
                type: errorType, 
                message: userMessage,
                originalError: err.message,
                canRetry,
                userGuidance,
                statusCode
            });
            
            // Increment retry count for network/server errors
            if (canRetry) {
                setRetryCount(prev => prev + 1);
            }
        } finally {
            setLoading(false);
            setIsRetrying(false);
        }
    }, [messageId]);

    // Enhanced retry handler with exponential backoff for network errors
    const handleRetry = useCallback(() => {
        if (error?.type === 'network' && retryCount >= 3) {
            setError(prev => ({
                ...prev,
                message: '多次重試失敗，請檢查網路連線',
                userGuidance: '已嘗試多次重新載入，請檢查您的網路連線狀態，或稍後再試。'
            }));
            return;
        }
        
        // Add delay for network errors (exponential backoff)
        const delay = error?.type === 'network' ? Math.min(1000 * Math.pow(2, retryCount), 5000) : 0;
        
        if (delay > 0) {
            setTimeout(() => {
                fetchMessage(true);
            }, delay);
        } else {
            fetchMessage(true);
        }
    }, [error, retryCount, fetchMessage]);

    // Handler for permission errors - show helpful guidance
    const handlePermissionError = useCallback((action) => {
        if (!isLoggedIn) {
            setPermissionError({
                type: 'not_logged_in',
                message: '請先登入才能執行此操作',
                action: action,
                guidance: '您需要登入帳戶才能編輯或刪除訊息。',
                actionButton: {
                    text: '前往登入',
                    onClick: () => navigate('/login')
                }
            });
        } else if (currentUser?.id !== message?.userId) {
            setPermissionError({
                type: 'not_owner',
                message: '您只能編輯或刪除自己的訊息',
                action: action,
                guidance: '此訊息屬於其他使用者，您沒有權限進行修改。',
                actionButton: {
                    text: '返回首頁',
                    onClick: () => navigate('/')
                }
            });
        }
    }, [isLoggedIn, currentUser, message, navigate]);

    // Clear permission error
    const clearPermissionError = () => {
        setPermissionError(null);
    };

    // Clear action error
    const clearActionError = () => {
        setActionError(null);
    };

    useEffect(() => {
        fetchMessage();
    }, [fetchMessage]);

    // Update page title based on loading state
    useEffect(() => {
        if (loading) {
            setPageMeta({
                title: PAGE_TITLES.LOADING,
                description: PAGE_DESCRIPTIONS.LOADING,
                author: '',
                publishedTime: ''
            });
        }
    }, [loading, setPageMeta]);

    // Update page title based on error state
    useEffect(() => {
        if (error) {
            let title, description;
            
            switch (error.type) {
                case 'not_found':
                    title = PAGE_TITLES.NOT_FOUND;
                    description = PAGE_DESCRIPTIONS.NOT_FOUND;
                    break;
                case 'network':
                    title = PAGE_TITLES.NETWORK_ERROR;
                    description = PAGE_DESCRIPTIONS.NETWORK_ERROR;
                    break;
                default:
                    title = PAGE_TITLES.GENERAL_ERROR;
                    description = PAGE_DESCRIPTIONS.GENERAL_ERROR;
                    break;
            }
            
            setPageMeta({
                title,
                description,
                author: '',
                publishedTime: ''
            });
        }
    }, [error, setPageMeta]);

    // Update page title based on message content
    useEffect(() => {
        if (message && !loading && !error) {
            const title = generateMessageTitle(message);
            const description = generateMessageDescription(message);

            setPageMeta({
                title,
                description,
                author: message.userId,
                publishedTime: message.createdAt
            });
        }
    }, [message, loading, error, setPageMeta]);

    // Reset page meta when component unmounts
    useEffect(() => {
        return () => {
            resetPageMeta();
        };
    }, [resetPageMeta]);

    // Enhanced loading skeleton component with retry state
    const LoadingSkeleton = () => (
        <div className="single-message">
            <div className="loading-skeleton">
                {isRetrying && (
                    <div className="retry-indicator">
                        <div className="retry-spinner"></div>
                        <span>重新載入中...</span>
                    </div>
                )}
                <div className="skeleton-header">
                    <div className="skeleton-line skeleton-title"></div>
                    <div className="skeleton-line skeleton-subtitle"></div>
                </div>
                <div className="skeleton-content">
                    <div className="skeleton-line skeleton-author"></div>
                    <div className="skeleton-line skeleton-time"></div>
                    <div className="skeleton-line skeleton-text"></div>
                    <div className="skeleton-line skeleton-text"></div>
                    <div className="skeleton-line skeleton-text short"></div>
                </div>
            </div>
        </div>
    );

    // Permission Error Component
    const PermissionError = () => (
        <div className="permission-error-overlay">
            <div className="permission-error-content">
                <div className="permission-error-icon">🔒</div>
                <h3>{permissionError.message}</h3>
                <p>{permissionError.guidance}</p>
                <div className="permission-error-actions">
                    {permissionError.actionButton && (
                        <button 
                            onClick={permissionError.actionButton.onClick}
                            className="permission-action-button primary"
                        >
                            {permissionError.actionButton.text}
                        </button>
                    )}
                    <button 
                        onClick={clearPermissionError}
                        className="permission-action-button secondary"
                    >
                        關閉
                    </button>
                </div>
            </div>
        </div>
    );

    // Action Error Component
    const ActionError = () => (
        <div className="action-error">
            <div className="action-error-content">
                <div className="action-error-icon">⚠️</div>
                <span className="action-error-message">{actionError.message}</span>
                <span className="action-error-guidance">{actionError.guidance}</span>
                <button 
                    onClick={clearActionError}
                    className="action-error-close"
                    aria-label="關閉錯誤訊息"
                >
                    ✕
                </button>
            </div>
        </div>
    );

    // Enhanced 404 Error component
    const NotFoundError = () => (
        <div className="single-message error-page">
            <div className="error-content not-found">
                <div className="error-icon">📄</div>
                <h2>訊息不存在</h2>
                <p>{error.message}</p>
                <div className="error-guidance-text">
                    <p>{error.userGuidance}</p>
                </div>
                <div className="error-actions">
                    <Link to="/" className="back-button primary">
                        返回首頁
                    </Link>
                    <Link to="/messages" className="back-button secondary">
                        查看我的訊息
                    </Link>
                </div>
            </div>
        </div>
    );

    // Enhanced Network Error component
    const NetworkError = () => (
        <div className="single-message error-page">
            <div className="error-content network-error">
                <div className="error-icon">🌐</div>
                <h2>網路連線錯誤</h2>
                <p>{error.message}</p>
                <div className="error-guidance-text">
                    <p>{error.userGuidance}</p>
                    {retryCount > 0 && (
                        <p className="retry-info">已重試 {retryCount} 次</p>
                    )}
                </div>
                <div className="error-actions">
                    <button 
                        onClick={handleRetry} 
                        className="back-button primary"
                        disabled={isRetrying || retryCount >= 3}
                    >
                        {isRetrying ? '重試中...' : retryCount >= 3 ? '已達重試上限' : '重新載入'}
                    </button>
                    <Link to="/" className="back-button secondary">
                        返回首頁
                    </Link>
                </div>
                {retryCount >= 3 && (
                    <div className="max-retry-guidance">
                        <h4>建議解決方案：</h4>
                        <ul>
                            <li>檢查網路連線狀態</li>
                            <li>嘗試重新整理頁面 (Ctrl+F5)</li>
                            <li>清除瀏覽器快取</li>
                            <li>稍後再試</li>
                        </ul>
                    </div>
                )}
            </div>
        </div>
    );

    // Enhanced General Error component with specific error types
    const GeneralError = () => {
        let icon, title;
        
        switch (error.type) {
            case 'permission':
                icon = '🔒';
                title = '權限不足';
                break;
            case 'auth':
                icon = '🔐';
                title = '需要登入';
                break;
            case 'server':
                icon = '🔧';
                title = '伺服器錯誤';
                break;
            default:
                icon = '⚠️';
                title = '載入錯誤';
                break;
        }
        
        return (
            <div className="single-message error-page">
                <div className="error-content general-error">
                    <div className="error-icon">{icon}</div>
                    <h2>{title}</h2>
                    <p>{error.message}</p>
                    <div className="error-guidance-text">
                        <p>{error.userGuidance}</p>
                        {error.statusCode && (
                            <p className="error-code">錯誤代碼: {error.statusCode}</p>
                        )}
                    </div>
                    <div className="error-actions">
                        {error.canRetry && (
                            <button 
                                onClick={handleRetry} 
                                className="back-button primary"
                                disabled={isRetrying}
                            >
                                {isRetrying ? '重試中...' : '重新載入'}
                            </button>
                        )}
                        {error.type === 'auth' && (
                            <Link to="/login" className="back-button primary">
                                前往登入
                            </Link>
                        )}
                        <Link to="/" className="back-button secondary">
                            返回首頁
                        </Link>
                    </div>
                </div>
            </div>
        );
    };

    // Format date for display
    const formatDate = (dateString) => {
        const date = new Date(dateString);
        return date.toLocaleString('zh-TW', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
    };

    // Render loading state
    if (loading) {
        return <LoadingSkeleton />;
    }

    // Render error states
    if (error) {
        switch (error.type) {
            case 'not_found':
                return <NotFoundError />;
            case 'network':
                return <NetworkError />;
            default:
                return <GeneralError />;
        }
    }

    // This shouldn't happen if API works correctly, but just in case
    if (!message) {
        return <NotFoundError />;
    }

    // Check if current user is the author of the message
    const isAuthor = isLoggedIn && currentUser && currentUser.id === message.userId;

    // Enhanced handler functions for edit and delete operations with permission checks
    const handleEdit = () => {
        // Clear any existing errors
        clearPermissionError();
        clearActionError();
        
        // Check permissions
        if (!isLoggedIn || currentUser?.id !== message?.userId) {
            handlePermissionError('edit');
            return;
        }
        
        // TODO: Implement edit functionality in future tasks
        console.log('Edit button clicked');
        setActionError({
            type: 'not_implemented',
            message: '編輯功能尚未實作',
            guidance: '此功能將在後續任務中實作。'
        });
    };

    const handleDelete = () => {
        // Clear any existing errors
        clearPermissionError();
        clearActionError();
        
        // Check permissions
        if (!isLoggedIn || currentUser?.id !== message?.userId) {
            handlePermissionError('delete');
            return;
        }
        
        // TODO: Implement delete functionality in future tasks
        console.log('Delete button clicked');
        setActionError({
            type: 'not_implemented',
            message: '刪除功能尚未實作',
            guidance: '此功能將在後續任務中實作。'
        });
    };

    return (
        <div className="single-message">
            {/* Permission Error Overlay */}
            {permissionError && <PermissionError />}
            
            {/* Navigation breadcrumb */}
            <div className="breadcrumb">
                <Link to="/" className="breadcrumb-link">← 返回所有訊息列表</Link>
            </div>

            {/* Action Error Banner */}
            {actionError && <ActionError />}

            {/* Message content */}
            <div className="message-detail">
                <div className="message-header">
                    <div className="author-info">
                        <span className="author-label">作者：</span>
                        <Link to={`/user/${message.userId}/messages`} className="author-link">
                            {message.userId}
                        </Link>
                    </div>
                    <div className="time-info">
                        <div className="created-time">
                            <span className="time-label">發布時間：</span>
                            <span className="time-value">{formatDate(message.createdAt)}</span>
                        </div>
                        {message.updatedAt && message.updatedAt !== message.createdAt && (
                            <div className="updated-time">
                                <span className="time-label">更新時間：</span>
                                <span className="time-value">{formatDate(message.updatedAt)}</span>
                            </div>
                        )}
                    </div>
                </div>

                <div className="message-content">
                    <div className="message-text">
                        {message.content}
                    </div>
                </div>
                
                {/* Operation buttons - only show for message author */}
                {isAuthor && (
                    <div className="message-actions">
                        <button 
                            className="edit-button"
                            onClick={handleEdit}
                            type="button"
                            disabled={actionLoading}
                            aria-label="編輯此訊息"
                        >
                            {actionLoading ? '處理中...' : '編輯'}
                        </button>
                        <button 
                            className="delete-button"
                            onClick={handleDelete}
                            type="button"
                            disabled={actionLoading}
                            aria-label="刪除此訊息"
                        >
                            {actionLoading ? '處理中...' : '刪除'}
                        </button>
                    </div>
                )}

                {/* Status feedback for actions */}
                {actionLoading && (
                    <div className="action-status">
                        <div className="action-loading">
                            <div className="action-spinner"></div>
                            <span>正在處理您的請求...</span>
                        </div>
                    </div>
                )}

                {/* Navigation links */}
                <div className="navigation-links">
                    <Link to="/" className="nav-link">
                        📋 返回所有訊息
                    </Link>
                    <Link to={`/user/${message.userId}/messages`} className="nav-link">
                        👤 查看 {message.userId} 的所有訊息
                    </Link>
                </div>
            </div>
        </div>
    );
};

export default SingleMessage;