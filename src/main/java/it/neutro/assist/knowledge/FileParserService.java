package it.neutro.assist.knowledge;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileParserService {

    static final int MAX_TEXT_CHARS = 300_000;
    // Safety cap for legacy .xls (binary format, can't stream) to bound heap
    private static final int MAX_XLS_ROWS = 5_000;

    public String extractText(MultipartFile file) throws IOException {
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        String ext  = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : "";

        String text = switch (ext) {
            case "pdf"               -> parsePdf(file);
            case "xlsx"              -> parseXlsxStreaming(file);   // SAX stream — constant memory
            case "xls"               -> parseXlsLegacy(file);       // load-based, row-capped
            case "csv"               -> parseCsv(file);
            case "docx"              -> parseDocx(file);
            case "txt", "md", "text" -> parsePlainText(file);
            default -> throw new IllegalArgumentException(
                    "Unsupported file type: ." + ext + ". Supported: pdf, xlsx, xls, csv, docx, txt, md");
        };

        if (text.length() > MAX_TEXT_CHARS) {
            log.warn("Truncating text from {} chars to {} for file '{}'", text.length(), MAX_TEXT_CHARS, name);
            return text.substring(0, MAX_TEXT_CHARS) + "\n[... content truncated due to file size]";
        }
        return text;
    }

    public String getExtension(MultipartFile file) {
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        return name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : "unknown";
    }

    // ── PDF ───────────────────────────────────────────────────────────────
    private String parsePdf(MultipartFile file) throws IOException {
        try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
            return new PDFTextStripper().getText(doc);
        }
    }

    // ── XLSX: SAX event streaming — row-by-row, O(1) heap regardless of file size ──
    //   WorkbookFactory inflates a 5 MB xlsx to ~200 MB of Java objects; this approach
    //   keeps memory at roughly the file's compressed size (~5 MB).
    private String parseXlsxStreaming(MultipartFile file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (OPCPackage pkg = OPCPackage.open(file.getInputStream())) {
            XSSFReader xssfReader = new XSSFReader(pkg);
            SharedStrings sst    = xssfReader.getSharedStringsTable();
            StylesTable   styles = xssfReader.getStylesTable();
            DataFormatter fmt    = new DataFormatter();

            XSSFReader.SheetIterator sheetIter =
                    (XSSFReader.SheetIterator) xssfReader.getSheetsData();

            while (sheetIter.hasNext()) {
                try (InputStream sheetStream = sheetIter.next()) {
                    sb.append("Sheet: ").append(sheetIter.getSheetName()).append("\n");
                    RowCollector handler = new RowCollector(sb);
                    XMLReader xmlReader  = XMLHelper.newXMLReader();
                    xmlReader.setContentHandler(
                            new XSSFSheetXMLHandler(styles, null, sst, handler, fmt, false));
                    xmlReader.parse(new InputSource(sheetStream));
                    sb.append("\n");
                }
                if (sb.length() > MAX_TEXT_CHARS) break;
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Could not read xlsx file: " + e.getMessage(), e);
        }
        return sb.toString();
    }

    // ── XLS (legacy binary) — load into memory but cap rows to bound heap ──
    private String parseXlsLegacy(MultipartFile file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            DataFormatter fmt = new DataFormatter();
            for (Sheet sheet : workbook) {
                sb.append("Sheet: ").append(sheet.getSheetName()).append("\n");
                int rows = 0;
                for (Row row : sheet) {
                    if (row == null) continue;
                    if (++rows > MAX_XLS_ROWS) {
                        sb.append("[truncated — row limit reached]\n");
                        break;
                    }
                    StringBuilder rowText = new StringBuilder();
                    for (Cell cell : row) {
                        String val = safeFormat(fmt, cell);
                        if (!val.isBlank()) rowText.append(val.trim()).append(" | ");
                    }
                    if (!rowText.isEmpty()) sb.append(rowText).append("\n");
                }
                sb.append("\n");
                if (sb.length() > MAX_TEXT_CHARS) break;
            }
        } catch (Exception e) {
            throw new IOException("Could not read xls file: " + e.getMessage(), e);
        }
        return sb.toString();
    }

    private String safeFormat(DataFormatter fmt, Cell cell) {
        try { return fmt.formatCellValue(cell); } catch (Exception e) { return ""; }
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

    // ── SAX row handler for streaming xlsx ───────────────────────────────
    private static final class RowCollector implements SheetContentsHandler {
        private final StringBuilder sb;
        private final List<String>  row = new ArrayList<>();

        RowCollector(StringBuilder sb) { this.sb = sb; }

        @Override public void startRow(int rowNum) { row.clear(); }

        @Override public void endRow(int rowNum) {
            if (!row.isEmpty()) sb.append(String.join(" | ", row)).append("\n");
        }

        @Override public void cell(String cellRef, String formattedValue, XSSFComment comment) {
            if (formattedValue != null && !formattedValue.isBlank())
                row.add(formattedValue.trim());
        }
    }
}
