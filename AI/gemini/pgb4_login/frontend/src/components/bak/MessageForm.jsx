import React, { useState } from 'react';
import { createMessage, getTaskStatus } from '../api';
import { useUser } from '../contexts/UserContext';

const MessageForm = ({ onMessageCreated }) => {
  const { currentUser, isLoggedIn } = useUser();
  const [content, setContent] = useState('');
  const [status, setStatus] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!isLoggedIn || !currentUser) {
      setStatus('請先登入才能發布訊息');
      return;
    }
    
    setStatus('Sending message...');
    try {
      const returnedTaskId = await createMessage(currentUser.username, content);
      setStatus(`Message sent. Task ID: ${returnedTaskId}. Checking status...`);
      checkTaskStatus(returnedTaskId);
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

  if (!isLoggedIn) {
    return (
      <div>
        <h2>Create New Message</h2>
        <p>請先登入才能發布訊息</p>
      </div>
    );
  }

  return (
    <div>
      <h2>Create New Message</h2>
      <form onSubmit={handleSubmit}>
        <div>
          <label>Content:</label>
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            required
            placeholder="分享你的想法..."
          ></textarea>
        </div>
        <button type="submit">Post Message</button>
      </form>
      {status && <p>{status}</p>}
    </div>
  );
};

export default MessageForm;
