import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import MessageList from './components/MessageList';
import MessageForm from './components/MessageForm';
import UserMessages from './components/UserMessages';
import { getAllMessages } from './api';

function App() {
  const [messages, setMessages] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchAllMessages = async (currentPage) => {
    setLoading(true);
    setError(null);
    try {
      const data = await getAllMessages(currentPage);
      setMessages(data.content);
      setTotalPages(data.totalPages);
      setPage(data.number);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAllMessages(page);
  }, [page]);

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < totalPages) {
      setPage(newPage);
    }
  };

  if (loading) return <div>Loading messages...</div>;
  if (error) return <div>Error: {error.message}</div>;

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
          <Route
            path="/"
            element={
              <MessageList
                messages={messages}
                page={page}
                totalPages={totalPages}
                onPageChange={handlePageChange}
              />
            }
          />
          <Route path="/new" element={<MessageForm />} />
          <Route path="/user/:userId" element={<UserMessages />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;