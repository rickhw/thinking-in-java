import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getMessagesByUserId } from '../api';

const UserMessages = () => {
  const { userId } = useParams();
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchUserMessages = async () => {
      try {
        const data = await getMessagesByUserId(userId);
        setMessages(data);
      } catch (err) {
        setError(err);
      } finally {
        setLoading(false);
      }
    };

    fetchUserMessages();
  }, [userId]);

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
    </div>
  );
};

export default UserMessages;
