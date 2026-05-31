package com.gh.aiagent.rag; // 你的包名

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document; // Document 类
import org.springframework.ai.reader.markdown.MarkdownDocumentReader; // 读取器
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig; // 配置类
import org.springframework.beans.factory.annotation.Value; // @Value 注解
import org.springframework.core.io.Resource; // <--- 这里是你问的 Resource
import org.springframework.stereotype.Component; // @Component 注解

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
class LoveAppDocumentLoader {

    private final Resource[] resources;

    // 这里的 Resource 就是上面导入的 org.springframework.core.io.Resource
    LoveAppDocumentLoader(@Value("classpath:md/*.md") Resource[] resources) {
        this.resources = resources;
    }

    List<Document> loadMarkdown() {
        List<Document> allDocuments = new ArrayList<>();

        // 2. 遍历每一个找到的文件
        for (Resource resource : resources) {
            try {
                // 获取文件名用于元数据
                String filename = resource.getFilename();

                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("filename", filename) // 动态设置文件名
                        .build();

                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                // 将读取到的文档添加到总列表中
                allDocuments.addAll(reader.get());

            } catch (Exception e) {
                log.error("读取文件失败: {}", resource.getFilename(), e);

            }
        }
        return allDocuments;
    }
}