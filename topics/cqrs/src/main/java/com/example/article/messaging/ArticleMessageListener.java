package com.example.article.messaging;

import com.example.article.entity.Article;
import com.example.article.event.ArticleCreatedEvent;
import com.example.article.event.ArticleUpdatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class ArticleMessageListener {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String ARTICLE_CACHE_KEY = "ARTICLE_CACHE";

    @Autowired
    public ArticleMessageListener(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @EventListener
    public void handleArticleCreatedEvent(ArticleCreatedEvent event) {
        Article article = event.getArticle();
        redisTemplate.opsForHash().put(ARTICLE_CACHE_KEY, article.getId().toString(), article);
    }

    @EventListener
    public void handleArticleUpdatedEvent(ArticleUpdatedEvent event) {
        Article article = event.getArticle();
        redisTemplate.opsForHash().put(ARTICLE_CACHE_KEY, article.getId().toString(), article);
    }
}
