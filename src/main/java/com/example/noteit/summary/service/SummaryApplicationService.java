package com.example.noteit.summary.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SummaryApplicationService {

    private static final Logger log = LoggerFactory.getLogger(SummaryApplicationService.class);

    public void requestSummaryGeneration(String articleId) {
        // TODO: Read article metadata, then load body by content_storage_type (DB first, OSS later).
        log.info("Summary generation requested for articleId={}", articleId);
    }
}
