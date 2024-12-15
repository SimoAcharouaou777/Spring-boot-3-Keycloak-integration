package com.keyloack.integrationkeyloack.controller;

import com.keyloack.integrationkeyloack.entity.Article;
import com.keyloack.integrationkeyloack.entity.User;
import com.keyloack.integrationkeyloack.service.ArticleServiceImpl;
import com.keyloack.integrationkeyloack.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Stack;

@RestController
@RequestMapping("/articles")
public class ArticleController {

    @Autowired
    private UserServiceImpl userServiceImpl;

    private final ArticleServiceImpl articleServiceImpl;

    public ArticleController(ArticleServiceImpl articleServiceImpl) {
        this.articleServiceImpl = articleServiceImpl;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_USER')")
    public ResponseEntity<Article> createArticle(@RequestBody Article article , Principal principal) {
        String username = principal.getName();
        User user =  userServiceImpl.getUser(username);
        article.setAuthor(user);
        return ResponseEntity.ok(articleServiceImpl.createArticle(article));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<List<Article>> getArticles(@PathVariable Long id) {
        return ResponseEntity.ok(articleServiceImpl.getArticlesById(id));
    }
}
