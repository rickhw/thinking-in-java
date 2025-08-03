import React, { useState } from 'react';
import { createMessage, getTaskStatus } from '../api';

const MessageForm = ({ onMessageCreated }) => {
  const [userId, setUserId] = useState('');
  const [content, setContent] = useState('');
  const [status, setStatus] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setStatus('Sending message...');
    try {
      const returnedTaskId = await createMessage(userId, content);
      setStatus(`Message sent. Task ID: ${returnedTaskId}. Checking status...`);
      checkTaskStatus(returnedTaskId);
      setUserId('');
      setContent('');
    } catch (error) {
      setStatus(`Error sending message: ${error.message}`);
    }
  };

  const checkTaskStatus = async (id) => {
    let currentStatus = '';
    const interval = setInterval(async () => {
      try {
        const task = await getTaskStatus(id);
        currentStatus = task.status;
        setStatus(`Task ID: ${id}, Status: ${currentStatus}`);
        if (currentStatus === 'COMPLETED' || currentStatus === 'FAILED') {
          clearInterval(interval);
          if (currentStatus === 'COMPLETED') {
            setStatus(`Task ID: ${id}, Status: ${currentStatus}. Message created successfully!`);
            if (onMessageCreated) {
              onMessageCreated();
            }
          } else {
            setStatus(`Task ID: ${id}, Status: ${currentStatus}. Error: ${task.error}`);
          }
        }
      } catch (error) {
        clearInterval(interval);
        setStatus(`Error checking task status: ${error.message}`);
      }
    }, 2000); // Poll every 2 seconds
  };

  return (
    <div>
      <h2>Create New Message</h2>
      <form onSubmit={handleSubmit}>
        <div>
          <label>User ID:</label>
          <input
            type="text"
            value={userId}
            onChange={(e) => setUserId(e.target.value)}
            required
          />
        </div>
        <div>
          <label>Content:</label>
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            required
          ></textarea>
        </div>
        <button type="submit">Post Message</button>
      </form>
      {status && <p>{status}</p>}
    </div>
  );
};

export default MessageForm;
