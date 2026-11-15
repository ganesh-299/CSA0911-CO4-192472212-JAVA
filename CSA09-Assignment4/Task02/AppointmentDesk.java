package Task02;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * AppointmentDesk - Hospital Outpatient Department (OPD) Appointment Booking GUI.
 * Features dynamic doctor filtering by department, validation, confirmation before close,
 * and comprehensive event-driven architecture.
 */
public class AppointmentDesk extends JFrame {

    // Input Components
    private JTextField txtPatientId;
    private JTextField txtPatientName;
    private JTextField txtContact;
    private JComboBox<String> cmbDepartment;
    private JComboBox<String> cmbDoctor;
    private JComboBox<String> cmbTimeSlot;
    private JTextArea txtSummaryArea;

    // Action Buttons
    private JButton btnBook;
    private JButton btnReset;
    private JLabel lblStatus;

    // Department to Doctor Mapping (In-Memory Database / Map Model)
    private final Map<String, String[]> departmentDoctorMap;

    // Booking state tracking flag
    private boolean isBookingCommitted = false;

    public AppointmentDesk() {
        super("Hospital Outpatient Appointment Desk");
        departmentDoctorMap = new LinkedHashMap<>();
        initDoctorDirectory();
        initComponents();
        setupWindowClosingGuard();
    }

    /**
     * Initializes departments and their corresponding specialist doctors.
     */
    private void initDoctorDirectory() {
        departmentDoctorMap.put("Cardiology", new String[]{
                "Dr. Robert Vance (Senior Cardiologist)",
                "Dr. Sarah Jenkins (Interventional Cardiology)",
                "Dr. Alistair Finch (Heart Rhythm Specialist)"
        });
        departmentDoctorMap.put("Neurology", new String[]{
                "Dr. Elena Rostova (Consultant Neurologist)",
                "Dr. Marcus Brody (Neuro-Surgeon)",
                "Dr. Clara Higgins (Stroke Specialist)"
        });
        departmentDoctorMap.put("Orthopedics", new String[]{
                "Dr. Arthur Dent (Joint Replacement)",
                "Dr. Lisa Ray (Sports Medicine & Spine)",
                "Dr. David Kim (Trauma Specialist)"
        });
        departmentDoctorMap.put("Pediatrics", new String[]{
                "Dr. Emily Watson (Pediatrician)",
                "Dr. Noah Bennett (Neonatologist)",
                "Dr. Grace Hopper (Child Health & Nutrition)"
        });
        departmentDoctorMap.put("Dermatology", new String[]{
                "Dr. Chloe Martin (Cosmetic Dermatology)",
                "Dr. Samuel Green (Clinical Dermatologist)"
        });
        departmentDoctorMap.put("General Medicine", new String[]{
                "Dr. Alan Grant (Senior Physician)",
                "Dr. Bruce Wayne (Family Medicine)",
                "Dr. Diana Prince (Internal Medicine)"
        });
    }

    private void initComponents() {
        setSize(820, 640);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // 1. Top Header Banner
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(13, 71, 161));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        JLabel lblHeader = new JLabel("HOSPITAL OUTPATIENT APPOINTMENT BOOKING SYSTEM");
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("SansSerif", Font.BOLD, 17));
        headerPanel.add(lblHeader);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Content Split: Booking Form (Left) & Appointment Feed (Right)
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 12, 12));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Patient & Appointment Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtPatientId = new JTextField(15);
        txtPatientName = new JTextField(15);
        txtContact = new JTextField(15);

        String[] departments = departmentDoctorMap.keySet().toArray(new String[0]);
        cmbDepartment = new JComboBox<>(departments);
        cmbDoctor = new JComboBox<>();
        updateDoctorDropdown(departments[0]); // Initial population

        String[] timeSlots = new String[]{
                "09:00 AM - 09:30 AM",
                "09:30 AM - 10:00 AM",
                "10:30 AM - 11:00 AM",
                "11:30 AM - 12:00 PM",
                "02:00 PM - 02:30 PM",
                "03:00 PM - 03:30 PM",
                "04:00 PM - 04:30 PM"
        };
        cmbTimeSlot = new JComboBox<>(timeSlots);

        // Populate Form Rows
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Patient ID (e.g. PID-101):"), gbc);
        gbc.gridx = 1; formPanel.add(txtPatientId, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Patient Name:"), gbc);
        gbc.gridx = 1; formPanel.add(txtPatientName, gbc);

        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Contact Number (10 Digits):"), gbc);
        gbc.gridx = 1; formPanel.add(txtContact, gbc);

        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Select Department:"), gbc);
        gbc.gridx = 1; formPanel.add(cmbDepartment, gbc);

        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(new JLabel("Select Doctor:"), gbc);
        gbc.gridx = 1; formPanel.add(cmbDoctor, gbc);

        gbc.gridx = 0; gbc.gridy = 5; formPanel.add(new JLabel("Preferred Time Slot:"), gbc);
        gbc.gridx = 1; formPanel.add(cmbTimeSlot, gbc);

        centerPanel.add(formPanel);

        // Right Panel: Appointment Log & Confirmation Details
        JPanel summaryPanel = new JPanel(new BorderLayout(6, 6));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Booking Confirmation & OPD Log"));
        txtSummaryArea = new JTextArea();
        txtSummaryArea.setEditable(false);
        txtSummaryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtSummaryArea.setText("--- OPD APPOINTMENT DESK ACTIVE ---\n\nEnter patient information, choose department and slot,\nthen click 'Book Appointment'.\n");
        JScrollPane scrollPane = new JScrollPane(txtSummaryArea);
        summaryPanel.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(summaryPanel);
        add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom Controls & Status Bar
        JPanel bottomContainer = new JPanel(new BorderLayout());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        btnBook = new JButton("Book Appointment");
        btnBook.setBackground(new Color(46, 125, 50));
        btnReset = new JButton("Reset Form");

        buttonPanel.add(btnBook);
        buttonPanel.add(btnReset);

        lblStatus = new JLabel(" Ready. Select department to load doctors.", JLabel.LEFT);
        lblStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));

        bottomContainer.add(buttonPanel, BorderLayout.NORTH);
        bottomContainer.add(lblStatus, BorderLayout.SOUTH);
        add(bottomContainer, BorderLayout.SOUTH);

        // --- Event Listeners Registration ---

        // 1. Dynamic Doctor Dropdown Update upon Department Change
        cmbDepartment.addActionListener(e -> {
            String selectedDept = (String) cmbDepartment.getSelectedItem();
            updateDoctorDropdown(selectedDept);
            lblStatus.setText(" Loaded specialist doctors for: " + selectedDept);
        });

        // 2. Book Appointment Action Listener
        btnBook.addActionListener(e -> handleBooking());

        // 3. Reset Button Action Listener
        btnReset.addActionListener(e -> resetForm());
    }

    /**
     * Updates doctor dropdown options dynamically based on department selection.
     */
    private void updateDoctorDropdown(String department) {
        String[] doctors = departmentDoctorMap.get(department);
        if (doctors != null) {
            cmbDoctor.setModel(new DefaultComboBoxModel<>(doctors));
        } else {
            cmbDoctor.setModel(new DefaultComboBoxModel<>(new String[]{"No doctor currently available"}));
        }
    }

    /**
     * Validates input fields and completes appointment booking.
     */
    private void handleBooking() {
        String pid = txtPatientId.getText().trim();
        String name = txtPatientName.getText().trim();
        String contact = txtContact.getText().trim();
        String dept = (String) cmbDepartment.getSelectedItem();
        String doctor = (String) cmbDoctor.getSelectedItem();
        String slot = (String) cmbTimeSlot.getSelectedItem();

        // 1. Validation Checks
        if (pid.isEmpty()) {
            showError("Patient ID is required (e.g., PID-1001).");
            txtPatientId.requestFocus();
            return;
        }
        if (!pid.matches("^[A-Za-z0-9\\-]+$")) {
            showError("Patient ID must contain only alphanumeric characters and hyphens.");
            txtPatientId.requestFocus();
            return;
        }
        if (name.isEmpty() || name.length() < 2) {
            showError("Patient Name is required (minimum 2 characters).");
            txtPatientName.requestFocus();
            return;
        }
        if (!contact.matches("^[0-9]{10}$")) {
            showError("Contact number must be exactly 10 digits.");
            txtContact.requestFocus();
            return;
        }

        // 2. Confirmation Dialog
        String message = String.format(
                "Please confirm appointment details:\n\n" +
                "Patient ID : %s\n" +
                "Name       : %s\n" +
                "Contact    : %s\n" +
                "Department : %s\n" +
                "Doctor     : %s\n" +
                "Time Slot  : %s\n\n" +
                "Proceed with confirmation?",
                pid, name, contact, dept, doctor, slot
        );

        int confirm = JOptionPane.showConfirmDialog(this, message, "Confirm Appointment",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // Generate Appointment Token
            String token = "APT-" + (1000 + new Random().nextInt(9000));
            isBookingCommitted = true;

            // Update Log Area
            StringBuilder log = new StringBuilder();
            log.append("=========================================\n");
            log.append("       APPOINTMENT CONFIRMED            \n");
            log.append("=========================================\n");
            log.append("Booking Token : ").append(token).append("\n");
            log.append("Patient ID    : ").append(pid).append("\n");
            log.append("Patient Name  : ").append(name).append("\n");
            log.append("Department    : ").append(dept).append("\n");
            log.append("Doctor        : ").append(doctor).append("\n");
            log.append("Slot Allocated: ").append(slot).append("\n");
            log.append("Timestamp     : ").append(new Date()).append("\n");
            log.append("Status        : SCHEDULED / ACTIVE\n");
            log.append("=========================================\n\n");

            txtSummaryArea.append(log.toString());
            lblStatus.setText(" Appointment " + token + " successfully booked for " + name);

            JOptionPane.showMessageDialog(this,
                    "Appointment Successfully Booked!\nToken: " + token + "\nPlease report 15 minutes before the time slot.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            // Reset form for next patient
            txtPatientId.setText("");
            txtPatientName.setText("");
            txtContact.setText("");
            isBookingCommitted = false;
        } else {
            lblStatus.setText(" Booking cancelled by user.");
        }
    }

    /**
     * Resets input controls.
     */
    private void resetForm() {
        txtPatientId.setText("");
        txtPatientName.setText("");
        txtContact.setText("");
        cmbDepartment.setSelectedIndex(0);
        cmbTimeSlot.setSelectedIndex(0);
        lblStatus.setText(" Form reset. Ready for new booking.");
        isBookingCommitted = false;
    }

    /**
     * Helper to show warning dialog.
     */
    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Input Validation Error", JOptionPane.WARNING_MESSAGE);
        lblStatus.setText(" Validation Error: " + msg);
    }

    /**
     * Determines whether booking data is currently entered and unsaved.
     */
    public boolean isBookingInProgress() {
        if (isBookingCommitted) return false;
        return !txtPatientId.getText().trim().isEmpty() ||
               !txtPatientName.getText().trim().isEmpty() ||
               !txtContact.getText().trim().isEmpty();
    }

    /**
     * Configures WindowClosing confirmation prompt when user attempts to close
     * window with half-filled unsaved booking in progress.
     */
    private void setupWindowClosingGuard() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (isBookingInProgress()) {
                    int choice = JOptionPane.showConfirmDialog(AppointmentDesk.this,
                            "An appointment booking is currently in progress!\n" +
                            "Unsaved patient data will be lost.\n\n" +
                            "Are you sure you want to exit?",
                            "Confirm Exit - Unsaved Booking",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (choice == JOptionPane.YES_OPTION) {
                        dispose();
                        System.exit(0);
                    }
                } else {
                    dispose();
                    System.exit(0);
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AppointmentDesk desk = new AppointmentDesk();
            desk.setVisible(true);
        });
    }
}
