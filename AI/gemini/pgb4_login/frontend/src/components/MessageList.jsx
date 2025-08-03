import React from 'react';

const MessageList = ({ 
  messages, 
  page, 
  totalPages, 
  onPageChange, 
  showActions = false, 
  currentUserId, 
  onEdit, 
  onDelete,
  title = "All Messages" 
}) => {
  return (
    <div>
      <h2>{title}</h2>
      {messages.length === 0 ? (
        <p>No messages yet.</p>
      ) : (
        <ul className="message-list">
          {messages.map((message) => (
            <li key={message.id} className="message-item">
              <div className="message-content">
                <strong>{message.userId}:</strong> {message.content}
                <p><em>Created at: {new Date(message.createdAt).toLocaleString()}</em></p>
              </div>
              {showActions && currentUserId === message.userId && (
                <div className="message-actions">
                  <button onClick={() => onEdit(message)} className="edit-btn">
                    編輯
                  </button>
                  <button onClick={() => onDelete(message.id)} className="delete-btn">
                    刪除
                  </button>
                </div>
              )}
            </li>
          ))}
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
