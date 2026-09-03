package id.aisnext.academic.web;

import static id.aisnext.academic.api.StudentDirectoryAuthorities.READ_STUDENTS;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Publishes the server-derived student-directory capability to Thymeleaf navigation. */
@ControllerAdvice
public class StudentDirectoryNavigationAdvice {
    /** Creates the stateless student-directory capability contributor. */
    public StudentDirectoryNavigationAdvice() {
    }

    /**
     * Reports whether the current active role can browse the student directory.
     *
     * @param authentication current authentication, or null for anonymous traffic
     * @return whether legacy menu 887727 grants read access
     */
    @ModelAttribute("canReadStudentDirectory")
    public boolean canRead(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> READ_STUDENTS.equals(authority.getAuthority()));
    }
}
