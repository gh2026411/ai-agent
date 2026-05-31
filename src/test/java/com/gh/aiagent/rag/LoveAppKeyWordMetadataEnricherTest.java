package com.gh.aiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class LoveAppKeyWordMetadataEnricherTest {


    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Resource
    private LoveAppKeyWordMetadataEnricher loveAppKeyWordMetadataEnricher;
    @Test
    void enrichDocuments() {
        loveAppKeyWordMetadataEnricher.enrichDocuments(loveAppDocumentLoader.loadMarkdown());
    }
}