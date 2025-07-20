import React, { useEffect, useState, useCallback } from 'react';
import api from '../api';
import PostCard from './PostCard';

function PostList() {
  const [posts, setPosts] = useState([]);

  const fetchPosts = useCallback(() => {
    api.get('/posts')
      .then(response => {
        setPosts(response.data.content);
      })
      .catch(error => {
        console.error('Error fetching posts:', error);
      });
  }, []);

  useEffect(() => {
    fetchPosts();
  }, [fetchPosts]);

  return (
    <div>
      <h2>Latest Posts</h2>
      {posts.map(post => (
        <PostCard key={post.id} post={post} onUpdate={fetchPosts} onDelete={fetchPosts} />
      ))}
    </div>
  );
}

export default PostList;
