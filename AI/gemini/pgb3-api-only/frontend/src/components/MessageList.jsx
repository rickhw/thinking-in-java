import React from 'react';

const MessageList = ({ messages, page, totalPages, onPageChange }) => {
  return (
    <div>
      <h2>All Messages</h2>
      {messages.length === 0 ? (
        <p>No messages yet.</p>
      ) : (
        <ul>
          {messages.map((message) => (
            <li key={message.id}>
              <strong>{message.userId}:</strong> {message.content}
              <p><em>Created at: {new Date(message.createdAt).toLocaleString()}</em></p>
            </li>
          ))}
        </ul>
      )}
      <div className="pagination-controls">
        <button onClick={() => onPageChange(page - 1)} disabled={page === 0}>
          Previous
        </button>
        <span> Page {page + 1} of {totalPages} </span>
        <button onClick={() => onPageChange(page + 1)} disabled={page === totalPages - 1}>
          Next
        </button>
      </div>
    </div>
  );
};

export default MessageList;
