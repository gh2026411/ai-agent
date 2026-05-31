package com.gh.aiagent.tools;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class FileOperationToolTest {
    @Resource
    private FileOperationTool fileOperationTool;
    @Test
    void readFile() {String result=fileOperationTool.readFile("filetest");
        assertNotNull(result);
    }



    @Test
    void writeFile() {fileOperationTool.writeFile("filetest","hello world");
    }
}