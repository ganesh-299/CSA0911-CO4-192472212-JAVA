# TASK 2: HOSPITAL OUTPATIENT APPOINTMENT DESK

## 1. Aim
To design and implement a robust, interactive Hospital Outpatient Department (OPD) Appointment Desk GUI application using Java Swing. The application dynamically updates available specialist doctors based on department selection, validates patient demographic data, issues formatted appointment tokens, and protects against data loss by prompting for confirmation before window closure if a booking is in progress.

---

## 2. Requirements
- **Language / Runtime:** Java Standard Edition (JDK 8+ / OpenJDK 26 tested)
- **GUI Toolkit:** Java Swing (`JFrame`, `JPanel`, `JTextField`, `JComboBox`, `JButton`, `JTextArea`, `JScrollPane`, `JOptionPane`)
- **Event Handling:** `ActionListener`, `ItemListener`, `WindowAdapter` (`windowClosing`)
- **Data Structure:** `Map<String, String[]>` for department-to-doctor catalog mapping

---

## 3. Component Hierarchy

```
JFrame ("Hospital Outpatient Appointment Desk")
│
├── BorderLayout.NORTH
│   └── JPanel (Header Banner)
│       └── JLabel ("HOSPITAL OUTPATIENT APPOINTMENT BOOKING SYSTEM")
│
├── BorderLayout.CENTER
│   └── JPanel (GridLayout 1x2, Margins 12px)
│       ├── JPanel (Left: "Patient & Appointment Details", GridBagLayout)
│       │   ├── JLabel & JTextField (txtPatientId)
│       │   ├── JLabel & JTextField (txtPatientName)
│       │   ├── JLabel & JTextField (txtContact)
│       │   ├── JLabel & JComboBox (cmbDepartment)
│       │   ├── JLabel & JComboBox (cmbDoctor)
│       │   └── JLabel & JComboBox (cmbTimeSlot)
│       │
│       └── JPanel (Right: "Booking Confirmation & OPD Log", BorderLayout)
│           └── JScrollPane
│               └── JTextArea (txtSummaryArea)
│
└── BorderLayout.SOUTH
    └── JPanel (BorderLayout)
        ├── JPanel (FlowLayout: btnBook, btnReset)
        └── JLabel (lblStatus - Realtime status & alerts)
```

---

## 4. Event Table

| Event Source | Event Generated | Listener Interface | Registration Method | Handler Method / Behavior |
|--------------|-----------------|--------------------|---------------------|---------------------------|
| `cmbDepartment` (`JComboBox`) | `ActionEvent` / `ItemEvent` | `ActionListener` | `addActionListener(...)` | Reads selected department, fetches corresponding doctors from map, calls `cmbDoctor.setModel(...)`. |
| `btnBook` (`JButton`) | `ActionEvent` | `ActionListener` | `addActionListener(...)` | Validates ID, Name, Contact; shows confirmation popup; generates token; appends to log. |
| `btnReset` (`JButton`) | `ActionEvent` | `ActionListener` | `addActionListener(...)` | Clears all text fields, resets combo boxes to index 0. |
| Window Close Box (`[X]`) | `WindowEvent` (`WINDOW_CLOSING`) | `WindowListener` (`WindowAdapter`) | `addWindowListener(...)` | Checks `isBookingInProgress()`. If fields are dirty, prompts `JOptionPane.showConfirmDialog`. |

---

## 5. Algorithm & Pseudocode

### Algorithm for Dynamic Doctor Population
1. User clicks or selects an item from `cmbDepartment`.
2. The `ActionListener` triggers and retrieves the selected string `dept`.
3. Lookup `dept` in `departmentDoctorMap`.
4. Create a new `DefaultComboBoxModel<String>` with the doctor array.
5. Set `cmbDoctor.setModel(newModel)`.

### Algorithm for Window Closing Guard
1. User clicks the window's close button `[X]`.
2. Intercept via `WindowAdapter.windowClosing(WindowEvent e)`.
3. Check `isBookingInProgress()`:
   - If `txtPatientId`, `txtPatientName`, or `txtContact` contains text:
     - Prompt `JOptionPane.showConfirmDialog(...)`: "Booking in progress. Discard unsaved changes?"
     - If user clicks **YES**: call `dispose()` and `System.exit(0)`.
     - If user clicks **NO**: cancel event, keep window open.
   - If form is empty: close immediately.

### Pseudocode
```text
FUNCTION windowClosing(event):
    IF patientId != "" OR patientName != "" OR contact != "" THEN
        choice = SHOW_CONFIRM_DIALOG("Booking in progress! Exit and lose data?")
        IF choice == YES THEN
            DISPOSE_WINDOW()
            EXIT_PROGRAM()
        END IF
    ELSE
        DISPOSE_WINDOW()
        EXIT_PROGRAM()
    END IF
END FUNCTION
```

---

## 6. Event Sequence vs Applet Lifecycle Comparison

| Aspect | Modern Java Swing Desktop Application | Legacy Java Applet |
|--------|---------------------------------------|--------------------|
| **Execution Model** | Standalone JVM process started via `public static void main(String[] args)` on OS desktop. | Embedded in browser JVM plugin, managed by browser runtime or `appletviewer`. |
| **Lifecycle Stages** | 1. Class loading<br>2. EDT Initialization (`SwingUtilities.invokeLater`)<br>3. Frame construction & layout<br>4. Event dispatch loop (`EventQueue`)<br>5. Window closing (`dispose`, exit). | 1. `init()`: Load parameters, create GUI<br>2. `start()`: Begin animations/threads<br>3. `paint(Graphics g)`: Render UI<br>4. `stop()`: Pause when tab hidden<br>5. `destroy()`: Final cleanup. |
| **Event Sequence** | OS Mouse/Keyboard Interrupt $\to$ Native Window Manager $\to$ AWT EventQueue $\to$ Event Dispatch Thread (EDT) $\to$ Registered `ActionListener`/`MouseListener`. | Browser Event $\to$ Java Plugin sandbox $\to$ `handleEvent(Event)` / AWT dispatch $\to$ Applet methods. |
| **Security & Deprecation**| Full desktop sandbox/native permissions. Fully active and supported in modern JDKs. | Highly restricted browser sandbox. Completely deprecated in Java 9 and removed in Java 11+. |

---

## 7. Map vs Database: Alternative Concept & Complexity

### Alternative Concepts:
- **In-Memory Map (`Map<String, String[]>`):**
  - Best for small, static catalogs (e.g. 5 departments and 20 doctors).
  - Extremely fast with zero network overhead.
- **Relational Database (e.g. MySQL / SQLite via JDBC):**
  - Essential for multi-user hospital systems where doctor schedules, leaves, and booked slots change in real time across multiple reception desks.
  - Query: `SELECT doctor_name FROM doctors WHERE department_id = ? AND is_available = 1;`

### Big-O Complexity Comparison:

| Operation | In-Memory `HashMap` Lookup | Indexed Relational Database Query |
|-----------|----------------------------|-----------------------------------|
| **Department $\to$ Doctors Lookup** | **$\mathcal{O}(1)$** average time | **$\mathcal{O}(\log N)$** B-Tree index scan + network I/O latency |
| **Memory Footprint** | Stored in JVM heap: $\mathcal{O}(D + K)$ where $D$ = departments, $K$ = doctors. | JVM memory: $\mathcal{O}(1)$ for connection pool; persistence offloaded to DB disk. |
| **Concurrency / Data Consistency** | High risk of stale data across multiple instances unless synchronized. | ACID compliant transactions guarantee synchronized booking across desks. |

---

## 8. Sample Output

### Booking Confirmation in Log Area:
```
=========================================
       APPOINTMENT CONFIRMED            
=========================================
Booking Token : APT-4892
Patient ID    : PID-1002
Patient Name  : Samantha Miller
Department    : Cardiology
Doctor        : Dr. Robert Vance (Senior Cardiologist)
Slot Allocated: 10:30 AM - 11:00 AM
Timestamp     : Sun Nov 15 13:35:00 IST 2026
Status        : SCHEDULED / ACTIVE
=========================================
```

---

## 9. Test Cases

| Test ID | Test Scenario | Inputs | Expected Behavior | Pass/Fail |
|---------|---------------|--------|-------------------|-----------|
| **TC-01** | Dynamic Doctor Selection | Select "Pediatrics" in Department | Doctor dropdown updates immediately to Dr. Emily Watson, Dr. Noah Bennett, Dr. Grace Hopper. | PASS |
| **TC-02** | Valid Booking | `PID-501`, `John Doe`, `9876543210`, "Orthopedics" | Confirmation popup displayed. On Yes, generates token `APT-XXXX` and writes to log. | PASS |
| **TC-03** | Missing Patient ID | Empty ID, `John Doe`, `9876543210` | Warning: "Patient ID is required (e.g., PID-1001)." Focus returned to Patient ID. | PASS |
| **TC-04** | Invalid Contact Number | `PID-501`, `John`, `9876` (< 10 digits) | Warning: "Contact number must be exactly 10 digits." | PASS |
| **TC-05** | Window Closing with Data | Enter `PID-501`, then click `[X]` | Warning dialog: "An appointment booking is currently in progress! Unsaved patient data will be lost. Are you sure you want to exit?" | PASS |
| **TC-06** | Window Closing Empty Form | Form is blank, click `[X]` | Window closes immediately without warning dialog. | PASS |

---

## 10. Compilation, Execution & Screenshot Instructions

### Compilation:
```bash
javac Task02/AppointmentDesk.java
```

### Execution:
```bash
java Task02.AppointmentDesk
```

### Screenshot Steps to Capture:
1. **Dynamic Dropdown:** Open the application, click the Department combo box and select "Neurology"; capture the updated doctor list.
2. **Confirmation Dialog:** Fill in valid patient details and click "Book Appointment"; capture the confirmation popup window.
3. **Closing Warning Dialog:** Type a Patient ID and Name, then click the red window close button `[X]`; capture the unsaved progress confirmation dialog.
