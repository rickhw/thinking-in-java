import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import MessageList from './components/MessageList';
import MessageForm from './components/MessageForm';
import UserMessages from './components/UserMessages';

function App() {
  return (
    <Router>
      <div style={{ padding: '20px' }}>
        <h1>Message Board</h1>
        <nav>
          <ul style={{ listStyle: 'none', padding: 0, display: 'flex', gap: '15px' }}>
            <li>
              <Link to="/">All Messages</Link>
            </li>
            <li>
              <Link to="/new">New Message</Link>
            </li>
            <li>
              <Link to="/user/example_user">Messages by Example User</Link>
            </li>
          </ul>
        </nav>

        <hr />

        <Routes>
          <Route path="/" element={<MessageList />} />
          <Route path="/new" element={<MessageForm />} />
          <Route path="/user/:userId" element={<UserMessages />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;