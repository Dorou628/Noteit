package com.example.noteit.summary.domain;

public interface SummaryExecutionStrategy {

    void execute(String articleId, Runnable task);
}
