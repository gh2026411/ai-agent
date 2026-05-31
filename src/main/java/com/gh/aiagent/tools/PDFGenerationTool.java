package com.gh.aiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.gh.aiagent.constant.FileConstant;
import com.itextpdf.io.font.PdfEncodings; // 建议显式引用编码常量
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;

public class PDFGenerationTool {

    @Tool(description = "Generate a PDF file with given content")
    public String generatePDF(
            @ToolParam(description = "Name of the file to save the generated PDF") String fileName,
            @ToolParam(description = "Content to be included in the PDF") String content) {

        // 1. 优化路径拼接 (防止双斜杠或缺失斜杠)
        String fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
        String filePath = fileDir + "/" + fileName;

        try {
            // 创建目录
            FileUtil.mkdir(fileDir);

            // 2. 修正字体名称为 iText 7 标准名称
            // STSong-Light 是常用的中文宋体
            PdfFont font = PdfFontFactory.createFont("STSong-Light", PdfEncodings.IDENTITY_H);

            // 写入 PDF
            try (PdfWriter writer = new PdfWriter(filePath);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {

                document.setFont(font);
                Paragraph paragraph = new Paragraph(content);
                document.add(paragraph);
            }

            return "PDF generated successfully to: " + filePath;

        } catch (IOException e) {
            // 捕获具体的 IO 异常或字体加载异常
            return "Error generating PDF: " + e.getMessage();
        }
    }
}