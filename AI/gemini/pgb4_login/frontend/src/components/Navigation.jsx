import React from 'react';
import { Link } from 'react-router-dom';
import { useUser } from '../contexts/UserContext';
import { usePageTitle } from '../contexts/PageContext';

const Navigation = () => {
    const { currentUser, logout, isLoggedIn } = useUser();
    const { pageTitle } = usePageTitle();

    const handleLogout = () => {
        logout();
    };

    return (
        <nav className="navigation">
            <div className="nav-left">
                <h1 className="page-title">{pageTitle}</h1>
            </div>
            
            <div className="nav-right">
                <ul className="nav-menu">
                    <li>
                        <Link to="/" className="nav-link">
                            <span className="nav-icon">🏠</span>
                            <span className="nav-text">首頁</span>
                        </Link>
                    </li>
                    {isLoggedIn ? (
                        <>
                            <li>
                                <Link to="/create" className="nav-link">
                                    <span className="nav-icon">✏️</span>
                                    <span className="nav-text">發布</span>
                                </Link>
                            </li>
                            <li>
                                <Link to="/messages" className="nav-link">
                                    <span className="nav-icon">💬</span>
                                    <span className="nav-text">我的訊息</span>
                                </Link>
                            </li>
                            <li>
                                <Link to="/profile" className="nav-link">
                                    <span className="nav-icon">👤</span>
                                    <span className="nav-text">資料</span>
                                </Link>
                            </li>
                            <li className="user-info">
                                <span className="welcome-text">
                                    <span className="nav-icon">👋</span>
                                    {currentUser.username}
                                </span>
                            </li>
                            <li>
                                <button onClick={handleLogout} className="logout-btn">
                                    <span className="nav-icon">🚪</span>
                                    <span className="nav-text">登出</span>
                                </button>
                            </li>
                        </>
                    ) : (
                        <>
                            <li>
                                <Link to="/login" className="nav-link">
                                    <span className="nav-icon">🔑</span>
                                    <span className="nav-text">登入</span>
                                </Link>
                            </li>
                            <li>
                                <Link to="/register" className="nav-link">
                                    <span className="nav-icon">📝</span>
                                    <span className="nav-text">註冊</span>
                                </Link>
                            </li>
                        </>
                    )}
                </ul>
            </div>
        </nav>
    );
};

export default Navigation;