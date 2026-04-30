package com.axzl.biz.ai.reader;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * 自定义文档读取器
 * 支持 txt/word(.docx)/pdf/md 格式的文本提取
 * 扩展 Spring AI DocumentReader 标准接口
 *
 * @author liuchao
 */
public class CustomDocumentReader {

    private static final Logger log = LoggerFactory.getLogger(CustomDocumentReader.class);

    /**
     * 读取文件并提取文本内容
     *
     * @param filePath 文件路径
     * @param fileType 文件类型(txt/word/pdf/md)
     * @return 提取的文本内容
     */
    public String readDocument(String filePath, String fileType) {
        try {
            return switch (fileType.toLowerCase()) {
                case "txt" -> readTextFile(filePath);
                case "word" -> readWordFile(filePath);
                case "pdf" -> readPdfFile(filePath);
                case "md" -> readMarkdownFile(filePath);
                default -> throw new IllegalArgumentException("不支持的文件类型: " + fileType);
            };
        } catch (IOException e) {
            log.error("读取文档失败, 文件路径: {}, 文件类型: {}", filePath, fileType, e);
            throw new RuntimeException("读取文档失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从InputStream读取文档
     */
    public String readDocument(InputStream inputStream, String fileType) throws IOException {
        if (fileType == null) {
            throw new IOException("文件类型不能为空");
        }
        
        return switch (fileType.toLowerCase()) {
            case "txt" -> readTextStream(inputStream);
            case "word" -> readWordStream(inputStream);
            case "pdf" -> readPdfStream(inputStream);
            case "md" -> readTextStream(inputStream);
            default -> throw new IOException("不支持的文件类型: " + fileType);
        };
    }

    /**
     * 读取纯文本文件
     */
    private String readTextFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    /**
     * 读取Word文档(.docx)
     */
    private String readWordFile(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument document = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    /**
     * 读取PDF文档
     */
    private String readPdfFile(String filePath) throws IOException {
        try (PDDocument document = Loader.loadPDF(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        }
    }

    /**
     * 读取Markdown文件（去除markdown标记）
     */
    private String readMarkdownFile(String filePath) throws IOException {
        String content = readTextFile(filePath);
        return cleanMarkdown(content);
    }

    /**
     * 从InputStream读取文本
     */
    private String readTextStream(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    /**
     * 从InputStream读取Word文档
     */
    private String readWordStream(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    /**
     * 从InputStream读取PDF文档
     */
    private String readPdfStream(InputStream inputStream) throws IOException {
        // PDFBox需要支持mark/reset的流
        byte[] bytes = inputStream.readAllBytes();
        try (PDDocument document = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        }
    }

    /**
     * 清理Markdown格式标记
     */
    private String cleanMarkdown(String markdown) {
        if (markdown == null) return "";
        return markdown
                .replaceAll("#{1,6}\\s*", "")           // 去除标题标记
                .replaceAll("\\*{1,2}(.+?)\\*{1,2}", "$1") // 去除加粗/斜体
                .replaceAll("`{1,3}(.+?)`{1,3}", "$1")     // 去除行内代码
                .replaceAll("\\[([^\\]]+)\\]\\([^\\)]+\\)", "$1") // 去除链接
                .replaceAll("!\\[([^\\]]*)\\]\\([^\\)]+\\)", "$1") // 去除图片
                .replaceAll("[-*+]\\s+", "")              // 去除列表标记
                .replaceAll("\\|", " ")                   // 表格分隔符替换
                .replaceAll("^---+$", "")                 // 去除水平线
                .replaceAll(">\\s+", "")                  // 去除引用标记
                .trim();
    }
}
