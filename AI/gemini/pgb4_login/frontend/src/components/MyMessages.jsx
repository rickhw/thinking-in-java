import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useUser } from '../contexts/UserContext';
import { getMessagesByUserId, deleteMessage, updateMessage, getTaskStatus } from '../api';
import MessageList from './MessageList';

const MyMessages = () => {
    const { pageNumber } = useParams();
    const navigate = useNavigate();
    const { currentUser, isLoggedIn } = useUser();
    const [messages, setMessages] = useState([]);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [editingMessage, setEditingMessage] = useState(null);
    const [editContent, setEditContent] = useState('');
    
    // 從 URL 參數獲取頁面號碼，預設為 1（顯示），但 API 使用 0-based
    const parsedPageNumber = pageNumber ? parseInt(pageNumber) : 1;
    const currentPage = Math.max(0, parsedPageNumber - 1); // 確保不會是負數

    const fetchMyMessages = async (page) => {
        if (!isLoggedIn || !currentUser) return;
        
        setLoading(true);
        setError(null);
        try {
            const data = await getMessagesByUserId(currentUser.username, page);
            setMessages(data.content || []);
            setTotalPages(data.totalPages || 0);
        } catch (err) {
            setError(err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (isLoggedIn && currentUser) {
            fetchMyMessages(currentPage);
        }
    }, [currentPage, isLoggedIn, currentUser]);

    // 當總頁數載入後，檢查當前頁面是否有效
    useEffect(() => {
        if (totalPages > 0 && currentPage >= totalPages) {
            // 如果當前頁面超出範圍，重定向到最後一頁
            const lastPage = totalPages;
            if (lastPage === 1) {
                navigate('/my-messages');
            } else {
                navigate(`/my-messages/page/${lastPage}`);
            }
        }
    }, [totalPages, currentPage, navigate]);

    const handlePageChange = (newPage) => {
        if (newPage >= 0 && newPage < totalPages) {
            // 更新 URL，頁面號碼從 1 開始顯示
            const displayPage = newPage + 1;
            if (displayPage === 1) {
                navigate('/my-messages');
            } else {
                navigate(`/my-messages/page/${displayPage}`);
            }
        }
    };

    const handleEdit = (message) => {
        setEditingMessage(message);
        setEditContent(message.content);
    };

    const handleSaveEdit = async () => {
        if (!editContent.trim()) return;
        
        try {
            const taskId = await updateMessage(editingMessage.id, editContent);
            // 可以選擇性地檢查任務狀態
            setEditingMessage(null);
            setEditContent('');
            // 重新載入訊息
            setTimeout(() => fetchMyMessages(currentPage), 1000);
        } catch (err) {
            setError(err);
        }
    };

    const handleCancelEdit = () => {
        setEditingMessage(null);
        setEditContent('');
    };

    const handleDelete = async (messageId) => {
        if (window.confirm('確定要刪除這則訊息嗎？')) {
            try {
                const taskId = await deleteMessage(messageId);
                // 重新載入訊息
                setTimeout(() => fetchMyMessages(currentPage), 1000);
            } catch (err) {
                setError(err);
            }
        }
    };

    if (!isLoggedIn) {
        return <div>請先登入以查看您的訊息</div>;
    }

    if (loading) {
        return <div>載入中...</div>;
    }

    if (error) {
        return <div>錯誤: {error.message}</div>;
    }

    return (
        <div className="my-messages">
            <h2>我的訊息</h2>
            {editingMessage && (
                <div className="edit-modal">
                    <h3>編輯訊息</h3>
                    <textarea
                        value={editContent}
                        onChange={(e) => setEditContent(e.target.value)}
                        rows="4"
                        cols="50"
                    />
                    <div>
                        <button onClick={handleSaveEdit}>儲存</button>
                        <button onClick={handleCancelEdit}>取消</button>
                    </div>
                </div>
            )}
            <MessageList
                messages={messages}
                page={currentPage}
                totalPages={totalPages}
                onPageChange={handlePageChange}
                showActions={true}
                currentUserId={currentUser?.username}
                onEdit={handleEdit}
                onDelete={handleDelete}
            />
        </div>
    );
};

export default MyMessages;