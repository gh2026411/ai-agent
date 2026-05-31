package com.gh.aiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.gh.aiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;


@Component
public class FileOperationTool {

    // 定义具体的文件存放子目录
    private final String FILE_DIR = FileConstant.FILE_SAVE_DIR + "/file";

    @Tool(description = "Read content from a file")
    public String readFile(@ToolParam(description = "Name of the file to read") String name) {
        String filePath = FILE_DIR + "/" + name;
        try {
            // 读取指定路径的UTF-8编码文件内容
            return FileUtil.readUtf8String(filePath);
        } catch (Exception e) {
            return "Error reading file: " + e.getMessage();
        }
    }

    @Tool(description = "Write content to a file and return it for browser download")
    public String writeFileForDownload(
            @ToolParam(description = "Name of the file to write") String name,
            @ToolParam(description = "Content to write to the file") String content) {

        try {
            // 返回文件内容供浏览器下载，格式：FILE_DOWNLOAD:filename:content
            return "FILE_DOWNLOAD:" + name + ":" + content;
        } catch (Exception e) {
            return "Error preparing file for download: " + e.getMessage();
        }
    }

    @Tool(description = "Write content to a file (saves to local disk)")
    public String writeFile(
            @ToolParam(description = "Name of the file to write") String name,
            @ToolParam(description = "Content to write to the file") String content) {

        String filePath = FILE_DIR + "/" + name;
        try {
            // 如果目录不存在则自动创建
            FileUtil.mkdir(FILE_DIR);
            // 将内容以UTF-8编码写入文件
            FileUtil.writeUtf8String(content, filePath);
            return "File written successfully to: " + filePath;
        } catch (Exception e) {
            return "Error writing to file: " + e.getMessage();
        }
    }
}