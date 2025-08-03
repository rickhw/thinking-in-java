import React, { useEffect, useState } from 'react';
import { getAllMessages } from '../api';

const MessageList = () => {
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchMessages = async () => {
      try {
        const data = await getAllMessages();
        setMessages(data);
      } catch (err) {
        setError(err);
      } finally {
        setLoading(false);
      }
    };

    fetchMessages();
  }, []);

  if (loading) return <div>Loading messages...</div>;
  if (error) return <div>Error: {error.message}</div>;

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
    </div>
  );
};

export default MessageList;
