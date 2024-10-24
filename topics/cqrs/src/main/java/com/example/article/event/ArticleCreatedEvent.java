package com.example.article.event;

import com.example.article.entity.Article;
import org.springframework.context.ApplicationEvent;

public class ArticleCreatedEvent extends ApplicationEvent {

    private final Article article;

    public ArticleCreatedEvent(Object source, Article article) {
        super(source);
        this.article = article;
    }

    public Article getArticle() {
        return article;
    }
}
