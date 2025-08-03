import React from 'react';
import { Link } from 'react-router-dom';
import { useUser } from '../contexts/UserContext';

const Navigation = () => {
    const { currentUser, logout, isLoggedIn } = useUser();

    const handleLogout = () => {
        logout();
    };

    return (
        <nav className="navigation">
            <ul>
                <li>
                    <Link to="/">首頁</Link>
                </li>
                {isLoggedIn ? (
                    <>
                        <li>
                            <Link to="/create">發布訊息</Link>
                        </li>
                        <li>
                            <Link to="/messages">我的訊息</Link>
                        </li>
                        <li>
                            <Link to="/profile">我的資料</Link>
                        </li>
                        <li>
                            <span>歡迎, {currentUser.username}!</span>
                        </li>
                        <li>
                            <button onClick={handleLogout} className="logout-btn">
                                登出
                            </button>
                        </li>
                    </>
                ) : (
                    <>
                        <li>
                            <Link to="/login">登入</Link>
                        </li>
                        <li>
                            <Link to="/register">註冊</Link>
                        </li>
                    </>
                )}
            </ul>
        </nav>
    );
};

export default Navigation;