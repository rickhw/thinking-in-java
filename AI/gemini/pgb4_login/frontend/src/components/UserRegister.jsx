import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { registerUser } from '../api';
import { useUser } from '../contexts/UserContext';
import { usePageTitle } from '../contexts/PageContext';

const UserRegister = () => {
    const navigate = useNavigate();
    const { login } = useUser();
    const { setPageTitle } = usePageTitle();
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: ''
    });
    const [status, setStatus] = useState('');
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        setPageTitle('用戶註冊');
    }, [setPageTitle]);

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setStatus('Creating user...');

        try {
            const newUser = await registerUser(formData);
            setStatus(`註冊成功！正在自動登入...`);
            
            // 自動登入新註冊的用戶
            login(newUser);
            
            setTimeout(() => {
                navigate('/');
            }, 1500);
        } catch (error) {
            setStatus(`註冊失敗: ${error.message}`);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="user-register">

            <div className="profile-edit">
                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label htmlFor="username">用戶名:</label>
                        <input
                            type="text"
                            id="username"
                            name="username"
                            value={formData.username}
                            onChange={handleChange}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="email">電子郵件:</label>
                        <input
                            type="email"
                            id="email"
                            name="email"
                            value={formData.email}
                            onChange={handleChange}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="password">密碼:</label>
                        <input
                            type="password"
                            id="password"
                            name="password"
                            value={formData.password}
                            onChange={handleChange}
                            required
                        />
                    </div>
                    <div className="form-actions">
                        <button type="submit" disabled={loading}>
                            {loading ? '註冊中...' : '註冊'}
                        </button>
                        <button type="button" onClick={() => navigate(-1)}>
                            取消
                        </button>
                    </div>
                </form>
            </div>

            {status && (
                <div className={`status-message ${status.includes('successfully') ? 'success' : 'error'}`}>
                    {status}
                </div>
            )}
        </div>
    );
};

export default UserRegister;