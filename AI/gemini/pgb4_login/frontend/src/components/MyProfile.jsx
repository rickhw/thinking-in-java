import React, { useState, useEffect } from 'react';
import { useUser } from '../contexts/UserContext';
import { usePageTitle } from '../contexts/PageContext';
import { updateUser } from '../api';

const MyProfile = () => {
    const { currentUser, isLoggedIn, login } = useUser();
    const { setPageTitle } = usePageTitle();
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: ''
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    useEffect(() => {
        setPageTitle('我的資料');
    }, [setPageTitle]);

    useEffect(() => {
        if (currentUser) {
            setFormData({
                username: currentUser.username || '',
                email: currentUser.email || '',
                password: ''
            });
        }
    }, [currentUser]);

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        setSuccess('');

        try {
            const updateData = {
                username: formData.username,
                email: formData.email
            };
            
            // 只有在輸入新密碼時才更新密碼
            if (formData.password.trim()) {
                updateData.password = formData.password;
            }

            const updatedUser = await updateUser(currentUser.id, updateData);
            
            // 更新本地用戶資訊
            login(updatedUser);
            setSuccess('資料更新成功！');
            setFormData({ ...formData, password: '' }); // 清空密碼欄位
        } catch (err) {
            setError(err.message || '更新失敗');
        } finally {
            setLoading(false);
        }
    };

    if (!isLoggedIn) {
        return <div>請先登入以查看您的資料</div>;
    }

    return (
        <div className="my-profile">
            <form onSubmit={handleSubmit} className="profile-form">
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
                    <label htmlFor="password">新密碼 (留空表示不更改):</label>
                    <input
                        type="password"
                        id="password"
                        name="password"
                        value={formData.password}
                        onChange={handleChange}
                        placeholder="輸入新密碼或留空"
                    />
                </div>

                {error && <div className="error-message">{error}</div>}
                {success && <div className="success-message">{success}</div>}
                
                <button type="submit" disabled={loading}>
                    {loading ? '更新中...' : '更新資料'}
                </button>
            </form>
        </div>
    );
};

export default MyProfile;