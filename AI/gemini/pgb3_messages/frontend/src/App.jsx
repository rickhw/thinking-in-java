import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import MessageList from './components/MessageList';
import MessageForm from './components/MessageForm';
import UserMessages from './components/UserMessages';
import UserProfile from './components/UserProfile';
import UserRegister from './components/UserRegister';
import { getMessages } from './api';
import './App.css';

function App() {
    return (
        <Router>
            <div className="App">
                <nav>
                    <ul>
                        <li>
                            <Link to="/">Home</Link>
                        </li>
                        <li>
                            <Link to="/user/rick/messages">Rick's Messages</Link>
                        </li>
                        <li>
                            <Link to="/profile/1">Rick's Profile</Link>
                        </li>
                        <li>
                            <Link to="/profile/2">Alice's Profile</Link>
                        </li>
                        <li>
                            <Link to="/register">Register User</Link>
                        </li>
                    </ul>
                </nav>
                <Routes>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/user/:userId/messages" element={<UserMessages />} />
                    <Route path="/profile/:userId" element={<UserProfile />} />
                    <Route path="/register" element={<UserRegister />} />
                </Routes>
            </div>
        </Router>
    );
}

function HomePage() {
    const [messages, setMessages] = useState([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchMessages = async (currentPage) => {
        setLoading(true);
        setError(null);
        try {
            const data = await getMessages(currentPage);
            setMessages(data.content || []);
            setTotalPages(data.totalPages || 0);
            setPage(data.number || 0);
        } catch (err) {
            setError(err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchMessages(page);
    }, [page]);

    const handlePageChange = (newPage) => {
        if (newPage >= 0 && newPage < totalPages) {
            setPage(newPage);
        }
    };

    const refreshMessages = () => {
        fetchMessages(page);
    };

    return (
        <>
            <h1>Message Board</h1>
            <MessageForm onMessageCreated={refreshMessages} />
            {loading ? (
                <div>Loading messages...</div>
            ) : error ? (
                <div>Error: {error.message}</div>
            ) : (
                <MessageList
                    messages={messages}
                    page={page}
                    totalPages={totalPages}
                    onPageChange={handlePageChange}
                />
            )}
        </>
    );
}

export default App;
