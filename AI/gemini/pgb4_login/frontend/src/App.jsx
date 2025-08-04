import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, useParams, useNavigate } from 'react-router-dom';
import { UserProvider, useUser } from './contexts/UserContext';
import { PageProvider, usePageTitle } from './contexts/PageContext';
import Navigation from './components/Navigation';
import MessageList from './components/MessageList';
import CreateMessage from './components/CreateMessage';
import Login from './components/Login';
import UserRegister from './components/UserRegister';
import MyMessages from './components/MyMessages';
import MyProfile from './components/MyProfile';
import SingleMessage from './components/SingleMessage';
import UserMessages from './components/bak/UserMessages';
import { getMessages } from './api';
import './App.css';

function App() {
    return (
        <UserProvider>
            <PageProvider>
                <Router>
                    <div className="App">
                        <Navigation />
                        <main className="main-content">
                            <Routes>
                                <Route path="/" element={<HomePage />} />
                                <Route path="/page/:pageNumber" element={<HomePage />} />
                                <Route path="/login" element={<LoginPage />} />
                                <Route path="/register" element={<UserRegister />} />
                                <Route path="/create" element={<CreateMessage />} />
                                <Route path="/messages" element={<MyMessages />} />
                                <Route path="/messages/page/:pageNumber" element={<MyMessages />} />
                                <Route path="/profile" element={<MyProfile />} />
                                <Route path="/message/:messageId" element={<SingleMessage />} />
                                <Route path="/user/:userId/messages" element={<UserMessages />} />
                                <Route path="/user/:userId/messages/page/:pageNumber" element={<UserMessages />} />
                            </Routes>
                        </main>
                    </div>
                </Router>
            </PageProvider>
        </UserProvider>
    );
}

function HomePage() {
    const { pageNumber } = useParams();
    const navigate = useNavigate();
    const [messages, setMessages] = useState([]);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    
    // 從 URL 參數獲取頁面號碼，預設為 1（顯示），但 API 使用 0-based
    const parsedPageNumber = pageNumber ? parseInt(pageNumber) : 1;
    const currentPage = Math.max(0, parsedPageNumber - 1); // 確保不會是負數

    const fetchMessages = async (page) => {
        setLoading(true);
        setError(null);
        try {
            const data = await getMessages(page);
            setMessages(data.content || []);
            setTotalPages(data.totalPages || 0);
        } catch (err) {
            setError(err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchMessages(currentPage);
    }, [currentPage]);

    // 當總頁數載入後，檢查當前頁面是否有效
    useEffect(() => {
        if (totalPages > 0 && currentPage >= totalPages) {
            // 如果當前頁面超出範圍，重定向到最後一頁
            const lastPage = totalPages;
            if (lastPage === 1) {
                navigate('/');
            } else {
                navigate(`/page/${lastPage}`);
            }
        }
    }, [totalPages, currentPage, navigate]);

    const handlePageChange = (newPage) => {
        if (newPage >= 0 && newPage < totalPages) {
            // 更新 URL，頁面號碼從 1 開始顯示
            const displayPage = newPage + 1;
            if (displayPage === 1) {
                navigate('/');
            } else {
                navigate(`/page/${displayPage}`);
            }
        }
    };

    const refreshMessages = () => {
        fetchMessages(currentPage);
    };

    return (
        <>
            {loading ? (
                <div>Loading messages...</div>
            ) : error ? (
                <div>Error: {error.message}</div>
            ) : (
                <MessageList
                    messages={messages}
                    page={currentPage}
                    totalPages={totalPages}
                    onPageChange={handlePageChange}
                    title="所有訊息"
                    showUserLinks={true}
                />
            )}
        </>
    );
}

function LoginPage() {
    const { login } = useUser();
    const { setPageTitle } = usePageTitle();
    
    useEffect(() => {
        setPageTitle('用戶登入');
    }, [setPageTitle]);
    
    const handleLoginSuccess = (user) => {
        login(user);
        // 可以選擇重定向到首頁或其他頁面
        window.location.href = '/';
    };

    return <Login onLoginSuccess={handleLoginSuccess} />;
}

export default App;
