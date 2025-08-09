import React from 'react';
import { Link } from 'react-router-dom';
import { isValidMessageId, truncateMessageId } from '../utils/messageId';
import { generateMessageUrl, generateUserMessagesUrl } from '../utils/navigation';

const MessageList = ({ 
  messages, 
  page, 
  totalPages, 
  onPageChange, 
  showActions = false, 
  currentUserId, 
  onEdit, 
  onDelete,
  title = "All Messages",
  showUserLinks = true
}) => {
  return (
    <div>
      <h2>{title}</h2>
      {messages.length === 0 ? (
        <p>目前沒有訊息。</p>
      ) : (
        <ul className="message-list">
          {messages.map((message) => {
            // Validate message ID format
            const hasValidId = message.id && isValidMessageId(message.id);
            
            return (
              <li key={message.id} className="message-item">
                <div className="message-content">
                  <div className="message-header">
                    {showUserLinks ? (
                      <Link to={generateUserMessagesUrl(message.userId)} className="user-link">
                        <strong>{message.userId}</strong>
                      </Link>
                    ) : (
                      <strong>{message.userId}</strong>
                    )}
                    <span className="message-time">
                      {new Date(message.createdAt).toLocaleString()}
                    </span>
                    {/* Display message ID for debugging/admin purposes */}
                    <span className="message-id-display" title={`Message ID: ${message.id}`}>
                      ID: {truncateMessageId(message.id)}
                    </span>
                  </div>
                  <div className="message-text">
                    {message.content}
                  </div>
                  <div className="message-permalink">
                    {hasValidId ? (
                      <Link to={generateMessageUrl(message.id)} className="permalink-link">
                        查看詳情
                      </Link>
                    ) : (
                      <span className="permalink-link disabled" title="無效的訊息 ID">
                        查看詳情 (不可用)
                      </span>
                    )}
                  </div>
                  {!hasValidId && (
                    <div className="message-warning">
                      ⚠️ 此訊息的 ID 格式不正確
                    </div>
                  )}
                </div>
                {showActions && currentUserId === message.userId && (
                  <div className="message-actions">
                    <button 
                      onClick={() => onEdit(message)} 
                      className="edit-btn"
                      disabled={!hasValidId}
                      title={!hasValidId ? "無法編輯：訊息 ID 格式不正確" : "編輯此訊息"}
                    >
                      編輯
                    </button>
                    <button 
                      onClick={() => hasValidId && onDelete(message.id)} 
                      className="delete-btn"
                      disabled={!hasValidId}
                      title={!hasValidId ? "無法刪除：訊息 ID 格式不正確" : "刪除此訊息"}
                    >
                      刪除
                    </button>
                  </div>
                )}
              </li>
            );
          })}
        </ul>
      )}
      <div className="pagination-controls">
        <button onClick={() => onPageChange(page - 1)} disabled={page === 0 || totalPages === 0}>
          上一頁
        </button>
        <span className="page-info">
          {totalPages > 0 ? `第 ${page + 1} 頁，共 ${totalPages} 頁` : '沒有資料'}
        </span>
        <button onClick={() => onPageChange(page + 1)} disabled={page >= totalPages - 1 || totalPages === 0}>
          下一頁
        </button>
      </div>
    </div>
  );
};

export default MessageList;
