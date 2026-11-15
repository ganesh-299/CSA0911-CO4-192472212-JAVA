package Task01;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * FeeService - Encapsulates all business logic for student fee management.
 * Separates core fee calculation, validation, and receipt generation
 * from the GUI layer (FeeWindow), adhering to the Single Responsibility Principle.
 */
class FeeService {
    private boolean lastPaymentSuccessful = false;
    private String lastReceiptText = "";

    /**
     * Validates student roll number and name inputs.
     * @param rollNo Student roll number
     * @param name Student name
     * @return Error message if invalid, or null if valid.
     */
    public String validateInputs(String rollNo, String name) {
        if (rollNo == null || rollNo.trim().isEmpty()) {
            return "Roll Number cannot be empty.";
        }
        if (name == null || name.trim().isEmpty()) {
            return "Student Name cannot be empty.";
        }
        if (!rollNo.matches("[a-zA-Z0-9]+")) {
            return "Roll Number must be alphanumeric without spaces.";
        }
        return null; // Valid
    }

    /**
     * Calculates net payable fee based on tuition, scholarship percentage, and late fines.
     * Big-O Time Complexity: O(1) - simple arithmetic calculation.
     * Big-O Space Complexity: O(1) - uses fixed auxiliary memory.
     */
    public double calculateNetFee(double baseTuition, double scholarshipPercent, double lateFine) {
        if (baseTuition < 0) baseTuition = 0;
        if (scholarshipPercent < 0) scholarshipPercent = 0;
        if (scholarshipPercent > 100) scholarshipPercent = 100;
        if (lateFine < 0) lateFine = 0;

        double discount = (baseTuition * scholarshipPercent) / 100.0;
        return (baseTuition - discount) + lateFine;
    }

    /**
     * Processes payment transaction.
     */
    public boolean processPayment(String rollNo, double amount) {
        if (amount <= 0) {
            lastPaymentSuccessful = false;
            return false;
        }
        // Simulated payment gateway confirmation
        lastPaymentSuccessful = true;
        return true;
    }

    /**
     * Generates a formatted academic fee receipt.
     */
    public String generateReceipt(String rollNo, String name, String course, String semester,
                                  double baseTuition, double scholarshipPercent,
                                  double lateFine, double totalPaid) {
        String timestamp = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date());
        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("            OFFICIAL COLLEGE FEE RECEIPT             \n");
        sb.append("====================================================\n");
        sb.append(String.format("Date & Time   : %s\n", timestamp));
        sb.append(String.format("Roll Number   : %s\n", rollNo));
        sb.append(String.format("Student Name  : %s\n", name));
        sb.append(String.format("Course/Dept   : %s\n", course));
        sb.append(String.format("Semester      : %s\n", semester));
        sb.append("----------------------------------------------------\n");
        sb.append(String.format("Base Tuition  : $ %.2f\n", baseTuition));
        sb.append(String.format("Scholarship   : %.1f%% (-$ %.2f)\n", scholarshipPercent, (baseTuition * scholarshipPercent / 100.0)));
        sb.append(String.format("Late Fine     : +$ %.2f\n", lateFine));
        sb.append("----------------------------------------------------\n");
        sb.append(String.format("Total Amount  : $ %.2f\n", totalPaid));
        sb.append("Payment Status: SUCCESSFUL / PAID\n");
        sb.append("Transaction ID: TXN").append(System.currentTimeMillis()).append("\n");
        sb.append("====================================================\n");
        this.lastReceiptText = sb.toString();
        return this.lastReceiptText;
    }

    public boolean isLastPaymentSuccessful() {
        return lastPaymentSuccessful;
    }

    public String getLastReceiptText() {
        return lastReceiptText;
    }

    public void reset() {
        lastPaymentSuccessful = false;
        lastReceiptText = "";
    }
}

/**
 * FeeWindow - Presentation layer for the College Fee Management System.
 * Uses clean modular design with separate ActionListeners for Pay, Clear, and Receipt.
 */
public class FeeWindow extends JFrame {

    // UI Input Components
    private JTextField txtRollNo;
    private JTextField txtName;
    private JComboBox<String> cmbCourse;
    private JComboBox<String> cmbSemester;
    private JTextField txtBaseTuition;
    private JTextField txtScholarship;
    private JTextField txtLateFine;

    // Output & Action Components
    private JTextArea txtReceiptArea;
    private JButton btnPay;
    private JButton btnClear;
    private JButton btnReceipt;
    private JLabel lblStatus;

    // Service Dependency
    private final FeeService feeService;

    public FeeWindow() {
        super("Campus Fee Management System - Redesigned GUI");
        this.feeService = new FeeService();
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(780, 620);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // 1. Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(24, 76, 120));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
        JLabel lblHeader = new JLabel("CAMPUS STUDENT FEE PAYMENT PORTAL");
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerPanel.add(lblHeader);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Panel - Split into Form (Left) and Receipt View (Right)
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Student & Fee Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtRollNo = new JTextField(15);
        txtName = new JTextField(15);
        cmbCourse = new JComboBox<>(new String[]{"Computer Science & Engg", "Information Technology", "Electronics & Comm", "Mechanical Engg", "Civil Engg"});
        cmbSemester = new JComboBox<>(new String[]{"Semester 1", "Semester 2", "Semester 3", "Semester 4", "Semester 5", "Semester 6", "Semester 7", "Semester 8"});
        txtBaseTuition = new JTextField("45000", 15);
        txtScholarship = new JTextField("10", 15);
        txtLateFine = new JTextField("0", 15);

        // Row 0: Roll No
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Roll Number:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtRollNo, gbc);

        // Row 1: Name
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Student Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtName, gbc);

        // Row 2: Course
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Department/Course:"), gbc);
        gbc.gridx = 1;
        formPanel.add(cmbCourse, gbc);

        // Row 3: Semester
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Semester:"), gbc);
        gbc.gridx = 1;
        formPanel.add(cmbSemester, gbc);

        // Row 4: Base Tuition
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Base Tuition Fee ($):"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtBaseTuition, gbc);

        // Row 5: Scholarship %
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Scholarship (%):"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtScholarship, gbc);

        // Row 6: Late Fine
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Late Fine ($):"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtLateFine, gbc);

        centerPanel.add(formPanel);

        // Receipt Output Panel
        JPanel receiptPanel = new JPanel(new BorderLayout(5, 5));
        receiptPanel.setBorder(BorderFactory.createTitledBorder("Official Receipt / Summary"));
        txtReceiptArea = new JTextArea();
        txtReceiptArea.setEditable(false);
        txtReceiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtReceiptArea.setText("Please enter student details, calculate and click Pay Fee.\nReceipt will appear here after successful payment.");
        JScrollPane scrollPane = new JScrollPane(txtReceiptArea);
        receiptPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(receiptPanel);

        add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom Control & Status Panel
        JPanel bottomContainer = new JPanel(new BorderLayout());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPay = new JButton("Pay Fee");
        btnPay.setBackground(new Color(34, 139, 34));
        btnPay.setToolTipText("Validates input, calculates total fee and records payment");

        btnClear = new JButton("Clear Form");
        btnClear.setToolTipText("Resets all inputs and receipt output");

        btnReceipt = new JButton("Print / View Receipt");
        btnReceipt.setEnabled(false); // Enabled only after payment
        btnReceipt.setToolTipText("Displays and formats the official transaction receipt");

        buttonPanel.add(btnPay);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnReceipt);

        lblStatus = new JLabel(" Ready. Enter details to proceed.", JLabel.LEFT);
        lblStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        lblStatus.setForeground(new Color(50, 50, 50));

        bottomContainer.add(buttonPanel, BorderLayout.NORTH);
        bottomContainer.add(lblStatus, BorderLayout.SOUTH);
        add(bottomContainer, BorderLayout.SOUTH);

        // Register Distinct Event Handlers
        btnPay.addActionListener(new PayHandler());
        btnClear.addActionListener(new ClearHandler());
        btnReceipt.addActionListener(new ReceiptHandler());
    }

    /**
     * Dedicated Event Handler for 'Pay' Action.
     * Validates input, parses amounts safely, calls FeeService, updates UI.
     */
    private class PayHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String rollNo = txtRollNo.getText().trim();
            String name = txtName.getText().trim();

            // Input validation
            String validationError = feeService.validateInputs(rollNo, name);
            if (validationError != null) {
                JOptionPane.showMessageDialog(FeeWindow.this, validationError, "Validation Error", JOptionPane.WARNING_MESSAGE);
                lblStatus.setText(" Status: " + validationError);
                return;
            }

            // Numeric input parsing
            double tuition, scholarship, lateFine;
            try {
                tuition = Double.parseDouble(txtBaseTuition.getText().trim());
                scholarship = Double.parseDouble(txtScholarship.getText().trim());
                lateFine = Double.parseDouble(txtLateFine.getText().trim());

                if (tuition < 0 || scholarship < 0 || lateFine < 0 || scholarship > 100) {
                    JOptionPane.showMessageDialog(FeeWindow.this,
                            "Fee and Fine must be >= 0. Scholarship must be between 0 and 100%.",
                            "Invalid Range", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(FeeWindow.this,
                        "Please enter valid numeric values for Tuition, Scholarship, and Fine.",
                        "Number Format Error", JOptionPane.ERROR_MESSAGE);
                lblStatus.setText(" Status: Numeric format error.");
                return;
            }

            // Business Logic via FeeService
            double netFee = feeService.calculateNetFee(tuition, scholarship, lateFine);

            int confirm = JOptionPane.showConfirmDialog(FeeWindow.this,
                    String.format("Confirm Payment of $ %.2f for %s (%s)?", netFee, name, rollNo),
                    "Confirm Payment", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = feeService.processPayment(rollNo, netFee);
                if (success) {
                    btnReceipt.setEnabled(true);
                    lblStatus.setText(String.format(" Status: Payment of $ %.2f recorded successfully.", netFee));
                    JOptionPane.showMessageDialog(FeeWindow.this,
                            "Payment Successful!\nClick 'Print / View Receipt' to inspect transaction details.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);

                    // Auto-display receipt
                    String receipt = feeService.generateReceipt(rollNo, name,
                            (String) cmbCourse.getSelectedItem(),
                            (String) cmbSemester.getSelectedItem(),
                            tuition, scholarship, lateFine, netFee);
                    txtReceiptArea.setText(receipt);
                }
            } else {
                lblStatus.setText(" Status: Payment cancelled by user.");
            }
        }
    }

    /**
     * Dedicated Event Handler for 'Clear' Action.
     * Clears all fields and restores initial form state.
     */
    private class ClearHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            txtRollNo.setText("");
            txtName.setText("");
            cmbCourse.setSelectedIndex(0);
            cmbSemester.setSelectedIndex(0);
            txtBaseTuition.setText("45000");
            txtScholarship.setText("0");
            txtLateFine.setText("0");
            txtReceiptArea.setText("Form cleared. Ready for new input.");
            btnReceipt.setEnabled(false);
            feeService.reset();
            lblStatus.setText(" Status: Form cleared.");
            txtRollNo.requestFocus();
        }
    }

    /**
     * Dedicated Event Handler for 'Receipt' Action.
     * Shows receipt dialog or regenerates receipt view.
     */
    private class ReceiptHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!feeService.isLastPaymentSuccessful()) {
                JOptionPane.showMessageDialog(FeeWindow.this,
                        "No completed payment transaction found. Please pay fee first.",
                        "Receipt Unavailable", JOptionPane.WARNING_MESSAGE);
                return;
            }
            txtReceiptArea.setText(feeService.getLastReceiptText());
            lblStatus.setText(" Status: Receipt displayed.");
            JOptionPane.showMessageDialog(FeeWindow.this,
                    "Receipt is displayed in the summary panel.\nYou may copy or print it.",
                    "Receipt Ready", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Enforce execution on Event Dispatch Thread (EDT) for thread safety
        SwingUtilities.invokeLater(() -> {
            FeeWindow window = new FeeWindow();
            window.setVisible(true);
        });
    }
}
