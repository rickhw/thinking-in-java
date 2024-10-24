package com.example.article.service;

import com.example.article.entity.Article;
import com.example.article.event.ArticleCreatedEvent;
import com.example.article.event.ArticleUpdatedEvent;
import com.example.article.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CommandService {

    private final ArticleRepository articleRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public CommandService(ArticleRepository articleRepository, ApplicationEventPublisher eventPublisher) {
        this.articleRepository = articleRepository;
        this.eventPublisher = eventPublisher;
    }

    public Article createArticle(Article article) {
        article.setCreatedAt(LocalDateTime.now());
        Article savedArticle = articleRepository.save(article);
        eventPublisher.publishEvent(new ArticleCreatedEvent(this, savedArticle));
        return savedArticle;
    }

    public Optional<Article> updateArticle(Long id, Article updatedArticle) {
        Optional<Article> existingArticle = articleRepository.findById(id);
        if (existingArticle.isPresent()) {
            Article article = existingArticle.get();
            article.setSubject(updatedArticle.getSubject());
            article.setContent(updatedArticle.getContent());
            Article savedArticle = articleRepository.save(article);
            eventPublisher.publishEvent(new ArticleUpdatedEvent(this, savedArticle));
            return Optional.of(savedArticle);
        }
        return Optional.empty();
    }
}
