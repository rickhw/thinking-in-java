import React, { useState, useEffect, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
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
    const { currentUser, isLoggedIn } = useUser();
    const { setPageMeta, resetPageMeta } = usePageTitle();
    const [message, setMessage] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Function to fetch message data
    const fetchMessage = useCallback(async () => {
        if (!messageId) {
            setError({ type: 'invalid_id', message: 'Message ID is required' });
            setLoading(false);
            return;
        }

        setLoading(true);
        setError(null);
        
        try {
            const messageData = await getMessageById(messageId);
            setMessage(messageData);
        } catch (err) {
            // Determine error type based on response
            const errorMessage = err.message.toLowerCase();
            if (errorMessage.includes('404') || errorMessage.includes('not found')) {
                setError({ type: 'not_found', message: 'Message not found' });
            } else if (errorMessage.includes('network') || errorMessage.includes('fetch')) {
                setError({ type: 'network', message: 'Network error occurred' });
            } else {
                setError({ type: 'general', message: err.message });
            }
        } finally {
            setLoading(false);
        }
    }, [messageId]);

    // Handler for retry button - reloads the message data
    const handleRetry = () => {
        fetchMessage();
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

    // Loading skeleton component
    const LoadingSkeleton = () => (
        <div className="single-message">
            <div className="loading-skeleton">
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

    // 404 Error component
    const NotFoundError = () => (
        <div className="single-message error-page">
            <div className="error-content not-found">
                <div className="error-icon">📄</div>
                <h2>訊息不存在</h2>
                <p>抱歉，您要查看的訊息可能已被刪除或不存在。</p>
                <div className="error-actions">
                    <Link to="/" className="back-button primary">
                        返回首頁
                    </Link>
                    <button 
                        onClick={handleRetry} 
                        className="back-button secondary"
                    >
                        重新載入
                    </button>
                </div>
            </div>
        </div>
    );

    // Network Error component
    const NetworkError = () => (
        <div className="single-message error-page">
            <div className="error-content network-error">
                <div className="error-icon">🌐</div>
                <h2>網路連線錯誤</h2>
                <p>無法載入訊息內容，請檢查您的網路連線。</p>
                <div className="error-actions">
                    <button 
                        onClick={handleRetry} 
                        className="back-button primary"
                    >
                        重新載入
                    </button>
                    <Link to="/" className="back-button secondary">
                        返回首頁
                    </Link>
                </div>
            </div>
        </div>
    );

    // General Error component
    const GeneralError = () => (
        <div className="single-message error-page">
            <div className="error-content general-error">
                <div className="error-icon">⚠️</div>
                <h2>載入錯誤</h2>
                <p>載入訊息時發生錯誤：{error.message}</p>
                <div className="error-actions">
                    <button 
                        onClick={handleRetry} 
                        className="back-button primary"
                    >
                        重新載入
                    </button>
                    <Link to="/" className="back-button secondary">
                        返回首頁
                    </Link>
                </div>
            </div>
        </div>
    );

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

    // Handler functions for edit and delete operations
    const handleEdit = () => {
        // TODO: Implement edit functionality in future tasks
        console.log('Edit button clicked');
    };

    const handleDelete = () => {
        // TODO: Implement delete functionality in future tasks
        console.log('Delete button clicked');
    };

    return (
        <div className="single-message">
            {/* Navigation breadcrumb */}
            <div className="breadcrumb">
                <Link to="/" className="breadcrumb-link">← 返回所有訊息列表</Link>
            </div>

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
                        >
                            編輯
                        </button>
                        <button 
                            className="delete-button"
                            onClick={handleDelete}
                            type="button"
                        >
                            刪除
                        </button>
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