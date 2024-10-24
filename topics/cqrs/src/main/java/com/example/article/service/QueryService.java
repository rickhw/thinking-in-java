package com.example.article.service;

import com.example.article.entity.Article;
import com.example.article.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QueryService {

    private static final String ARTICLE_CACHE_KEY = "ARTICLE_CACHE";
    private final RedisTemplate<String, Object> redisTemplate;
    private final ArticleRepository articleRepository;

    @Autowired
    public QueryService(RedisTemplate<String, Object> redisTemplate, ArticleRepository articleRepository) {
        this.redisTemplate = redisTemplate;
        this.articleRepository = articleRepository;
    }

    public List<Article> getArticles(int cursor, int pageSize) {
        String cacheKey = cursor + "::" + pageSize;
        List<Article> articles = (List<Article>) redisTemplate.opsForValue().get(cacheKey);

        if (articles != null) {
            return articles;
        }

        articles = articleRepository.findAll()
                                    .stream()
                                    .skip(cursor)
                                    .limit(pageSize)
                                    .collect(Collectors.toList());

        redisTemplate.opsForValue().set(cacheKey, articles);

        return articles;
    }
}
