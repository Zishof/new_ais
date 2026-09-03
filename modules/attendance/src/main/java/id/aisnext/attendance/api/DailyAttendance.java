package id.aisnext.attendance.api;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Immutable read model for one employee and the latest attendance row on a selected date.
 *
 * @param employeeId legacy employee primary key
 * @param employeeNumber normalized NIP/code, possibly blank
 * @param employeeName normalized display name, possibly blank
 * @param employeeActive legacy null-active values normalized to true
 * @param date selected local calendar date
 * @param attendanceId latest matching legacy attendance ID, or null when unrecorded
 * @param statusCode legacy attendance-status code, possibly blank
 * @param statusName legacy attendance-status name, possibly blank
 * @param checkIn recorded arrival time, or null
 * @param checkOut recorded departure time, or null
 * @param note legacy attendance note, or null
 * @param recordState whether a matching row exists
 */
public record DailyAttendance(long employeeId, String employeeNumber, String employeeName,
                              boolean employeeActive, LocalDate date, Long attendanceId,
                              String statusCode, String statusName, LocalTime checkIn,
                              LocalTime checkOut, String note, AttendanceRecordState recordState) {
}
