package com.example.noteit.article.repository;

import com.example.noteit.article.model.ArticleMediaDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleMediaMapper {

    int batchInsert(@Param("mediaList") List<ArticleMediaDO> mediaList);

    int deleteByArticleId(@Param("articleId") long articleId);

    List<ArticleMediaDO> findByArticleId(@Param("articleId") long articleId);
}
