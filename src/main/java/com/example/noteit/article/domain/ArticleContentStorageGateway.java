package com.example.noteit.article.domain;

public interface ArticleContentStorageGateway {

    StoredArticleContent store(String content, String contentFormat, String contentPreview);

    String load(String contentStorageType, String contentText, String contentObjectKey, String contentUrl);
}
