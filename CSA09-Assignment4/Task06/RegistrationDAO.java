package Task06;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RegistrationDAO - Data Access Object for Event Registrations.
 * Executes SQL queries via JDBC with PreparedStatement to prevent SQL injection.
 * Implements pagination via SQL LIMIT and OFFSET.
 * Includes graceful in-memory failover for offline lab environments.
 */
public class RegistrationDAO {

    public static class Registration {
        private int id;
        private String studentId;
        private String studentName;
        private String eventName;
        private String registrationDate;

        public Registration(int id, String studentId, String studentName, String eventName, String registrationDate) {
            this.id = id;
            this.studentId = studentId;
            this.studentName = studentName;
            this.eventName = eventName;
            this.registrationDate = registrationDate;
        }

        public int getId() { return id; }
        public String getStudentId() { return studentId; }
        public String getStudentName() { return studentName; }
        public String getEventName() { return eventName; }
        public String getRegistrationDate() { return registrationDate; }
    }

    // In-memory fallback dataset for offline/mock demonstration
    private static final List<Registration> mockDb = new CopyOnWriteArrayList<>();
    private static final AtomicInteger mockIdCounter = new AtomicInteger(100);

    static {
        // Seed initial mock records in case MySQL is offline
        mockDb.add(new Registration(1, "21CS001", "Alice Johnson", "Hackathon 2026", "2026-11-10 09:30:00"));
        mockDb.add(new Registration(2, "21CS002", "Bob Smith", "AI & Machine Learning Workshop", "2026-11-10 10:15:00"));
        mockDb.add(new Registration(3, "21IT015", "Charlie Brown", "Web Dev Bootcamp", "2026-11-11 11:00:00"));
        mockDb.add(new Registration(4, "21EC030", "David Miller", "Robotics Expo", "2026-11-11 14:20:00"));
        mockDb.add(new Registration(5, "21ME045", "Emma Watson", "Hackathon 2026", "2026-11-12 16:45:00"));
        mockDb.add(new Registration(6, "21CS010", "Frank Castle", "Competitive Coding Marathon", "2026-11-13 10:00:00"));
        mockDb.add(new Registration(7, "21IT022", "Grace Hopper", "AI & Machine Learning Workshop", "2026-11-13 11:30:00"));
        mockDb.add(new Registration(8, "21CS033", "Henry Cavill", "Web Dev Bootcamp", "2026-11-14 13:10:00"));
        mockDb.add(new Registration(9, "21EC044", "Iris West", "Robotics Expo", "2026-11-14 15:40:00"));
        mockDb.add(new Registration(10, "21CS055", "Jack Ryan", "Hackathon 2026", "2026-11-15 09:00:00"));
        mockDb.add(new Registration(11, "21IT066", "Karen Page", "Competitive Coding Marathon", "2026-11-15 10:20:00"));
        mockDb.add(new Registration(12, "21ME077", "Luke Cage", "Robotics Expo", "2026-11-15 11:45:00"));
    }

    private boolean useMock = false;

    public RegistrationDAO() {
        this.useMock = !DatabaseConnection.testConnection();
    }

    public boolean isUsingMockDatabase() {
        return useMock;
    }

    public void setUseMock(boolean mock) {
        this.useMock = mock;
    }

    /**
     * Checks if a student is already registered for an event (duplicate prevention).
     */
    public boolean isDuplicate(String studentId, String eventName) throws SQLException {
        if (useMock) {
            for (Registration r : mockDb) {
                if (r.getStudentId().equalsIgnoreCase(studentId) && r.getEventName().equalsIgnoreCase(eventName)) {
                    return true;
                }
            }
            return false;
        }

        String sql = "SELECT COUNT(*) FROM registrations WHERE student_id = ? AND event_name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, eventName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            useMock = true; // Failover to mock
            return isDuplicate(studentId, eventName);
        }
        return false;
    }

    /**
     * Registers a student for an event.
     */
    public boolean registerStudent(String studentId, String studentName, String eventName) throws SQLException {
        if (isDuplicate(studentId, eventName)) {
            return false;
        }

        if (useMock) {
            String dateStr = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            mockDb.add(0, new Registration(mockIdCounter.incrementAndGet(), studentId, studentName, eventName, dateStr));
            return true;
        }

        String sql = "INSERT INTO registrations (student_id, student_name, event_name) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, studentName);
            ps.setString(3, eventName);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            useMock = true; // Failover to mock
            return registerStudent(studentId, studentName, eventName);
        }
    }

    /**
     * Fetches a paginated slice of registrations filtered by event.
     * Uses SQL LIMIT and OFFSET for maximum database efficiency.
     * 
     * @param eventFilter Event name or "All Events"
     * @param page Zero-based page index
     * @param pageSize Number of records per page
     */
    public List<Registration> getRegistrationsPaginated(String eventFilter, int page, int pageSize) throws SQLException {
        int offset = page * pageSize;

        if (useMock) {
            List<Registration> filtered = new ArrayList<>();
            for (Registration r : mockDb) {
                if ("All Events".equalsIgnoreCase(eventFilter) || r.getEventName().equalsIgnoreCase(eventFilter)) {
                    filtered.add(r);
                }
            }
            int start = Math.min(offset, filtered.size());
            int end = Math.min(offset + pageSize, filtered.size());
            return new ArrayList<>(filtered.subList(start, end));
        }

        List<Registration> list = new ArrayList<>();
        boolean hasFilter = !"All Events".equalsIgnoreCase(eventFilter);
        String sql = hasFilter
                ? "SELECT id, student_id, student_name, event_name, registration_date FROM registrations WHERE event_name = ? ORDER BY id DESC LIMIT ? OFFSET ?"
                : "SELECT id, student_id, student_name, event_name, registration_date FROM registrations ORDER BY id DESC LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int paramIdx = 1;
            if (hasFilter) {
                ps.setString(paramIdx++, eventFilter);
            }
            ps.setInt(paramIdx++, pageSize);
            ps.setInt(paramIdx, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Registration(
                            rs.getInt("id"),
                            rs.getString("student_id"),
                            rs.getString("student_name"),
                            rs.getString("event_name"),
                            rs.getString("registration_date")
                    ));
                }
            }
        } catch (SQLException e) {
            useMock = true;
            return getRegistrationsPaginated(eventFilter, page, pageSize);
        }
        return list;
    }

    /**
     * Counts total registrations matching the current filter (needed for pagination count).
     */
    public int getTotalCount(String eventFilter) throws SQLException {
        if (useMock) {
            int count = 0;
            for (Registration r : mockDb) {
                if ("All Events".equalsIgnoreCase(eventFilter) || r.getEventName().equalsIgnoreCase(eventFilter)) {
                    count++;
                }
            }
            return count;
        }

        boolean hasFilter = !"All Events".equalsIgnoreCase(eventFilter);
        String sql = hasFilter
                ? "SELECT COUNT(*) FROM registrations WHERE event_name = ?"
                : "SELECT COUNT(*) FROM registrations";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (hasFilter) {
                ps.setString(1, eventFilter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            useMock = true;
            return getTotalCount(eventFilter);
        }
        return 0;
    }

    /**
     * Fetches all matching registrations for CSV export.
     */
    public List<Registration> getAllForExport(String eventFilter) throws SQLException {
        if (useMock) {
            List<Registration> list = new ArrayList<>();
            for (Registration r : mockDb) {
                if ("All Events".equalsIgnoreCase(eventFilter) || r.getEventName().equalsIgnoreCase(eventFilter)) {
                    list.add(r);
                }
            }
            return list;
        }

        List<Registration> list = new ArrayList<>();
        boolean hasFilter = !"All Events".equalsIgnoreCase(eventFilter);
        String sql = hasFilter
                ? "SELECT id, student_id, student_name, event_name, registration_date FROM registrations WHERE event_name = ? ORDER BY id DESC"
                : "SELECT id, student_id, student_name, event_name, registration_date FROM registrations ORDER BY id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (hasFilter) {
                ps.setString(1, eventFilter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Registration(
                            rs.getInt("id"),
                            rs.getString("student_id"),
                            rs.getString("student_name"),
                            rs.getString("event_name"),
                            rs.getString("registration_date")
                    ));
                }
            }
        } catch (SQLException e) {
            useMock = true;
            return getAllForExport(eventFilter);
        }
        return list;
    }
}
