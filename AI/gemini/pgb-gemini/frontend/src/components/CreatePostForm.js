import React, { useState } from 'react';
import api from '../api';

function CreatePostForm() {
  const [content, setContent] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.post('/posts', { content });
      setContent('');
      alert('Post created successfully!');
      // Optionally, refresh the post list
    } catch (error) {
      console.error('Error creating post:', error);
      alert('Failed to create post.');
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <textarea
        placeholder="What's on your mind?"
        value={content}
        onChange={(e) => setContent(e.target.value)}
        rows="4"
        cols="50"
      />
      <br />
      <button type="submit">Create Post</button>
    </form>
  );
}

export default CreatePostForm;
