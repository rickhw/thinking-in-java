
import { useEffect, useState } from 'react';
import axios from 'axios';
import { Post } from './types';
import { Card, ListGroup } from 'react-bootstrap';

function PostListPage() {
    const [posts, setPosts] = useState<Post[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        axios.get('/api/posts')
            .then(response => {
                setPosts(response.data.content);
                setLoading(false);
            })
            .catch(error => {
                setError('Error fetching posts');
                setLoading(false);
                console.error('There was an error fetching the posts!', error);
            });
    }, []);

    if (loading) return <div>Loading...</div>;
    if (error) return <div>{error}</div>;

    return (
        <div className="container mt-4">
            <h1>All Posts</h1>
            <ListGroup>
                {posts.map(post => (
                    <ListGroup.Item key={post.id}>
                        <Card>
                            <Card.Body>
                                <Card.Title>{post.authorName}</Card.Title>
                                <Card.Text>{post.content}</Card.Text>
                                <Card.Footer className="text-muted">
                                    Posted on {new Date(post.createdAt).toLocaleDateString()}
                                </Card.Footer>
                            </Card.Body>
                        </Card>
                    </ListGroup.Item>
                ))}
            </ListGroup>
        </div>
    );
}

export default PostListPage;
