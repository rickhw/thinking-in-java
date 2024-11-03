// Login.js
import React, { useEffect, useState } from 'react';

function Login() {
    const [user, setUser] = useState(null);

    useEffect(() => {
        fetch(`${process.env.REACT_APP_BACKEND_URL}/user`, { credentials: 'include' })
            .then((response) => response.json())
            .then((data) => setUser(data));
    }, []);

    return (
        <div>
            {user ? (
                <div>
                    <h2>Welcome, {user.name}</h2>
                    <img src={user.picture} alt="User profile" />
                </div>
            ) : (
                <a href={`${process.env.REACT_APP_BACKEND_URL}/oauth2/authorization/google`}>
                    Login with Google
                </a>
            )}
        </div>
    );
}

export default Login;
