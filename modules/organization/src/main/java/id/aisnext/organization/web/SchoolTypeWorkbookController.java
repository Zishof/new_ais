package id.aisnext.organization.web;

import id.aisnext.organization.application.SchoolTypeImportResult;
import id.aisnext.organization.application.SchoolTypeService;
import id.aisnext.organization.domain.SchoolTypeConflictException;
import id.aisnext.organization.domain.SchoolTypeNotFoundException;
import id.aisnext.organization.domain.SchoolTypeValidationException;
import id.aisnext.organization.infrastructure.SchoolTypeWorkbook;
import id.aisnext.tenant.api.WriteOwnershipDeniedException;
import java.io.IOException;
import java.security.Principal;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** HTTP boundary for bounded `.xlsx` school-type export and atomic import. */
@Controller
public class SchoolTypeWorkbookController {
    private static final long MAX_UPLOAD_BYTES = 1_048_576L;

    private final SchoolTypeService service;
    private final SchoolTypeWorkbook workbook;

    /**
     * Creates the workbook controller.
     *
     * @param service school-type application service
     * @param workbook bounded Office Open XML codec
     */
    public SchoolTypeWorkbookController(SchoolTypeService service, SchoolTypeWorkbook workbook) {
        this.service = service;
        this.workbook = workbook;
    }

    /**
     * Downloads a current full catalogue with identifiers and concurrency tokens.
     *
     * @return attachment response containing an `.xlsx` workbook
     */
    @GetMapping({"/school-types/export.xlsx", "/api/v1/school-types/export.xlsx"})
    @ResponseBody
    public ResponseEntity<byte[]> exportWorkbook() {
        byte[] bytes = workbook.write(service.exportRows());
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("jenis-sekolah.xlsx").build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(bytes.length)
                .body(bytes);
    }

    /**
     * Imports a workbook from the browser UI and reports the atomic outcome as a flash message.
     *
     * @param file uploaded `.xlsx` file
     * @param principal authenticated handoff principal
     * @param redirect flash-message destination
     * @return redirect to the catalogue
     */
    @PostMapping("/school-types/import")
    @PreAuthorize("hasAuthority('LEGACY_MENU_881247_CREATE') and "
            + "hasAuthority('LEGACY_MENU_881247_UPDATE') and hasAuthority('LEGACY_MENU_881247_DELETE')")
    public String importWorkbook(@RequestParam("file") MultipartFile file, Principal principal,
                                 RedirectAttributes redirect) {
        try {
            SchoolTypeImportResult result = service.importRows(read(file), principal.getName());
            redirect.addFlashAttribute("success", "Impor berhasil: " + result.created()
                    + " dibuat, " + result.updated() + " diperbarui");
        } catch (SchoolTypeValidationException | SchoolTypeConflictException
                 | SchoolTypeNotFoundException | WriteOwnershipDeniedException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/school-types";
    }

    /**
     * Imports a workbook through the versioned API under the same strict bulk privilege gate.
     *
     * @param file uploaded `.xlsx` file
     * @param principal authenticated handoff principal
     * @return committed create/update counters
     */
    @PostMapping("/api/v1/school-types/import")
    @PreAuthorize("hasAuthority('LEGACY_MENU_881247_CREATE') and "
            + "hasAuthority('LEGACY_MENU_881247_UPDATE') and hasAuthority('LEGACY_MENU_881247_DELETE')")
    @ResponseBody
    public SchoolTypeImportResult importWorkbookApi(@RequestParam("file") MultipartFile file,
                                                      Principal principal) {
        return service.importRows(read(file), principal.getName());
    }

    /**
     * Validates the HTTP-level file bound and parses the caller-owned upload stream.
     *
     * @param file uploaded workbook
     * @return parsed workbook rows
     * @throws SchoolTypeValidationException when absent, oversized, unreadable, or structurally invalid
     */
    private java.util.List<id.aisnext.organization.application.SchoolTypeImportRow> read(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new SchoolTypeValidationException("File .xlsx wajib dipilih");
        if (file.getSize() > MAX_UPLOAD_BYTES) {
            throw new SchoolTypeValidationException("Ukuran file .xlsx maksimal 1 MiB");
        }
        try {
            return workbook.read(file.getInputStream());
        } catch (IOException exception) {
            throw new SchoolTypeValidationException("File .xlsx tidak dapat dibaca");
        }
    }
}
