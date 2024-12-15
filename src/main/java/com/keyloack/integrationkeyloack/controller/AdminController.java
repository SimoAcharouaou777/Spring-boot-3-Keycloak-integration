package com.keyloack.integrationkeyloack.controller;

import com.keyloack.integrationkeyloack.entity.Article;
import com.keyloack.integrationkeyloack.entity.User;
import com.keyloack.integrationkeyloack.service.ArticleServiceImpl;
import com.keyloack.integrationkeyloack.service.UserService;
import com.keyloack.integrationkeyloack.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    ArticleServiceImpl articleService;

    private final UserServiceImpl userServiceImpl;

    public AdminController(UserServiceImpl userServiceImpl){
        this.userServiceImpl = userServiceImpl;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.ok(userServiceImpl.createUser(user));
    }

    @PostMapping("/users/{id}/roles")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> assignRoleToUser(@PathVariable Long id, @RequestParam String roleName) {
        userServiceImpl.assignRoleToUser(id, roleName.toUpperCase());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @GetMapping("/articles/unapproved")
    public ResponseEntity<List<Article>> getUnapprovedArticles(){
        return ResponseEntity.ok(articleService.getUnapprovedArtiles());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @PostMapping("/articles/{id}/approve")
    public ResponseEntity<Article> approveArticle(@PathVariable Long id){
        return ResponseEntity.ok(articleService.approveArticle(id));
    }
}
