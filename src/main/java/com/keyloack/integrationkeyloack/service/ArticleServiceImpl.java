package com.keyloack.integrationkeyloack.service;

import com.keyloack.integrationkeyloack.entity.Article;
import com.keyloack.integrationkeyloack.entity.User;
import com.keyloack.integrationkeyloack.repository.ArticleRepository;
import com.keyloack.integrationkeyloack.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    public ArticleServiceImpl(ArticleRepository articleRepository, UserRepository userRepository) {
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Article createArticle(Article article) {
        return articleRepository.save(article);
    }

    @Override
    public List<Article> getArticlesById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return articleRepository.findByAuthor(user);
    }
}

