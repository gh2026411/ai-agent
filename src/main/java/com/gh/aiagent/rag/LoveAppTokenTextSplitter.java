package com.gh.aiagent.rag;


import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LoveAppTokenTextSplitter {

    public List<Document> splitCustomized(List<Document> documents) {
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> result= splitter.apply(documents);
        return result;
    }
}
