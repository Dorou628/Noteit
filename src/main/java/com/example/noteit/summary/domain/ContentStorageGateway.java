package com.example.noteit.summary.domain;

public interface ContentStorageGateway {

    String loadContent(String contentStorageType, String contentText, String contentObjectKey, String contentUrl);
}
