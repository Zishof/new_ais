package id.aisnext.academic.web;

import id.aisnext.academic.application.StudentDirectoryService;
import id.aisnext.security.api.HandoffPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Accessible server-rendered boundary for the minimized school-student directory. */
@Controller
public class StudentDirectoryPageController {
    private final StudentDirectoryService service;

    /**
     * Creates the student directory page controller.
     *
     * @param service validated student directory application boundary
     */
    public StudentDirectoryPageController(StudentDirectoryService service) {
        this.service = service;
    }

    /**
     * Renders one role-scoped page while preserving its filter and paging state.
     *
     * @param principal authenticated handoff identity carrying the active role
     * @param page zero-based page index
     * @param size requested page size from 1 through 100
     * @param query literal student-name or student-number fragment
     * @param model template model receiving the page and preserved filter
     * @return student directory template name
     */
    @GetMapping("/academic/students")
    public String list(@AuthenticationPrincipal HandoffPrincipal principal,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "25") int size,
                       @RequestParam(name = "q", defaultValue = "") String query,
                       Model model) {
        model.addAttribute("result", service.findStudents(principal.activeRoleId(), page, size, query));
        model.addAttribute("query", query == null ? "" : query.trim());
        return "academic/students";
    }
}
