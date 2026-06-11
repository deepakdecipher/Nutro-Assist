package it.neutro.assist.dietplan;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses a nutritionist diet template Excel file.
 *
 * Expected columns (row 1 = header, data starts at row 2):
 * A: Day (integer 1-N)
 * B: Meal Type (BREAKFAST / MORNING_SNACK / LUNCH / EVENING_SNACK / DINNER)
 * C: Meal Name
 * D: Description (optional)
 * E: Calories (integer)
 * F: Protein g (decimal)
 * G: Carbs g (decimal)
 * H: Fat g (decimal)
 */
@Component
public class DietTemplateExcelParser {

    public List<ParsedRow> parse(MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<ParsedRow> rows = new ArrayList<>();
            int displayOrder = 0;
            int prevDay = -1;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                int day = (int) numericCell(row, 0);
                if (day != prevDay) {
                    displayOrder = 0;
                    prevDay = day;
                }

                MealType mealType = MealType.valueOf(stringCell(row, 1).toUpperCase().replace(" ", "_"));
                String mealName  = stringCell(row, 2);
                String desc      = stringCell(row, 3);
                int calories     = (int) numericCell(row, 4);
                double protein   = numericCell(row, 5);
                double carbs     = numericCell(row, 6);
                double fat       = numericCell(row, 7);

                rows.add(new ParsedRow(day, mealType, mealName, desc, calories, protein, carbs, fat, displayOrder++));
            }
            return rows;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid meal type in Excel file. Use: BREAKFAST, MORNING_SNACK, LUNCH, EVENING_SNACK, DINNER");
        }
    }

    private String stringCell(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            default -> "";
        };
    }

    private double numericCell(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return 0;
        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING -> {
                try { yield Double.parseDouble(cell.getStringCellValue().trim()); }
                catch (NumberFormatException e) { yield 0; }
            }
            default -> 0;
        };
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < 3; i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null && cell.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }

    public record ParsedRow(int day, MealType mealType, String mealName, String description,
                             int calories, double proteinG, double carbsG, double fatG, int displayOrder) {}
}
