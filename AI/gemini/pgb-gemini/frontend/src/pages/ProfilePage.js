import React, { useEffect, useState, useCallback } from 'react';
import api from '../api';
import PostCard from '../components/PostCard';

function ProfilePage() {
  const [posts, setPosts] = useState([]);

  const fetchMyPosts = useCallback(async () => {
    try {
      const response = await api.get('/users/me/posts');
      setPosts(response.data.content);
    } catch (error) {
      console.error('Error fetching my posts:', error);
    }
  }, []);

  useEffect(() => {
    fetchMyPosts();
  }, [fetchMyPosts]);

  return (
    <div>
      <h1>My Posts</h1>
      {posts.map(post => (
        <PostCard key={post.id} post={post} onUpdate={fetchMyPosts} onDelete={fetchMyPosts} />
      ))}
    </div>
  );
}

export default ProfilePage;
