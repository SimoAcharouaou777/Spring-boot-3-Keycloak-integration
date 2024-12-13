package com.keyloack.integrationkeyloack.service;

import com.keyloack.integrationkeyloack.entity.Article;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;


public interface ArticleService {

    Article createArticle(Article article);

    List<Article> getArticlesById(Long id);
}
