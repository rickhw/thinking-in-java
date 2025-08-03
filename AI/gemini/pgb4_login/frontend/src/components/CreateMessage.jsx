import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { createMessage, getTaskStatus } from '../api';
import { useUser } from '../contexts/UserContext';
import { usePageTitle } from '../contexts/PageContext';

const CreateMessage = () => {
  const { currentUser, isLoggedIn } = useUser();
  const { setPageTitle } = usePageTitle();
  const navigate = useNavigate();
  const [content, setContent] = useState('');
  const [status, setStatus] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setPageTitle('發布新訊息');
  }, [setPageTitle]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!isLoggedIn || !currentUser) {
      setStatus('請先登入才能發布訊息');
      return;
    }
    
    setLoading(true);
    setStatus('發布中...');
    try {
      const returnedTaskId = await createMessage(currentUser.username, content);
      setStatus(`訊息已提交處理，任務 ID: ${returnedTaskId}`);
      checkTaskStatus(returnedTaskId);
    } catch (error) {
      setStatus(`發布失敗: ${error.message}`);
      setLoading(false);
    }
  };

  const checkTaskStatus = async (id) => {
    let currentStatus = '';
    const interval = setInterval(async () => {
      try {
        const task = await getTaskStatus(id);
        currentStatus = task.status;
        if (currentStatus === 'COMPLETED' || currentStatus === 'FAILED') {
          clearInterval(interval);
          setLoading(false);
          if (currentStatus === 'COMPLETED') {
            setStatus('訊息發布成功！正在跳轉...');
            setTimeout(() => {
              navigate('/');
            }, 1500);
          } else {
            setStatus(`發布失敗: ${task.error}`);
          }
        }
      } catch (error) {
        clearInterval(interval);
        setLoading(false);
        setStatus(`檢查狀態時發生錯誤: ${error.message}`);
      }
    }, 2000);
  };

  if (!isLoggedIn) {
    return (
      <div className="create-message-container">
        <p>請先登入才能發布訊息</p>
        <button onClick={() => navigate('/login')} className="login-redirect-btn">
          前往登入
        </button>
      </div>
    );
  }

  return (
    <div className="create-message-container">
      
      <form onSubmit={handleSubmit} className="create-message-form">
        <div className="form-group">
          <label htmlFor="content">訊息內容:</label>
          <textarea
            id="content"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            required
            placeholder="分享你的想法..."
            rows="6"
            disabled={loading}
          />
        </div>
        
        <div className="form-actions">
          <button type="submit" disabled={loading || !content.trim()}>
            {loading ? '發布中...' : '發布訊息'}
          </button>
          <button type="button" onClick={() => navigate(-1)} disabled={loading}>
            取消
          </button>
        </div>
      </form>
      
      {status && (
        <div className={`status-message ${status.includes('成功') ? 'success' : status.includes('失敗') ? 'error' : ''}`}>
          {status}
        </div>
      )}
    </div>
  );
};

export default CreateMessage;