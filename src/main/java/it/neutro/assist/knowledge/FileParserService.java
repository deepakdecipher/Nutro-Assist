package it.neutro.assist.knowledge;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileParserService {

    public String extractText(MultipartFile file) throws IOException {
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        String ext = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : "";

        return switch (ext) {
            case "pdf"        -> parsePdf(file);
            case "xlsx", "xls" -> parseExcel(file);
            case "csv"        -> parseCsv(file);
            case "docx"       -> parseDocx(file);
            case "txt", "md", "text" -> parsePlainText(file);
            default -> throw new IllegalArgumentException(
                    "Unsupported file type: ." + ext + ". Supported: pdf, xlsx, xls, csv, docx, txt, md");
        };
    }

    public String getExtension(MultipartFile file) {
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        return name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : "unknown";
    }

    // ── PDF ────────────────────────────────────────────────────────────────
    private String parsePdf(MultipartFile file) throws IOException {
        try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    // ── Excel (XLSX / XLS) ────────────────────────────────────────────────
    private String parseExcel(MultipartFile file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            for (Sheet sheet : workbook) {
                sb.append("Sheet: ").append(sheet.getSheetName()).append("\n");
                for (Row row : sheet) {
                    StringBuilder rowText = new StringBuilder();
                    for (Cell cell : row) {
                        String val = switch (cell.getCellType()) {
                            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
                            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                            case FORMULA -> cell.getCellFormula();
                            default      -> cell.getStringCellValue();
                        };
                        if (!val.isBlank()) rowText.append(val.trim()).append(" | ");
                    }
                    if (!rowText.isEmpty()) sb.append(rowText).append("\n");
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    // ── CSV ───────────────────────────────────────────────────────────────
    private String parseCsv(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(line -> String.join(" | ", line.split(",")))
                    .collect(Collectors.joining("\n"));
        }
    }

    // ── DOCX ──────────────────────────────────────────────────────────────
    private String parseDocx(MultipartFile file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
            doc.getParagraphs().forEach(p -> {
                String text = p.getText();
                if (!text.isBlank()) sb.append(text).append("\n");
            });
            doc.getTables().forEach(table ->
                table.getRows().forEach(row -> {
                    row.getTableCells().forEach(cell -> sb.append(cell.getText()).append(" | "));
                    sb.append("\n");
                })
            );
        }
        return sb.toString();
    }

    // ── Plain text / Markdown ─────────────────────────────────────────────
    private String parsePlainText(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
