import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getMessagesByUserId } from '../api';

const UserMessages = () => {
  const { userId } = useParams();
  const [messages, setMessages] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchUserMessages = async (currentPage) => {
    setLoading(true);
    setError(null);
    try {
      const data = await getMessagesByUserId(userId, currentPage);
      setMessages(data.content);
      setTotalPages(data.totalPages);
      setPage(data.number);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUserMessages(page);
  }, [userId, page]);

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < totalPages) {
      setPage(newPage);
    }
  };

  if (loading) return <div>Loading messages for {userId}...</div>;
  if (error) return <div>Error: {error.message}</div>;

  return (
    <div>
      <h2>Messages by {userId}</h2>
      {messages.length === 0 ? (
        <p>No messages found for {userId}.</p>
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
        <button onClick={() => handlePageChange(page - 1)} disabled={page === 0}>
          Previous
        </button>
        <span> Page {page + 1} of {totalPages} </span>
        <button onClick={() => handlePageChange(page + 1)} disabled={page === totalPages - 1}>
          Next
        </button>
      </div>
    </div>
  );
};

export default UserMessages;
