
package com.example.twitterclone.dto;

import com.example.twitterclone.domain.post.Post;

public class PostMapper {

    public static PostResponse toPostResponse(Post post) {
        PostResponse dto = new PostResponse();
        dto.setId(post.getId());
        dto.setContent(post.getContent());
        dto.setAuthorId(post.getUser().getId());
        dto.setAuthorName(post.getUser().getName());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        return dto;
    }
}
