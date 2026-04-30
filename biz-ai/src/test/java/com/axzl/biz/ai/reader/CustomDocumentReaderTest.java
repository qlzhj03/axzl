package com.axzl.biz.ai.reader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CustomDocumentReader 单元测试
 *
 * @author liuchao
 */
class CustomDocumentReaderTest {

    private final CustomDocumentReader reader = new CustomDocumentReader();

    @TempDir
    Path tempDir;

    // ==================== InputStream读取测试 ====================

    @Test
    @DisplayName("读取TXT文本流 - 应返回全部文本内容")
    void testReadTxtStream() throws IOException {
        String content = "这是一段测试文本\n包含多行内容\n用于验证文本读取功能";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        String result = reader.readDocument(is, "txt");

        assertEquals(content, result);
    }

    @Test
    @DisplayName("读取MD文本流 - 应返回文本内容")
    void testReadMdStream() throws IOException {
        String content = "# 标题\n这是markdown内容\n**加粗文字**";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        String result = reader.readDocument(is, "md");

        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    @Test
    @DisplayName("不支持的文件类型 - 应抛出IOException")
    void testUnsupportedFileType() {
        InputStream is = new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8));

        IOException exception = assertThrows(IOException.class, () ->
                reader.readDocument(is, "exe"));

        assertTrue(exception.getMessage().contains("不支持的文件类型"));
    }

    // ==================== 文件读取测试 ====================

    @Test
    @DisplayName("读取TXT文件 - 应返回全部文本内容")
    void testReadTxtFile() throws IOException {
        String content = "文件内容\n第二行\n第三行";
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, content, StandardCharsets.UTF_8);

        String result = reader.readDocument(file.toString(), "txt");

        assertEquals(content.stripTrailing() + "\n", result.stripTrailing() + "\n");
    }

    @Test
    @DisplayName("读取MD文件 - 应去除Markdown标记")
    void testReadMdFile() throws IOException {
        String content = "# 标题\n\n这是正文内容\n\n**加粗**和*斜体*文字";
        Path file = tempDir.resolve("test.md");
        Files.writeString(file, content, StandardCharsets.UTF_8);

        String result = reader.readDocument(file.toString(), "md");

        assertNotNull(result);
        // 验证标题标记被去除
        assertFalse(result.contains("# 标题"));
        // 验证加粗标记被去除
        assertFalse(result.contains("**"));
        // 验证正文内容保留
        assertTrue(result.contains("这是正文内容"));
        assertTrue(result.contains("加粗"));
        assertTrue(result.contains("斜体"));
    }

    // ==================== Markdown清理测试 ====================

    @Test
    @DisplayName("Markdown标题标记 - 应被去除")
    void testMarkdownHeadersRemoved() throws IOException {
        String content = "# 一级标题\n## 二级标题\n### 三级标题";
        Path file = tempDir.resolve("headers.md");
        Files.writeString(file, content, StandardCharsets.UTF_8);

        String result = reader.readDocument(file.toString(), "md");

        assertFalse(result.contains("#"));
    }

    @Test
    @DisplayName("Markdown链接 - 应保留链接文字")
    void testMarkdownLinksCleaned() throws IOException {
        String content = "点击[这里](https://example.com)查看详情";
        Path file = tempDir.resolve("links.md");
        Files.writeString(file, content, StandardCharsets.UTF_8);

        String result = reader.readDocument(file.toString(), "md");

        assertTrue(result.contains("这里"));
        assertFalse(result.contains("](https"));
    }

    @Test
    @DisplayName("空MD文件 - 应返回空字符串")
    void testEmptyMdFile() throws IOException {
        Path file = tempDir.resolve("empty.md");
        Files.writeString(file, "", StandardCharsets.UTF_8);

        String result = reader.readDocument(file.toString(), "md");

        assertEquals("", result);
    }
}
