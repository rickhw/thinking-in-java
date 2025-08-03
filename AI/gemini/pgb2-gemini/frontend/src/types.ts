
export interface Post {
    id: number;
    content: string;
    authorId: number;
    authorName: string;
    createdAt: string; // ISO 8601 date string
    updatedAt: string; // ISO 8601 date string
}
