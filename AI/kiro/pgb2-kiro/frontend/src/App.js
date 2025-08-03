import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import './App.css';

function App() {
  return (
    <Router>
      <div className="App">
        <header className="App-header">
          <h1>Twitter Board</h1>
          <p>Coming Soon...</p>
        </header>
        <main>
          <Routes>
            <Route path="/" element={<div>Home Page</div>} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;