import React from 'react';
import PostList from '../components/PostList';
import CreatePostForm from '../components/CreatePostForm';

function HomePage() {
  return (
    <div>
      <h1>Home Page</h1>
      <CreatePostForm />
      <PostList />
    </div>
  );
}

export default HomePage;
