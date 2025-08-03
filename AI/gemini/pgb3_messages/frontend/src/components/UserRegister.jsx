import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { registerUser } from '../api';

const UserRegister = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: ''
    });
    const [status, setStatus] = useState('');
    const [loading, setLoading] = useState(false);

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
            setStatus(`User created successfully! User ID: ${newUser.id}`);
            setTimeout(() => {
                navigate(`/profile/${newUser.id}`);
            }, 2000);
        } catch (error) {
            setStatus(`Error creating user: ${error.message}`);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="user-register">
            <div className="profile-header">
                <h2>Register New User</h2>
                <button onClick={() => navigate(-1)} className="back-button">
                    ← Back
                </button>
            </div>

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
                        <label htmlFor="password">Password:</label>
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
                            {loading ? 'Creating...' : 'Register User'}
                        </button>
                        <button type="button" onClick={() => navigate(-1)}>
                            Cancel
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