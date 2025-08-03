import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { usePageTitle } from '../contexts/PageContext';
import { getMessagesByUserId } from '../api';
import MessageList from './MessageList';

const UserMessages = () => {
    const { userId, pageNumber } = useParams();
    const navigate = useNavigate();
    const { setPageTitle } = usePageTitle();
    const [messages, setMessages] = useState([]);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    
    // 從 URL 參數獲取頁面號碼，預設為 1（顯示），但 API 使用 0-based
    const parsedPageNumber = pageNumber ? parseInt(pageNumber) : 1;
    const currentPage = Math.max(0, parsedPageNumber - 1); // 確保不會是負數

    useEffect(() => {
        if (userId) {
            setPageTitle(`${userId} 的訊息`);
        }
    }, [userId, setPageTitle]);

    const fetchUserMessages = async (page) => {
        setLoading(true);
        setError(null);
        try {
            const data = await getMessagesByUserId(userId, page);
            setMessages(data.content || []);
            setTotalPages(data.totalPages || 0);
        } catch (err) {
            setError(err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (userId) {
            fetchUserMessages(currentPage);
        }
    }, [currentPage, userId]);

    // 當總頁數載入後，檢查當前頁面是否有效
    useEffect(() => {
        if (totalPages > 0 && currentPage >= totalPages) {
            // 如果當前頁面超出範圍，重定向到最後一頁
            const lastPage = totalPages;
            if (lastPage === 1) {
                navigate(`/user/${userId}/messages`);
            } else {
                navigate(`/user/${userId}/messages/page/${lastPage}`);
            }
        }
    }, [totalPages, currentPage, navigate, userId]);

    const handlePageChange = (newPage) => {
        if (newPage >= 0 && newPage < totalPages) {
            // 更新 URL，頁面號碼從 1 開始顯示
            const displayPage = newPage + 1;
            if (displayPage === 1) {
                navigate(`/user/${userId}/messages`);
            } else {
                navigate(`/user/${userId}/messages/page/${displayPage}`);
            }
        }
    };

    if (loading) {
        return <div>載入中...</div>;
    }

    if (error) {
        return <div>錯誤: {error.message}</div>;
    }

    return (
        <div className="user-messages">
            
            <MessageList
                messages={messages}
                page={currentPage}
                totalPages={totalPages}
                onPageChange={handlePageChange}
                title={`${userId} 的所有訊息`}
                showUserLinks={true}
            />
        </div>
    );
};

export default UserMessages;