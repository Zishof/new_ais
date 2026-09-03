package id.aisnext.academic.web;

import id.aisnext.academic.api.StudentDirectoryEntry;
import id.aisnext.academic.application.StudentDirectoryService;
import id.aisnext.kernel.api.PageResult;
import id.aisnext.security.api.HandoffPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Versioned JSON boundary for the role-scoped, read-only student directory. */
@RestController
@RequestMapping("/api/v1/academic/students")
public class StudentDirectoryApiController {
    private final StudentDirectoryService service;

    /**
     * Creates the student directory API controller.
     *
     * @param service validated student directory application boundary
     */
    public StudentDirectoryApiController(StudentDirectoryService service) {
        this.service = service;
    }

    /**
     * Returns one minimized student page inside the active legacy role's data scope.
     *
     * @param principal authenticated handoff identity carrying the active role
     * @param page zero-based page index
     * @param size requested page size from 1 through 100
     * @param query literal student-name or student-number fragment
     * @return immutable page containing no excluded personal columns
     */
    @GetMapping
    public PageResult<StudentDirectoryEntry> list(
            @AuthenticationPrincipal HandoffPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(name = "q", defaultValue = "") String query) {
        return service.findStudents(principal.activeRoleId(), page, size, query);
    }
}
