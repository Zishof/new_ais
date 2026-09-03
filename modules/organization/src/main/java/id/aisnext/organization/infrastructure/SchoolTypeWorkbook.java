package id.aisnext.organization.infrastructure;

import id.aisnext.organization.api.SchoolType;
import id.aisnext.organization.api.SchoolTypeCommand;
import id.aisnext.organization.application.SchoolTypeImportRow;
import id.aisnext.organization.domain.SchoolTypeValidationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

/**
 * Bounded Apache POI codec for the documented school-type `.xlsx` interchange format.
 *
 * <p>The codec accepts one worksheet, at most 1,000 data rows, six fixed columns, and no formula
 * cells. HTTP upload size is independently capped at one MiB.</p>
 */
@Component
public class SchoolTypeWorkbook {
    /** Maximum data rows accepted in one atomic import. */
    public static final int MAX_DATA_ROWS = 1_000;
    private static final List<String> HEADERS = List.of(
            "ID", "Nama", "Jenjang ID", "Keterangan", "Aktif", "Versi (jangan diubah)");
    private static final int MAX_CELL_CHARACTERS = 255;

    /** Creates the stateless workbook codec. */
    public SchoolTypeWorkbook() {
    }

    /**
     * Writes the full bounded export with identifiers and optimistic concurrency tokens.
     *
     * @param values school types in deterministic display order
     * @return complete Office Open XML workbook bytes
     * @throws IllegalStateException when POI cannot serialize the in-memory workbook
     */
    public byte[] write(List<SchoolType> values) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Jenis Sekolah");
            Row header = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            for (int index = 0; index < HEADERS.size(); index++) {
                Cell cell = header.createCell(index, CellType.STRING);
                cell.setCellValue(HEADERS.get(index));
                cell.setCellStyle(headerStyle);
            }
            int rowIndex = 1;
            for (SchoolType value : values) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0, CellType.NUMERIC).setCellValue(value.id());
                row.createCell(1, CellType.STRING).setCellValue(value.name());
                if (value.levelId() != null) row.createCell(2, CellType.NUMERIC).setCellValue(value.levelId());
                row.createCell(3, CellType.STRING).setCellValue(value.description() == null ? "" : value.description());
                row.createCell(4, CellType.BOOLEAN).setCellValue(value.active());
                row.createCell(5, CellType.STRING).setCellValue(value.versionToken());
            }
            sheet.createFreezePane(0, 1);
            int[] widths = {14, 28, 16, 42, 12, 48};
            for (int index = 0; index < widths.length; index++) sheet.setColumnWidth(index, widths[index] * 256);
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Workbook jenis sekolah tidak dapat dibuat", exception);
        }
    }

    /**
     * Parses a fixed-shape workbook and rejects formulas, extra sheets, and excessive rows.
     *
     * @param input trusted-size request stream; ownership remains with the caller
     * @return immutable imported rows ready for transactional business validation
     * @throws SchoolTypeValidationException when workbook structure or a cell value is invalid
     */
    public List<SchoolTypeImportRow> read(InputStream input) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(input)) {
            if (workbook.getNumberOfSheets() != 1) {
                throw new SchoolTypeValidationException("Workbook harus berisi tepat satu sheet");
            }
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getLastRowNum() > MAX_DATA_ROWS) {
                throw new SchoolTypeValidationException("Workbook maksimal " + MAX_DATA_ROWS + " baris data");
            }
            validateHeader(sheet.getRow(0));
            List<SchoolTypeImportRow> rows = new ArrayList<>();
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || blank(row)) continue;
                Long id = optionalLong(row, 0, rowIndex);
                String name = text(row, 1, rowIndex, true);
                Long levelId = requiredLong(row, 2, rowIndex);
                String description = text(row, 3, rowIndex, false);
                boolean active = bool(row, 4, rowIndex);
                String version = text(row, 5, rowIndex, false);
                if (id != null && version.isBlank()) {
                    throw invalid(rowIndex, "Versi wajib diisi untuk baris update");
                }
                rows.add(new SchoolTypeImportRow(id, version,
                        new SchoolTypeCommand(name, levelId, description, active)));
            }
            if (rows.isEmpty()) throw new SchoolTypeValidationException("Workbook tidak memiliki baris data");
            return List.copyOf(rows);
        } catch (SchoolTypeValidationException exception) {
            throw exception;
        } catch (IOException | RuntimeException exception) {
            throw new SchoolTypeValidationException("Workbook .xlsx tidak valid atau rusak");
        }
    }

    /**
     * Validates the exact six-column interchange header.
     *
     * @param row first worksheet row
     * @throws SchoolTypeValidationException when a header is missing or changed
     */
    private static void validateHeader(Row row) {
        if (row == null) throw new SchoolTypeValidationException("Header workbook tidak ditemukan");
        for (int index = 0; index < HEADERS.size(); index++) {
            String actual = rawText(row.getCell(index));
            if (!HEADERS.get(index).equals(actual)) {
                throw new SchoolTypeValidationException(
                        "Header kolom " + (index + 1) + " harus '" + HEADERS.get(index) + "'");
            }
        }
    }

    /**
     * Reports whether every supported cell in a data row is blank.
     *
     * @param row candidate data row
     * @return whether all six cells are absent or blank
     */
    private static boolean blank(Row row) {
        for (int index = 0; index < HEADERS.size(); index++) {
            if (!rawText(row.getCell(index)).isBlank()) return false;
        }
        return true;
    }

    /**
     * Reads bounded text and rejects formula cells.
     *
     * @param row source row
     * @param column zero-based column index
     * @param rowIndex zero-based row index for diagnostics
     * @param required whether blank text is invalid
     * @return formatted trimmed text
     */
    private static String text(Row row, int column, int rowIndex, boolean required) {
        Cell cell = row.getCell(column);
        rejectFormula(cell, rowIndex, column);
        String value = rawText(cell).trim();
        if (required && value.isEmpty()) throw invalid(rowIndex, HEADERS.get(column) + " wajib diisi");
        if (value.length() > MAX_CELL_CHARACTERS) {
            throw invalid(rowIndex, HEADERS.get(column) + " maksimal 255 karakter");
        }
        return value;
    }

    /**
     * Reads an optional positive integral identifier.
     *
     * @param row source row
     * @param column zero-based column index
     * @param rowIndex zero-based row index for diagnostics
     * @return positive identifier or null for a create row
     */
    private static Long optionalLong(Row row, int column, int rowIndex) {
        Cell cell = row.getCell(column);
        rejectFormula(cell, rowIndex, column);
        String value = rawText(cell).trim();
        if (value.isEmpty()) return null;
        try {
            long parsed = cell != null && cell.getCellType() == CellType.NUMERIC
                    ? exactLong(cell.getNumericCellValue()) : Long.parseLong(value);
            if (parsed <= 0) throw new NumberFormatException("not positive");
            return parsed;
        } catch (NumberFormatException exception) {
            throw invalid(rowIndex, HEADERS.get(column) + " harus bilangan bulat positif");
        }
    }

    /**
     * Reads a required positive integral identifier.
     *
     * @param row source row
     * @param column zero-based column index
     * @param rowIndex zero-based row index for diagnostics
     * @return positive identifier
     */
    private static Long requiredLong(Row row, int column, int rowIndex) {
        Long value = optionalLong(row, column, rowIndex);
        if (value == null) throw invalid(rowIndex, HEADERS.get(column) + " wajib diisi");
        return value;
    }

    /**
     * Reads common Indonesian/English boolean spellings with active as the blank default.
     *
     * @param row source row
     * @param column zero-based column index
     * @param rowIndex zero-based row index for diagnostics
     * @return parsed active state
     */
    private static boolean bool(Row row, int column, int rowIndex) {
        Cell cell = row.getCell(column);
        rejectFormula(cell, rowIndex, column);
        if (cell != null && cell.getCellType() == CellType.BOOLEAN) return cell.getBooleanCellValue();
        String value = rawText(cell).trim().toLowerCase(Locale.ROOT);
        return switch (value) {
            case "", "true", "ya", "aktif", "1" -> true;
            case "false", "tidak", "nonaktif", "0" -> false;
            default -> throw invalid(rowIndex, "Aktif harus true/false, ya/tidak, aktif/nonaktif, atau 1/0");
        };
    }

    /**
     * Converts an exactly integral finite spreadsheet number to long.
     *
     * @param value numeric cell value
     * @return exact long value
     * @throws NumberFormatException when fractional, non-finite, or outside long range
     */
    private static long exactLong(double value) {
        if (!Double.isFinite(value) || value != Math.rint(value) || value < 1 || value > Long.MAX_VALUE) {
            throw new NumberFormatException("not an exact positive long");
        }
        return (long) value;
    }

    /**
     * Rejects formulas so workbook imports cannot evaluate attacker-controlled expressions.
     *
     * @param cell candidate cell
     * @param rowIndex zero-based row index
     * @param column zero-based column index
     */
    private static void rejectFormula(Cell cell, int rowIndex, int column) {
        if (cell != null && cell.getCellType() == CellType.FORMULA) {
            throw invalid(rowIndex, "Formula tidak diizinkan pada kolom " + HEADERS.get(column));
        }
    }

    /**
     * Formats a cell without evaluating formulas.
     *
     * @param cell nullable workbook cell
     * @return locale-stable display text or empty text
     */
    private static String rawText(Cell cell) {
        return cell == null ? "" : new DataFormatter(Locale.ROOT).formatCellValue(cell);
    }

    /**
     * Creates a one-based row diagnostic for operator correction.
     *
     * @param zeroBasedRow zero-based worksheet row index
     * @param message specific validation explanation
     * @return typed validation exception
     */
    private static SchoolTypeValidationException invalid(int zeroBasedRow, String message) {
        return new SchoolTypeValidationException("Baris " + (zeroBasedRow + 1) + ": " + message);
    }
}
