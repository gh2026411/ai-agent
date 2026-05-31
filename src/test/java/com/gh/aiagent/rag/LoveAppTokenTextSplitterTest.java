package com.gh.aiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class LoveAppTokenTextSplitterTest {


    @Resource
    private LoveAppTokenTextSplitter loveAppTokenTextSplitter;
    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;
    @Test
    void splitCustomized() {
        loveAppTokenTextSplitter.splitCustomized(loveAppDocumentLoader.loadMarkdown());
    }
}