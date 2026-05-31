package com.gh.aiagent.rag;


import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

@Configuration
public class LoveAppVectorStoreConfig {
    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;
    @Bean
    VectorStore loveAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel)
                .build();

        List<Document> documents = loveAppDocumentLoader.loadMarkdown();
        simpleVectorStore.add(documents);
        return simpleVectorStore;
    }

//    pgvector配置
//    @Autowired
//    VectorStore vectorStore;
//    @Resource
//    private LoveAppKeyWordMetadataEnricher loveAppKeyWordMetadataEnricher;
//
//
//    @Bean
//    public VectorStore loveAppVectorStoreWithPg(EmbeddingModel dashscopeEmbeddingModel){
//        List<Document> documents =loveAppKeyWordMetadataEnricher.enrichDocuments(loveAppDocumentLoader.loadMarkdown()) ;
//
//        // 设置批次大小为 10，符合 DashScope 接口的限制
//        int batchSize = 10;
//        for (int i = 0; i < documents.size(); i += batchSize) {
//            // 截取当前批次的子列表（防止最后一批越界）
//            List<Document> batch = documents.subList(i, Math.min(i + batchSize, documents.size()));
//            // 将这一小批文档存入向量数据库
//            vectorStore.add(batch);
//        }
//
//        return vectorStore;
//    }

}
