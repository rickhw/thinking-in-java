import React, { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

function OAuth2RedirectHandler() {
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const getTokenFromUrl = () => {
      const params = new URLSearchParams(location.search);
      return params.get('token');
    };

    const token = getTokenFromUrl();
    if (token) {
      localStorage.setItem('jwtToken', token);
      navigate('/'); // Redirect to home page after successful login
    } else {
      // Handle error or show a message
      navigate('/login');
    }
  }, [location, navigate]);

  return (
    <div>
      Loading...
    </div>
  );
}

export default OAuth2RedirectHandler;
