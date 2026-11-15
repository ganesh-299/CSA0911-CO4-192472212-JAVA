package Task06;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * RegistrationService - Service layer orchestrating business rules,
 * input validation, duplicate enforcement, CSV reporting, and DAO interactions.
 */
public class RegistrationService {

    private final RegistrationDAO dao;

    public RegistrationService() {
        this.dao = new RegistrationDAO();
    }

    public RegistrationDAO getDao() {
        return dao;
    }

    /**
     * Validates student registration input fields.
     * @return Error message string if invalid, or null if completely valid.
     */
    public String validateInputs(String studentId, String studentName, String eventName) {
        if (studentId == null || studentId.trim().isEmpty()) {
            return "Student ID cannot be empty.";
        }
        if (!studentId.trim().matches("^[a-zA-Z0-9\\-]+$")) {
            return "Student ID must contain alphanumeric characters and hyphens only.";
        }
        if (studentName == null || studentName.trim().isEmpty()) {
            return "Student Name cannot be empty.";
        }
        if (studentName.trim().length() < 2) {
            return "Student Name must be at least 2 characters.";
        }
        if (eventName == null || eventName.trim().isEmpty()) {
            return "Please select a valid event.";
        }
        return null;
    }

    /**
     * Registers a student for an event.
     * Checks for duplicate registration.
     */
    public boolean register(String studentId, String studentName, String eventName) throws SQLException {
        studentId = studentId.trim();
        studentName = studentName.trim();
        eventName = eventName.trim();

        if (dao.isDuplicate(studentId, eventName)) {
            return false; // Duplicate found
        }
        return dao.registerStudent(studentId, studentName, eventName);
    }

    /**
     * Fetches paginated slice of registrations.
     */
    public List<RegistrationDAO.Registration> getPage(String eventFilter, int page, int pageSize) throws SQLException {
        return dao.getRegistrationsPaginated(eventFilter, page, pageSize);
    }

    /**
     * Retrieves total registration count for pagination calculation.
     */
    public int getTotalCount(String eventFilter) throws SQLException {
        return dao.getTotalCount(eventFilter);
    }

    /**
     * Computes total pages given count and page size.
     */
    public int calculateTotalPages(int totalCount, int pageSize) {
        if (totalCount <= 0) return 1;
        return (int) Math.ceil((double) totalCount / pageSize);
    }

    /**
     * Exports registrations matching the filter to a CSV file.
     */
    public int exportToCSV(File destinationFile, String eventFilter) throws IOException, SQLException {
        List<RegistrationDAO.Registration> records = dao.getAllForExport(eventFilter);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(destinationFile))) {
            // Write CSV Header
            writer.write("Registration_ID,Student_ID,Student_Name,Event_Name,Registration_Date");
            writer.newLine();

            // Write Records
            for (RegistrationDAO.Registration r : records) {
                String line = String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\"",
                        r.getId(),
                        escapeCsv(r.getStudentId()),
                        escapeCsv(r.getStudentName()),
                        escapeCsv(r.getEventName()),
                        escapeCsv(r.getRegistrationDate()));
                writer.write(line);
                writer.newLine();
            }
        }
        return records.size();
    }

    private String escapeCsv(String text) {
        if (text == null) return "";
        return text.replace("\"", "\"\"");
    }
}
