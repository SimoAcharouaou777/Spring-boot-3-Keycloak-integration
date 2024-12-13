package com.keyloack.integrationkeyloack.controller;

import com.keyloack.integrationkeyloack.entity.Article;
import com.keyloack.integrationkeyloack.service.ArticleServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/articles")
public class ArticleController {

    private final ArticleServiceImpl articleServiceImpl;

    public ArticleController(ArticleServiceImpl articleServiceImpl) {
        this.articleServiceImpl = articleServiceImpl;
    }

    @PostMapping
    public ResponseEntity<Article> createArticle(@RequestBody Article article) {
        return ResponseEntity.ok(articleServiceImpl.createArticle(article));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<List<Article>> getArticles(@PathVariable Long id) {
        return ResponseEntity.ok(articleServiceImpl.getArticlesById(id));
    }
}
