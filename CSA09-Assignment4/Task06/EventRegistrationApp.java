package Task06;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;

/**
 * EventRegistrationApp - Complete Campus Event Registration GUI Application.
 * Integrates Swing, JDBC, PreparedStatement, pagination (SQL LIMIT & OFFSET),
 * duplicate prevention, CSV export, JMenuBar, and SwingWorker background threading.
 */
public class EventRegistrationApp extends JFrame {

    // UI Input Components
    private JTextField txtStudentId;
    private JTextField txtStudentName;
    private JComboBox<String> cmbEvent;
    private JButton btnRegister;
    private JButton btnClear;

    // Filter & Table Components
    private JComboBox<String> cmbFilterEvent;
    private JButton btnRefresh;
    private JTable tblRegistrations;
    private DefaultTableModel tableModel;

    // Pagination Controls
    private JButton btnFirstPage;
    private JButton btnPrevPage;
    private JButton btnNextPage;
    private JButton btnLastPage;
    private JLabel lblPageInfo;
    private JComboBox<Integer> cmbPageSize;

    // Bottom Action & Status
    private JButton btnExportCsv;
    private JLabel lblStatus;

    // Service Layer Reference
    private final RegistrationService registrationService;

    // Pagination State
    private int currentPage = 0; // 0-based
    private int pageSize = 5;
    private int totalRecords = 0;
    private int totalPages = 1;

    private static final String[] EVENT_OPTIONS = {
            "Hackathon 2026",
            "AI & Machine Learning Workshop",
            "Web Dev Bootcamp",
            "Robotics Expo",
            "Competitive Coding Marathon"
    };

    public EventRegistrationApp() {
        super("Campus Event Registration & Management Portal");
        this.registrationService = new RegistrationService();
        initMenuBar();
        initComponents();
        loadRegistrationsAsync(); // Initial asynchronous load via SwingWorker
    }

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // 1. File Menu
        JMenu menuFile = new JMenu("File");
        JMenuItem itemExport = new JMenuItem("Export Registrations to CSV...");
        itemExport.addActionListener(e -> handleCsvExport());
        JMenuItem itemReset = new JMenuItem("Reset Registration Form");
        itemReset.addActionListener(e -> resetForm());
        JMenuItem itemExit = new JMenuItem("Exit Application");
        itemExit.addActionListener(e -> handleExit());

        menuFile.add(itemExport);
        menuFile.add(itemReset);
        menuFile.addSeparator();
        menuFile.add(itemExit);
        menuBar.add(menuFile);

        // 2. View Menu
        JMenu menuView = new JMenu("View");
        JMenuItem itemRefresh = new JMenuItem("Refresh Table Data");
        itemRefresh.addActionListener(e -> loadRegistrationsAsync());
        JMenuItem itemFirst = new JMenuItem("Go to First Page");
        itemFirst.addActionListener(e -> goToPage(0));
        JMenuItem itemLast = new JMenuItem("Go to Last Page");
        itemLast.addActionListener(e -> goToPage(totalPages - 1));

        menuView.add(itemRefresh);
        menuView.addSeparator();
        menuView.add(itemFirst);
        menuView.add(itemLast);
        menuBar.add(menuView);

        // 3. Database Menu
        JMenu menuDb = new JMenu("Database");
        JMenuItem itemTestConn = new JMenuItem("Test MySQL Connection");
        itemTestConn.addActionListener(e -> checkDbConnection());
        JMenuItem itemDbInfo = new JMenuItem("Database Info");
        itemDbInfo.addActionListener(e -> showDbInfo());

        menuDb.add(itemTestConn);
        menuDb.add(itemDbInfo);
        menuBar.add(menuDb);

        // 4. Help Menu
        JMenu menuHelp = new JMenu("Help");
        JMenuItem itemAbout = new JMenuItem("About Application");
        itemAbout.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Campus Event Registration Portal (Java Swing + JDBC)\n" +
                "Architecture: 3-Tier Layered (UI -> Service -> DAO -> Database)\n" +
                "Threading: SwingWorker asynchronous worker threads\n" +
                "Pagination: SQL LIMIT & OFFSET\n" +
                "Export: CSV format with custom RFC-4180 escaping.",
                "About", JOptionPane.INFORMATION_MESSAGE));
        menuHelp.add(itemAbout);
        menuBar.add(menuHelp);

        setJMenuBar(menuBar);
    }

    private void initComponents() {
        setSize(980, 680);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Top Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(21, 101, 192));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
        JLabel lblHeader = new JLabel("CAMPUS EVENT REGISTRATION & STUDENT TRACKING");
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("SansSerif", Font.BOLD, 17));
        headerPanel.add(lblHeader);
        add(headerPanel, BorderLayout.NORTH);

        // Center Split: Left = Form, Right = Table & Controls
        JPanel centerPanel = new JPanel(new BorderLayout(12, 12));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        // LEFT PANEL: Registration Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setPreferredSize(new Dimension(340, 400));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Student Event Registration"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtStudentId = new JTextField(14);
        txtStudentName = new JTextField(14);
        cmbEvent = new JComboBox<>(EVENT_OPTIONS);

        btnRegister = new JButton("Register Student");
        btnRegister.setBackground(new Color(46, 125, 50));
        btnClear = new JButton("Clear");

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Student ID:"), gbc);
        gbc.gridx = 1; formPanel.add(txtStudentId, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Student Name:"), gbc);
        gbc.gridx = 1; formPanel.add(txtStudentName, gbc);

        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Select Event:"), gbc);
        gbc.gridx = 1; formPanel.add(cmbEvent, gbc);

        JPanel formButtonBox = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        formButtonBox.add(btnRegister);
        formButtonBox.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        formPanel.add(formButtonBox, gbc);

        // Helper hint
        JTextArea txtHint = new JTextArea("Notes:\n• Duplicate registrations for the same student & event are rejected.\n• Background tasks use SwingWorker for smooth UI performance.");
        txtHint.setWrapStyleWord(true);
        txtHint.setLineWrap(true);
        txtHint.setEditable(false);
        txtHint.setBackground(formPanel.getBackground());
        txtHint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        gbc.gridy = 4;
        formPanel.add(txtHint, gbc);

        centerPanel.add(formPanel, BorderLayout.WEST);

        // RIGHT PANEL: Table, Filtering, Pagination
        JPanel tableContainer = new JPanel(new BorderLayout(8, 8));
        tableContainer.setBorder(BorderFactory.createTitledBorder("Registered Students (Live Catalog)"));

        // Filter Bar (North of Table)
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        filterBar.add(new JLabel("Filter by Event:"));
        String[] filterOptions = new String[EVENT_OPTIONS.length + 1];
        filterOptions[0] = "All Events";
        System.arraycopy(EVENT_OPTIONS, 0, filterOptions, 1, EVENT_OPTIONS.length);
        cmbFilterEvent = new JComboBox<>(filterOptions);
        btnRefresh = new JButton("Refresh");

        filterBar.add(cmbFilterEvent);
        filterBar.add(btnRefresh);
        filterBar.add(Box.createHorizontalStrut(15));
        btnExportCsv = new JButton("Export to CSV");
        btnExportCsv.setBackground(new Color(230, 81, 0));
        filterBar.add(btnExportCsv);

        tableContainer.add(filterBar, BorderLayout.NORTH);

        // Center: JTable
        String[] cols = {"Reg ID", "Student ID", "Student Name", "Registered Event", "Timestamp"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tblRegistrations = new JTable(tableModel);
        tblRegistrations.setRowHeight(24);
        tblRegistrations.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        tblRegistrations.getColumnModel().getColumn(0).setPreferredWidth(60);
        tblRegistrations.getColumnModel().getColumn(1).setPreferredWidth(100);
        tblRegistrations.getColumnModel().getColumn(2).setPreferredWidth(160);
        tblRegistrations.getColumnModel().getColumn(3).setPreferredWidth(180);
        tblRegistrations.getColumnModel().getColumn(4).setPreferredWidth(140);

        JScrollPane scrollPane = new JScrollPane(tblRegistrations);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        // South of Table: Pagination Toolbar
        JPanel paginationBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        btnFirstPage = new JButton("<< First");
        btnPrevPage = new JButton("< Prev");
        lblPageInfo = new JLabel("Page 1 of 1 (0 total)");
        lblPageInfo.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnNextPage = new JButton("Next >");
        btnLastPage = new JButton("Last >>");

        paginationBar.add(btnFirstPage);
        paginationBar.add(btnPrevPage);
        paginationBar.add(Box.createHorizontalStrut(10));
        paginationBar.add(lblPageInfo);
        paginationBar.add(Box.createHorizontalStrut(10));
        paginationBar.add(btnNextPage);
        paginationBar.add(btnLastPage);

        paginationBar.add(Box.createHorizontalStrut(20));
        paginationBar.add(new JLabel("Page Size:"));
        cmbPageSize = new JComboBox<>(new Integer[]{5, 10, 25});
        cmbPageSize.setSelectedItem(5);
        paginationBar.add(cmbPageSize);

        tableContainer.add(paginationBar, BorderLayout.SOUTH);
        centerPanel.add(tableContainer, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Status Bar at Bottom
        JPanel statusPanel = new JPanel(new BorderLayout());
        lblStatus = new JLabel(" Ready. Ready to accept student event registrations.", JLabel.LEFT);
        lblStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        statusPanel.add(lblStatus, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);

        // Event Listeners
        btnRegister.addActionListener(e -> handleRegistrationAsync());
        btnClear.addActionListener(e -> resetForm());
        btnRefresh.addActionListener(e -> loadRegistrationsAsync());
        cmbFilterEvent.addActionListener(e -> {
            currentPage = 0;
            loadRegistrationsAsync();
        });
        btnExportCsv.addActionListener(e -> handleCsvExport());

        // Pagination buttons
        btnFirstPage.addActionListener(e -> goToPage(0));
        btnPrevPage.addActionListener(e -> goToPage(currentPage - 1));
        btnNextPage.addActionListener(e -> goToPage(currentPage + 1));
        btnLastPage.addActionListener(e -> goToPage(totalPages - 1));

        cmbPageSize.addActionListener(e -> {
            pageSize = (Integer) cmbPageSize.getSelectedItem();
            currentPage = 0;
            loadRegistrationsAsync();
        });

        // Window closing confirmation
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });
    }

    /**
     * Executes registration on a background thread using SwingWorker.
     * Prevents GUI freezing during database calls.
     */
    private void handleRegistrationAsync() {
        String studentId = txtStudentId.getText().trim();
        String studentName = txtStudentName.getText().trim();
        String eventName = (String) cmbEvent.getSelectedItem();

        // 1. UI validation
        String error = registrationService.validateInputs(studentId, studentName, eventName);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Validation Error", JOptionPane.WARNING_MESSAGE);
            lblStatus.setText(" Validation Error: " + error);
            return;
        }

        // Disable button during background operation
        btnRegister.setEnabled(false);
        lblStatus.setText(" Registering student in database...");

        // Launch SwingWorker
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            private String failReason = null;

            @Override
            protected Boolean doInBackground() throws Exception {
                // Background thread (off EDT)
                try {
                    return registrationService.register(studentId, studentName, eventName);
                } catch (Exception ex) {
                    failReason = ex.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                // Event Dispatch Thread (EDT)
                btnRegister.setEnabled(true);
                try {
                    boolean success = get();
                    if (success) {
                        lblStatus.setText(String.format(" Registered: %s (%s) for %s", studentName, studentId, eventName));
                        JOptionPane.showMessageDialog(EventRegistrationApp.this,
                                String.format("Registration Successful!\n\nStudent: %s\nID: %s\nEvent: %s",
                                        studentName, studentId, eventName),
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        txtStudentId.setText("");
                        txtStudentName.setText("");
                        txtStudentId.requestFocus();
                        loadRegistrationsAsync(); // Refresh table view
                    } else {
                        if (failReason != null) {
                            JOptionPane.showMessageDialog(EventRegistrationApp.this,
                                    "Database Error: " + failReason, "Error", JOptionPane.ERROR_MESSAGE);
                            lblStatus.setText(" Database Error: " + failReason);
                        } else {
                            // Duplicate registration detected
                            JOptionPane.showMessageDialog(EventRegistrationApp.this,
                                    String.format("Duplicate Registration!\nStudent ID '%s' is already registered for '%s'.",
                                            studentId, eventName),
                                    "Duplicate Detected", JOptionPane.WARNING_MESSAGE);
                            lblStatus.setText(" Duplicate: Student already registered for this event.");
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(EventRegistrationApp.this,
                            "Unexpected Worker Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    /**
     * Asynchronously queries the database for paginated records and updates JTable on EDT.
     */
    private void loadRegistrationsAsync() {
        String filter = (String) cmbFilterEvent.getSelectedItem();
        lblStatus.setText(" Loading records from database...");

        SwingWorker<List<RegistrationDAO.Registration>, Void> worker = new SwingWorker<List<RegistrationDAO.Registration>, Void>() {
            private int count = 0;

            @Override
            protected List<RegistrationDAO.Registration> doInBackground() throws Exception {
                count = registrationService.getTotalCount(filter);
                return registrationService.getPage(filter, currentPage, pageSize);
            }

            @Override
            protected void done() {
                try {
                    List<RegistrationDAO.Registration> records = get();
                    totalRecords = count;
                    totalPages = registrationService.calculateTotalPages(totalRecords, pageSize);

                    // Clamp currentPage
                    if (currentPage >= totalPages && totalPages > 0) {
                        currentPage = totalPages - 1;
                    }

                    // Populate Table
                    tableModel.setRowCount(0);
                    for (RegistrationDAO.Registration r : records) {
                        tableModel.addRow(new Object[]{
                                r.getId(),
                                r.getStudentId(),
                                r.getStudentName(),
                                r.getEventName(),
                                r.getRegistrationDate()
                        });
                    }

                    // Update Pagination Controls
                    lblPageInfo.setText(String.format("Page %d of %d (Total: %d)",
                            (totalRecords == 0 ? 0 : currentPage + 1), totalPages, totalRecords));
                    btnFirstPage.setEnabled(currentPage > 0);
                    btnPrevPage.setEnabled(currentPage > 0);
                    btnNextPage.setEnabled(currentPage < totalPages - 1);
                    btnLastPage.setEnabled(currentPage < totalPages - 1);

                    String dbMode = registrationService.getDao().isUsingMockDatabase()
                            ? "[Offline / In-Memory Mock Mode]"
                            : "[MySQL Live Connected]";
                    lblStatus.setText(String.format(" %s Loaded %d records on Page %d.", dbMode, records.size(), currentPage + 1));
                } catch (Exception ex) {
                    lblStatus.setText(" Error fetching data: " + ex.getMessage());
                }
            }
        };

        worker.execute();
    }

    private void goToPage(int page) {
        if (page >= 0 && page < totalPages) {
            this.currentPage = page;
            loadRegistrationsAsync();
        }
    }

    /**
     * Handles exporting all filtered registrations to CSV.
     */
    private void handleCsvExport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Registrations as CSV");
        fileChooser.setSelectedFile(new File("event_registrations.csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File targetFile = fileChooser.getSelectedFile();
            if (!targetFile.getName().toLowerCase().endsWith(".csv")) {
                targetFile = new File(targetFile.getAbsolutePath() + ".csv");
            }

            final File finalFile = targetFile;
            String filter = (String) cmbFilterEvent.getSelectedItem();

            SwingWorker<Integer, Void> exportWorker = new SwingWorker<Integer, Void>() {
                @Override
                protected Integer doInBackground() throws Exception {
                    return registrationService.exportToCSV(finalFile, filter);
                }

                @Override
                protected void done() {
                    try {
                        int exported = get();
                        JOptionPane.showMessageDialog(EventRegistrationApp.this,
                                String.format("Successfully exported %d record(s) to:\n%s", exported, finalFile.getAbsolutePath()),
                                "Export Complete", JOptionPane.INFORMATION_MESSAGE);
                        lblStatus.setText(" Exported " + exported + " records to " + finalFile.getName());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(EventRegistrationApp.this,
                                "Failed to export CSV: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            exportWorker.execute();
        }
    }

    private void checkDbConnection() {
        boolean connected = DatabaseConnection.testConnection();
        if (connected) {
            registrationService.getDao().setUseMock(false);
            JOptionPane.showMessageDialog(this,
                    "Connection Successful!\nConnected to MySQL database: " + DatabaseConnection.getDbName() +
                    "\nHost: " + DatabaseConnection.getJdbcUrl(),
                    "Database Online", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Cannot connect to MySQL server.\nURL: " + DatabaseConnection.getJdbcUrl() +
                    "\n\nRunning in local In-Memory Fallback mode.\nAll application features (UI, validation, pagination, CSV) are active.",
                    "Database Offline / Standalone Mode", JOptionPane.WARNING_MESSAGE);
        }
        loadRegistrationsAsync();
    }

    private void showDbInfo() {
        String info = String.format(
                "Database Engine: MySQL via JDBC\n" +
                "Driver: com.mysql.cj.jdbc.Driver\n" +
                "JDBC URL: %s\n" +
                "Username: %s\n" +
                "Active Mode: %s\n" +
                "Current Page Size: %d\n" +
                "Total Filtered Records: %d",
                DatabaseConnection.getJdbcUrl(),
                DatabaseConnection.getUsername(),
                (registrationService.getDao().isUsingMockDatabase() ? "In-Memory Fallback" : "Live MySQL Connection"),
                pageSize, totalRecords
        );
        JOptionPane.showMessageDialog(this, info, "Database Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetForm() {
        txtStudentId.setText("");
        txtStudentName.setText("");
        cmbEvent.setSelectedIndex(0);
        txtStudentId.requestFocus();
        lblStatus.setText(" Registration form cleared.");
    }

    private void handleExit() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to exit the Campus Event Registration application?",
                "Confirm Exit", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new EventRegistrationApp().setVisible(true);
        });
    }
}
