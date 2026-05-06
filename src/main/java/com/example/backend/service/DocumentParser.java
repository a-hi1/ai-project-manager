package com.example.backend.service;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * 文档解析工具类
 * 支持：txt, md, docx, pdf 等格式
 */
public class DocumentParser {

    /**
     * 解析文档内容
     */
    public static String parseDocument(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            return "";
        }
        
        String lower = fileName.toLowerCase();
        
        if (lower.endsWith(".docx")) {
            return parseDocx(file);
        } else if (lower.endsWith(".pdf")) {
            return parsePdf(file);
        } else {
            // 文本文件：txt, md, html, json, xml, csv 等
            return parseText(file);
        }
    }

    /**
     * 解析 Word 文档 (.docx)
     */
    private static String parseDocx(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream();
             XWPFDocument document = new XWPFDocument(is)) {
            
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.trim().isEmpty()) {
                    sb.append(text).append("\n");
                }
            }
            return sb.toString();
        }
    }

    /**
     * 解析 PDF 文档
     */
    private static String parsePdf(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream();
             PDDocument document = Loader.loadPDF(is.readAllBytes())) {
            
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * 解析文本文件
     */
    private static String parseText(MultipartFile file) throws IOException {
        return new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
    }
}
