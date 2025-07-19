import React, { useState } from 'react';
import api from '../api';

function PostCard({ post, onUpdate, onDelete }) {
  const [isEditing, setIsEditing] = useState(false);
  const [editedContent, setEditedContent] = useState(post.content);

  const handleUpdate = async () => {
    try {
      await api.put(`/posts/${post.id}`, { content: editedContent });
      setIsEditing(false);
      if (onUpdate) onUpdate();
      alert('Post updated successfully!');
    } catch (error) {
      console.error('Error updating post:', error);
      alert('Failed to update post.');
    }
  };

  const handleDelete = async () => {
    if (window.confirm('Are you sure you want to delete this post?')) {
      try {
        await api.delete(`/posts/${post.id}`);
        if (onDelete) onDelete();
        alert('Post deleted successfully!');
      } catch (error) {
        console.error('Error deleting post:', error);
        alert('Failed to delete post.');
      }
    }
  };

  return (
    <div style={{ border: '1px solid #ccc', padding: '10px', margin: '10px 0' }}>
      {isEditing ? (
        <textarea
          value={editedContent}
          onChange={(e) => setEditedContent(e.target.value)}
          rows="4"
          cols="50"
        />
      ) : (
        <h3>{post.content}</h3>
      )}
      <p>By: {post.author ? post.author.name : 'Unknown'}</p>
      <p>Posted on: {new Date(post.createdAt).toLocaleString()}</p>
      {isEditing ? (
        <>
          <button onClick={handleUpdate}>Save</button>
          <button onClick={() => setIsEditing(false)}>Cancel</button>
        </>
      ) : (
        <>
          <button onClick={() => setIsEditing(true)}>Edit</button>
          <button onClick={handleDelete}>Delete</button>
        </>
      )}
    </div>
  );
}

export default PostCard;
