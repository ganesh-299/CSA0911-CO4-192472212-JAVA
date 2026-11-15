package Task03;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * LibrarySearch - Campus Library Search and Reservation GUI Application.
 * Demonstrates responsive nested layout managers (BorderLayout, FlowLayout, GridBagLayout),
 * JTable integration with JScrollPane, multi-parameter search filtering, and reservation handling.
 */
public class LibrarySearch extends JFrame {

    // Model class for Book entry
    static class Book {
        String id;
        String title;
        String author;
        String category;
        String status;

        public Book(String id, String title, String author, String category, String status) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.category = category;
            this.status = status;
        }
    }

    // Master dataset
    private final List<Book> bookDatabase = new ArrayList<>();

    // GUI Controls
    private JTextField txtSearchKeyword;
    private JComboBox<String> cmbCategory;
    private JButton btnSearch;
    private JButton btnResetSearch;

    // Table & Results Components
    private JTable tblResults;
    private DefaultTableModel tableModel;

    // Bottom Navigation & Reservation Controls
    private JButton btnPrev;
    private JButton btnNext;
    private JButton btnReserve;
    private JLabel lblStatus;

    public LibrarySearch() {
        super("Campus Library Search & Resource Portal");
        initBookCatalog();
        initComponents();
        populateTable(bookDatabase);
    }

    /**
     * Seeds initial catalog dataset.
     */
    private void initBookCatalog() {
        bookDatabase.add(new Book("CS-101", "Introduction to Java Programming", "Y. Daniel Liang", "Computer Science", "Available"));
        bookDatabase.add(new Book("CS-102", "Effective Java (3rd Edition)", "Joshua Bloch", "Computer Science", "Available"));
        bookDatabase.add(new Book("CS-103", "Design Patterns: Gang of Four", "Erich Gamma et al.", "Computer Science", "Issued"));
        bookDatabase.add(new Book("CS-104", "Clean Code: Agile Software Craft", "Robert C. Martin", "Computer Science", "Available"));
        bookDatabase.add(new Book("CS-105", "Operating System Concepts", "Silberschatz & Galvin", "Computer Science", "Available"));
        bookDatabase.add(new Book("MA-201", "Discrete Mathematics and Its Applications", "Kenneth Rosen", "Mathematics", "Available"));
        bookDatabase.add(new Book("MA-202", "Linear Algebra and Its Applications", "Gilbert Strang", "Mathematics", "Issued"));
        bookDatabase.add(new Book("MA-203", "Calculus: Early Transcendentals", "James Stewart", "Mathematics", "Available"));
        bookDatabase.add(new Book("PH-301", "Concepts of Modern Physics", "Arthur Beiser", "Physics", "Available"));
        bookDatabase.add(new Book("PH-302", "Fundamentals of Physics", "Halliday & Resnick", "Physics", "Issued"));
        bookDatabase.add(new Book("LT-401", "To Kill a Mockingbird", "Harper Lee", "Literature", "Available"));
        bookDatabase.add(new Book("LT-402", "The Great Gatsby", "F. Scott Fitzgerald", "Literature", "Available"));
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 580);
        setMinimumSize(new Dimension(600, 400));
        setLocationRelativeTo(null);

        // Root container uses BorderLayout
        setLayout(new BorderLayout(8, 8));

        // ==========================================
        // 1. NORTH: Title Banner + Search Filter Bar
        // ==========================================
        JPanel northContainer = new JPanel(new BorderLayout());

        // Header Title
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(38, 50, 56));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
        JLabel lblTitle = new JLabel("CAMPUS LIBRARY CATALOG SEARCH & RESERVATION");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 17));
        titlePanel.add(lblTitle);
        northContainer.add(titlePanel, BorderLayout.NORTH);

        // Search Controls Panel (FlowLayout for responsive alignment)
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Filters"));

        searchPanel.add(new JLabel("Keyword (Title / Author):"));
        txtSearchKeyword = new JTextField(18);
        searchPanel.add(txtSearchKeyword);

        searchPanel.add(new JLabel("Category:"));
        String[] categories = {"All Categories", "Computer Science", "Mathematics", "Physics", "Literature"};
        cmbCategory = new JComboBox<>(categories);
        searchPanel.add(cmbCategory);

        btnSearch = new JButton("Search Books");
        btnSearch.setBackground(new Color(30, 136, 229));
        btnResetSearch = new JButton("Reset All");

        searchPanel.add(btnSearch);
        searchPanel.add(btnResetSearch);
        northContainer.add(searchPanel, BorderLayout.SOUTH);

        add(northContainer, BorderLayout.NORTH);

        // ==========================================
        // 2. CENTER: Resizable Results JTable in JScrollPane
        // ==========================================
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        String[] columnNames = {"Book ID", "Book Title", "Author", "Category", "Availability Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only for direct edits
            }
        };

        tblResults = new JTable(tableModel);
        tblResults.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblResults.setRowHeight(24);
        tblResults.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        tblResults.getTableHeader().setBackground(new Color(230, 235, 240));

        // Set column widths
        tblResults.getColumnModel().getColumn(0).setPreferredWidth(80);
        tblResults.getColumnModel().getColumn(1).setPreferredWidth(280);
        tblResults.getColumnModel().getColumn(2).setPreferredWidth(170);
        tblResults.getColumnModel().getColumn(3).setPreferredWidth(140);
        tblResults.getColumnModel().getColumn(4).setPreferredWidth(110);

        JScrollPane scrollPane = new JScrollPane(tblResults);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // ==========================================
        // 3. SOUTH: Navigation, Action Buttons & Status Bar
        // ==========================================
        JPanel southContainer = new JPanel(new BorderLayout());

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        btnPrev = new JButton("<< Previous Book");
        btnNext = new JButton("Next Book >>");
        btnReserve = new JButton("Reserve Selected Book");
        btnReserve.setBackground(new Color(46, 125, 50));

        actionPanel.add(btnPrev);
        actionPanel.add(btnReserve);
        actionPanel.add(btnNext);

        lblStatus = new JLabel(" Total Books in Catalog: " + bookDatabase.size(), JLabel.LEFT);
        lblStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));

        southContainer.add(actionPanel, BorderLayout.NORTH);
        southContainer.add(lblStatus, BorderLayout.SOUTH);
        add(southContainer, BorderLayout.SOUTH);

        // ==========================================
        // Event Listeners
        // ==========================================

        // Search Action
        btnSearch.addActionListener(e -> executeSearch());
        txtSearchKeyword.addActionListener(e -> executeSearch()); // Enter key trigger

        // Reset Action
        btnResetSearch.addActionListener(e -> {
            txtSearchKeyword.setText("");
            cmbCategory.setSelectedIndex(0);
            populateTable(bookDatabase);
            lblStatus.setText(" Filters cleared. Showing all " + bookDatabase.size() + " books.");
        });

        // Reserve Action
        btnReserve.addActionListener(e -> handleReservation());

        // Previous Row Navigation
        btnPrev.addActionListener(e -> {
            int selectedRow = tblResults.getSelectedRow();
            if (selectedRow > 0) {
                tblResults.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
                tblResults.scrollRectToVisible(tblResults.getCellRect(selectedRow - 1, 0, true));
                updateSelectionStatus();
            } else if (tblResults.getRowCount() > 0 && selectedRow == -1) {
                tblResults.setRowSelectionInterval(0, 0);
                updateSelectionStatus();
            }
        });

        // Next Row Navigation
        btnNext.addActionListener(e -> {
            int selectedRow = tblResults.getSelectedRow();
            if (selectedRow < tblResults.getRowCount() - 1 && selectedRow >= 0) {
                tblResults.setRowSelectionInterval(selectedRow + 1, selectedRow + 1);
                tblResults.scrollRectToVisible(tblResults.getCellRect(selectedRow + 1, 0, true));
                updateSelectionStatus();
            } else if (tblResults.getRowCount() > 0 && selectedRow == -1) {
                tblResults.setRowSelectionInterval(0, 0);
                updateSelectionStatus();
            }
        });

        // Selection Listener
        tblResults.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectionStatus();
            }
        });
    }

    /**
     * Populates JTable with the given list of books.
     */
    private void populateTable(List<Book> books) {
        tableModel.setRowCount(0);
        for (Book b : books) {
            tableModel.addRow(new Object[]{b.id, b.title, b.author, b.category, b.status});
        }
    }

    /**
     * Filters catalog based on keyword and category.
     */
    private void executeSearch() {
        String keyword = txtSearchKeyword.getText().trim().toLowerCase();
        String selectedCategory = (String) cmbCategory.getSelectedItem();

        List<Book> filtered = new ArrayList<>();
        for (Book b : bookDatabase) {
            boolean matchesCategory = selectedCategory.equals("All Categories") ||
                    b.category.equalsIgnoreCase(selectedCategory);
            boolean matchesKeyword = keyword.isEmpty() ||
                    b.title.toLowerCase().contains(keyword) ||
                    b.author.toLowerCase().contains(keyword) ||
                    b.id.toLowerCase().contains(keyword);

            if (matchesCategory && matchesKeyword) {
                filtered.add(b);
            }
        }

        populateTable(filtered);
        lblStatus.setText(String.format(" Search Results: Found %d matching book(s) for keyword: '%s' in category: '%s'",
                filtered.size(), keyword, selectedCategory));

        if (filtered.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No books found matching your search criteria.\nTry searching by broader terms or 'All Categories'.",
                    "No Matches", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Updates status bar when a row is selected.
     */
    private void updateSelectionStatus() {
        int row = tblResults.getSelectedRow();
        if (row != -1) {
            String title = (String) tableModel.getValueAt(row, 1);
            String status = (String) tableModel.getValueAt(row, 4);
            lblStatus.setText(String.format(" Selected: '%s' | Availability: %s", title, status));
        }
    }

    /**
     * Handles reserving the selected book.
     */
    private void handleReservation() {
        int row = tblResults.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a book from the table to reserve.",
                    "No Book Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String bookId = (String) tableModel.getValueAt(row, 0);
        String title = (String) tableModel.getValueAt(row, 1);
        String currentStatus = (String) tableModel.getValueAt(row, 4);

        if ("Reserved".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this,
                    "Book '" + title + "' is already reserved by another student.",
                    "Already Reserved", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("Issued".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this,
                    "Book '" + title + "' is currently Issued out.\nReservation placed on waitlist.",
                    "Waitlist Placed", JOptionPane.INFORMATION_MESSAGE);
            tableModel.setValueAt("Waitlisted", row, 4);
            updateBookStatusInMemory(bookId, "Waitlisted");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Reserve '" + title + "' (ID: " + bookId + ") for 48 hours?",
                "Confirm Reservation", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            tableModel.setValueAt("Reserved", row, 4);
            updateBookStatusInMemory(bookId, "Reserved");
            lblStatus.setText(" Book '" + title + "' reserved successfully!");
            JOptionPane.showMessageDialog(this,
                    "Success! Book '" + title + "' has been reserved.\nPlease collect it from the circulation counter within 48 hours.",
                    "Reservation Confirmed", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateBookStatusInMemory(String bookId, String newStatus) {
        for (Book b : bookDatabase) {
            if (b.id.equals(bookId)) {
                b.status = newStatus;
                break;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LibrarySearch screen = new LibrarySearch();
            screen.setVisible(true);
        });
    }
}
