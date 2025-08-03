import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getUserById, updateUser } from '../api';

const UserProfile = () => {
    const { userId } = useParams();
    const navigate = useNavigate();
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isEditing, setIsEditing] = useState(false);
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: ''
    });
    const [updateStatus, setUpdateStatus] = useState('');

    useEffect(() => {
        const fetchUser = async () => {
            setLoading(true);
            setError(null);
            try {
                const userData = await getUserById(userId);
                setUser(userData);
                setFormData({
                    username: userData.username || '',
                    email: userData.email || '',
                    password: ''
                });
            } catch (err) {
                setError(`Failed to fetch user data: ${err.message}`);
            } finally {
                setLoading(false);
            }
        };

        if (userId) {
            fetchUser();
        }
    }, [userId]);

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setUpdateStatus('Updating...');
        try {
            const updatedUser = await updateUser(userId, formData);
            setUser(updatedUser);
            setIsEditing(false);
            setUpdateStatus('Profile updated successfully!');
            // Clear password field after successful update
            setFormData({
                ...formData,
                password: ''
            });
            setTimeout(() => setUpdateStatus(''), 3000);
        } catch (err) {
            setUpdateStatus(`Failed to update profile: ${err.message}`);
            setTimeout(() => setUpdateStatus(''), 5000);
        }
    };

    const handleCancel = () => {
        setIsEditing(false);
        setFormData({
            username: user.username || '',
            email: user.email || '',
            password: ''
        });
        setUpdateStatus('');
    };

    if (loading) return <div>Loading user profile...</div>;
    if (error) return <div style={{ color: 'red' }}>Error: {error}</div>;
    if (!user) return <div>User not found.</div>;

    return (
        <div className="user-profile">
            <div className="profile-header">
                <h2>User Profile</h2>
                <button onClick={() => navigate(-1)} className="back-button">
                    ← Back
                </button>
            </div>

            {!isEditing ? (
                <div className="profile-view">
                    <div className="profile-info">
                        <div className="info-item">
                            <strong>User ID:</strong> {user.id}
                        </div>
                        <div className="info-item">
                            <strong>Username:</strong> {user.username}
                        </div>
                        <div className="info-item">
                            <strong>Email:</strong> {user.email}
                        </div>
                    </div>
                    <div className="profile-actions">
                        <button onClick={() => setIsEditing(true)} className="edit-button">
                            Edit Profile
                        </button>
                        <button 
                            onClick={() => navigate(`/user/${user.username}/messages`)}
                            className="messages-button"
                        >
                            View Messages
                        </button>
                    </div>
                </div>
            ) : (
                <div className="profile-edit">
                    <form onSubmit={handleSubmit}>
                        <div className="form-group">
                            <label htmlFor="username">Username:</label>
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
                            <label htmlFor="email">Email:</label>
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
                            <label htmlFor="password">New Password (optional):</label>
                            <input
                                type="password"
                                id="password"
                                name="password"
                                value={formData.password}
                                onChange={handleChange}
                                placeholder="Leave blank to keep current password"
                            />
                        </div>
                        <div className="form-actions">
                            <button type="submit">Save Changes</button>
                            <button type="button" onClick={handleCancel}>
                                Cancel
                            </button>
                        </div>
                    </form>
                </div>
            )}

            {updateStatus && (
                <div className={`status-message ${updateStatus.includes('success') ? 'success' : 'error'}`}>
                    {updateStatus}
                </div>
            )}
        </div>
    );
};

export default UserProfile;