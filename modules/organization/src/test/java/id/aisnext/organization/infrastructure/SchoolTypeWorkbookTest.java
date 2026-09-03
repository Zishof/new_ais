package id.aisnext.organization.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import id.aisnext.organization.api.SchoolType;
import id.aisnext.organization.api.SchoolTypeCommand;
import id.aisnext.organization.domain.SchoolTypeValidationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

/** Verifies the bounded workbook format and formula rejection behavior. */
class SchoolTypeWorkbookTest {
    private final SchoolTypeWorkbook workbook = new SchoolTypeWorkbook();

    /** Creates the workbook test fixture. */
    SchoolTypeWorkbookTest() {
    }

    /** Confirms exported IDs, fields, active state, and version tokens survive a round trip. */
    @Test
    void roundTripsExportedRowsAsUpdates() {
        SchoolType value = new SchoolType(7L, "SMA Baru", 33L, "SMA", "UAT",
                true, "Admin", "admin", LocalDateTime.of(2026, 9, 4, 2, 0), "opaque-version");

        var imported = workbook.read(new ByteArrayInputStream(workbook.write(List.of(value))));

        assertThat(imported).hasSize(1);
        assertThat(imported.getFirst().id()).isEqualTo(7L);
        assertThat(imported.getFirst().versionToken()).isEqualTo("opaque-version");
        assertThat(imported.getFirst().command())
                .isEqualTo(new SchoolTypeCommand("SMA Baru", 33L, "UAT", true));
    }

    /**
     * Confirms a formula in any imported data cell is rejected without evaluation.
     *
     * @throws IOException when the malicious workbook fixture cannot be serialized
     */
    @Test
    void rejectsFormulaCells() throws IOException {
        byte[] malicious = workbookWithFormula();

        assertThatThrownBy(() -> workbook.read(new ByteArrayInputStream(malicious)))
                .isInstanceOf(SchoolTypeValidationException.class)
                .hasMessageContaining("Formula tidak diizinkan");
    }

    /**
     * Builds a structurally valid workbook whose name cell contains a formula.
     *
     * @return serialized malicious fixture bytes
     * @throws IOException when POI cannot serialize the fixture
     */
    private static byte[] workbookWithFormula() throws IOException {
        try (XSSFWorkbook source = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = source.createSheet("Jenis Sekolah");
            Row header = sheet.createRow(0);
            String[] headers = {"ID", "Nama", "Jenjang ID", "Keterangan", "Aktif", "Versi (jangan diubah)"};
            for (int index = 0; index < headers.length; index++) {
                header.createCell(index, CellType.STRING).setCellValue(headers[index]);
            }
            Row row = sheet.createRow(1);
            row.createCell(1).setCellFormula("1+1");
            row.createCell(2, CellType.NUMERIC).setCellValue(33);
            row.createCell(4, CellType.BOOLEAN).setCellValue(true);
            source.write(output);
            return output.toByteArray();
        }
    }
}
