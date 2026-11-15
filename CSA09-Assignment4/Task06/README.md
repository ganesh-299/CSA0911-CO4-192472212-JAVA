# TASK 6: CAMPUS EVENT REGISTRATION APPLICATION (SWING + JDBC + 3-TIER ARCHITECTURE)

## 1. Aim
To design, architect, and implement an enterprise-grade Campus Event Registration desktop application using Java Swing and MySQL via JDBC. The system enforces strict separation of concerns via a 3-Tier Architecture (Presentation $\to$ Service $\to$ Data Access Layer), eliminates GUI freezing via `SwingWorker` background asynchronous execution, implements database pagination using SQL `LIMIT` and `OFFSET`, strictly prevents duplicate registrations, supports event filtering, and exports data to CSV.

---

## 2. Requirements
- **Language / Runtime:** Java Standard Edition (JDK 8+ / OpenJDK 26 tested)
- **GUI Toolkit:** Java Swing (`JFrame`, `JTable`, `DefaultTableModel`, `JScrollPane`, `JMenuBar`, `JMenu`, `JMenuItem`, `JComboBox`, `JTextField`, `JButton`, `JFileChooser`, `JOptionPane`)
- **Database & Connectivity:** MySQL 8.x + MySQL Connector/J (`mysql-connector-j-8.3.0.jar`)
- **Threading:** Java Concurrency via `javax.swing.SwingWorker`
- **Architecture Layers:**
  1. `EventRegistrationApp.java`: Presentation UI, event listeners, menu bars, table view.
  2. `RegistrationService.java`: Business validation, duplicate checking coordination, CSV generation.
  3. `RegistrationDAO.java`: SQL queries via `PreparedStatement`, pagination offset math, connection lifecycle.
  4. `DatabaseConnection.java`: JDBC DriverManager connection management.
  5. `schema.sql`: DDL for database, tables, unique constraints, and indexes.

---

## 3. 3-Tier Architecture & Class Diagram

```
+-------------------------------------------------------------------------------+
|                             PRESENTATION LAYER                                |
|                           EventRegistrationApp                                |
|  - Manages GUI components: Form, JTable, JMenuBar, Pagination Bar, Status Bar |
|  - Offloads long-running I/O to background threads using SwingWorker          |
+-------------------------------------------------------------------------------+
                                      |
                                      | calls
                                      v
+-------------------------------------------------------------------------------+
|                            BUSINESS SERVICE LAYER                             |
|                             RegistrationService                               |
|  - Validates inputs (Student ID regex, Name length, Event selection)         |
|  - Enforces duplicate prevention logic                                        |
|  - Computes pagination parameters: totalPages = ceil(count / pageSize)        |
|  - Implements RFC-4180 compliant CSV export                                   |
+-------------------------------------------------------------------------------+
                                      |
                                      | calls
                                      v
+-------------------------------------------------------------------------------+
|                           DATA ACCESS LAYER (DAO)                             |
|                               RegistrationDAO                                 |
|  - Executes parameterized SQL statements via PreparedStatement                |
|  - Queries: INSERT, SELECT ... LIMIT ? OFFSET ?, SELECT COUNT(*)              |
|  - Includes graceful in-memory mock failover if MySQL daemon is offline       |
+-------------------------------------------------------------------------------+
                                      |
                                      | utilizes
                                      v
+-------------------------------------------------------------------------------+
|                           INFRASTRUCTURE / DATABASE                           |
|                    DatabaseConnection  <-->  MySQL Database                   |
|  - URL: jdbc:mysql://localhost:3306/campus_events                             |
|  - Tables: registrations (id, student_id, student_name, event_name, date)     |
+-------------------------------------------------------------------------------+
```

---

## 4. The Critical Role of `SwingWorker` in Responsive GUIs

### The Single-Thread Rule in Swing:
Swing components are **not thread-safe**. All GUI rendering, user clicks, and keyboard strokes are processed sequentially on a single dedicated thread called the **Event Dispatch Thread (EDT)**.

### What Happens Without `SwingWorker`:
If a database query (e.g., executing a slow SQL query, connecting to a remote MySQL host, or exporting 5,000 records) is executed directly on the EDT:
1. The EDT is blocked waiting for database socket I/O.
2. The GUI stops responding to mouse clicks and window repaints.
3. The OS displays the spinning beachball (macOS) or marks the window as *"Not Responding"* (Windows).

### How `SwingWorker` Solves It:
`SwingWorker<T, V>` provides two distinct execution phases:
1. `doInBackground() -> T`:
   - Runs automatically on a **background worker thread** managed by an internal worker pool.
   - Executes heavy JDBC network queries, SQL pagination, and file writing.
   - **Never touches Swing GUI components.**
2. `done()`:
   - Automatically executed back on the **Event Dispatch Thread (EDT)** when `doInBackground()` completes.
   - Calls `get()` to safely retrieve the query result.
   - Safely updates Swing components (`tableModel.setRowCount(0)`, `lblStatus.setText(...)`).

---

## 5. Duplicate Registration Prevention

A core requirement is ensuring that the same student cannot register for the same event twice. This is enforced at two distinct levels:

1. **Database Layer (Hard Constraint):**
   ```sql
   CONSTRAINT uk_student_event UNIQUE (student_id, event_name)
   ```
   If a duplicate `INSERT` statement reaches MySQL, the InnoDB storage engine rejects the insertion and throws a `java.sql.SQLIntegrityConstraintViolationException`.
2. **Application / DAO Layer (Soft Pre-Check):**
   ```sql
   SELECT COUNT(*) FROM registrations WHERE student_id = ? AND event_name = ?
   ```
   Before attempting an insert, `isDuplicate(studentId, eventName)` is executed. If a match exists, a descriptive `JOptionPane.WARNING_MESSAGE` is presented immediately to the user without generating unhandled exceptions.

---

## 6. SQL Pagination: Loading All 100,000 Records vs Pagination

### The Naive Approach: Fetching All Records (`SELECT * FROM registrations`)
Imagine an enterprise campus database containing **100,000 registrations**:
- **Memory / Heap Exhaustion:** Constructing 100,000 Java `Registration` entity objects, plus `DefaultTableModel` row vectors, consumes $\approx 80-120 \text{ MB}$ of JVM heap. Under heavy concurrency, this rapidly triggers `java.lang.OutOfMemoryError: Java heap space`.
- **Network Latency:** Serializing and transmitting millions of bytes across the network socket causes significant multi-second latency on every screen refresh.
- **Rendering Overhead:** Passing 100,000 rows to `JTable` chokes the Swing layout manager and causes severe scrolling lag.

### The Paginated Approach: SQL `LIMIT` and `OFFSET`
Instead of pulling all records, the DAO fetches only the specific page slice requested by the user:
```sql
SELECT id, student_id, student_name, event_name, registration_date
FROM registrations
WHERE event_name = ?
ORDER BY id DESC
LIMIT ? OFFSET ?;
```
- **Math Formula:**
  - $\text{offset} = \text{currentPage} \times \text{pageSize}$
  - $\text{totalPages} = \lceil \frac{\text{totalCount}}{\text{pageSize}} \rceil$

### Comparison Matrix:

| Metric | Loading All 100,000 Records | SQL `LIMIT` & `OFFSET` Pagination |
|--------|----------------------------|-----------------------------------|
| **JVM Memory Allocated** | $\mathcal{O}(N)$ ($\approx 100 \text{ MB}$) | **$\mathcal{O}(K)$** where $K = 5$ or $10$ rows ($\approx 5 \text{ KB}$) |
| **Database Query Latency** | $\approx 1500 - 3000 \text{ ms}$ | **$\approx 2 - 5 \text{ ms}$** (utilizing B-Tree index) |
| **Network Bandwidth** | $\approx 15 \text{ MB}$ per refresh | **$< 1 \text{ KB}$** per page flip |
| **UI Responsiveness** | Freezes UI for several seconds | Instantaneous, smooth 60 FPS page flips |

---

## 7. CSV Export Implementation

The CSV export function (`exportToCSV`) streams records directly from the database into a standard comma-separated text file following RFC-4180 rules:
- Any field containing commas, quotes, or whitespace is safely wrapped in double quotes.
- Internal double quotes are escaped as `""`.
- Handled inside a background `SwingWorker` so exporting thousands of rows does not stall the user interface.

Sample Export Output (`event_registrations.csv`):
```csv
Registration_ID,Student_ID,Student_Name,Event_Name,Registration_Date
12,"21ME077","Luke Cage","Robotics Expo","2026-11-15 11:45:00"
11,"21IT066","Karen Page","Competitive Coding Marathon","2026-11-15 10:20:00"
10,"21CS055","Jack Ryan","Hackathon 2026","2026-11-15 09:00:00"
```

---

## 8. MySQL Database Setup Instructions

To configure a local MySQL database for this application:

1. Open your terminal or MySQL command line client:
   ```bash
   mysql -u root -p
   ```
2. Execute the included SQL schema file:
   ```sql
   SOURCE Task06/schema.sql;
   ```
3. Or execute directly from shell:
   ```bash
   mysql -u root -p < Task06/schema.sql
   ```
4. Verify table and initial data:
   ```sql
   USE campus_events;
   SELECT * FROM registrations LIMIT 5;
   ```

> **Note on Standalone / Offline Execution:** If MySQL is not running on your machine, `EventRegistrationApp` automatically detects this and gracefully falls back to an **In-Memory Thread-Safe Mock Catalog**. All features—registration, duplicate rejection, pagination, filtering, and CSV export—remain 100% operational for evaluation and screenshot capture!

---

## 9. Test Cases

| Test ID | Test Scenario | Action / Input Data | Expected Result | Pass/Fail |
|---------|---------------|---------------------|-----------------|-----------|
| **TC-01** | Normal Registration | ID: `21CS099`, Name: `Diana Prince`, Event: `Hackathon 2026` | "Registration Successful!" popup; record added to page 1 of table; status updated. | PASS |
| **TC-02** | Duplicate Registration | Register `21CS099` for `Hackathon 2026` a second time | "Duplicate Registration! Student ID '21CS099' is already registered for 'Hackathon 2026'." Rejected. | PASS |
| **TC-03** | Blank Student ID | Leave ID empty, Name: `Bruce Wayne` | Warning: "Student ID cannot be empty." Focus restored to ID box. | PASS |
| **TC-04** | Short Student Name | ID: `21IT088`, Name: `A` (< 2 chars) | Warning: "Student Name must be at least 2 characters." | PASS |
| **TC-05** | Event Filtering | Filter by: "Robotics Expo" | Table displays only Robotics Expo students; pagination indicator updates total count. | PASS |
| **TC-06** | Pagination Page Flip | Click "Next >" button | Advances to Page 2; records 6 through 10 displayed; "Prev" button becomes active. | PASS |
| **TC-07** | Change Page Size | Select Page Size: `10` | Table displays 10 records per page; total pages recalculates from 3 down to 2. | PASS |
| **TC-08** | CSV Export | Click "Export to CSV" $\to$ Save file | File dialog opens; `event_registrations.csv` written to disk; confirmation shown. | PASS |

---

## 10. Compilation, Execution & Screenshot Instructions

### Compilation:
```bash
javac -cp "Task06/mysql-connector-j-8.3.0.jar:." Task06/*.java
```

### Execution:
```bash
# Standard Execution (supports both live MySQL and automatic in-memory failover):
java -cp "Task06/mysql-connector-j-8.3.0.jar:." Task06.EventRegistrationApp

# Optional: Run with explicit custom database credentials:
java -Ddb.host=localhost -Ddb.user=root -Ddb.pass=yourpassword -cp "Task06/mysql-connector-j-8.3.0.jar:." Task06.EventRegistrationApp
```

### Screenshot Steps to Capture:
1. **Main UI & Catalog:** Launch application; capture the full window showing the registration form on the left, filter and table with seed records on the right, and the JMenuBar.
2. **Registration Success:** Enter student details and click "Register Student"; capture the success dialog and the table updating with the new row.
3. **Duplicate Prevention:** Enter the exact same student ID and event again; capture the "Duplicate Registration!" warning popup.
4. **Pagination:** Click "Next >" to navigate to page 2; capture the table showing the next page slice and updated "Page 2 of X" indicator.
5. **CSV Export:** Click "Export to CSV"; save the file; capture the export success confirmation dialog.
