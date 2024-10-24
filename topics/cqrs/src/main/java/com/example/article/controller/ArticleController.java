package com.example.article.controller;

import com.example.article.entity.Article;
import com.example.article.service.CommandService;
import com.example.article.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/article")
public class ArticleController {

    private final CommandService commandService;
    private final QueryService queryService;

    @Autowired
    public ArticleController(CommandService commandService, QueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<Article> createArticle(@RequestBody Article article) {
        Article createdArticle = commandService.createArticle(article);
        return ResponseEntity.ok(createdArticle);
    }

    @GetMapping
    public ResponseEntity<List<Article>> getArticles(@RequestParam int cursor, @RequestParam int pageSize) {
        List<Article> articles = queryService.getArticles(cursor, pageSize);
        return ResponseEntity.ok(articles);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Article> updateArticle(@PathVariable Long id, @RequestBody Article article) {
        Optional<Article> updatedArticle = commandService.updateArticle(id, article);
        return updatedArticle.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
