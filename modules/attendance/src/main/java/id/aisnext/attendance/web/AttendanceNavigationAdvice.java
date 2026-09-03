package id.aisnext.attendance.web;

import static id.aisnext.attendance.api.AttendanceAuthorities.READ_DAILY;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Publishes server-derived daily attendance capability to Thymeleaf navigation. */
@ControllerAdvice
public class AttendanceNavigationAdvice {
    /** Creates the stateless capability model contributor. */
    public AttendanceNavigationAdvice() {
    }

    /**
     * Reports whether the current role can browse the daily attendance monitor.
     *
     * @param authentication current authentication, or null for anonymous traffic
     * @return whether legacy menu 10000269 grants read access
     */
    @ModelAttribute("canReadDailyAttendance")
    public boolean canRead(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> READ_DAILY.equals(authority.getAuthority()));
    }
}
