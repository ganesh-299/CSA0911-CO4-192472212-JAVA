package Task05;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Task 5 - Program 4: ConverterLambda
 * Implementation Approach: Uses modern Java Lambda Expressions (e -> ...).
 * 
 * TODO 1: Implement robust input parsing, validation and exchange rate conversion logic.
 * TODO 2: Configure Enter key triggering (Default Button) and WindowAdapter close handling.
 */
public class ConverterLambda extends JFrame {

    private JTextField txtAmount;
    private JComboBox<String> cmbFromCurrency;
    private JComboBox<String> cmbToCurrency;
    private JButton btnConvert;
    private JButton btnReset;
    private JLabel lblResultDisplay;

    private static final Map<String, Double> EXCHANGE_RATES = new HashMap<>();
    static {
        EXCHANGE_RATES.put("USD - US Dollar", 1.0);
        EXCHANGE_RATES.put("EUR - Euro", 0.92);
        EXCHANGE_RATES.put("GBP - British Pound", 0.79);
        EXCHANGE_RATES.put("INR - Indian Rupee", 83.25);
        EXCHANGE_RATES.put("JPY - Japanese Yen", 155.50);
        EXCHANGE_RATES.put("CAD - Canadian Dollar", 1.36);
    }

    public ConverterLambda() {
        super("Currency Converter (Approach 4: Lambda Expression)");
        initComponents();
    }

    private void initComponents() {
        setSize(520, 380);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(194, 24, 91));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
        JLabel lblTitle = new JLabel("CURRENCY CONVERTER - LAMBDA EXPRESSION");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        headerPanel.add(lblTitle);
        add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 20, 10, 20),
                BorderFactory.createTitledBorder("Currency Exchange Input")));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] currencies = EXCHANGE_RATES.keySet().toArray(new String[0]);
        txtAmount = new JTextField(12);
        cmbFromCurrency = new JComboBox<>(currencies);
        cmbToCurrency = new JComboBox<>(currencies);
        cmbToCurrency.setSelectedItem("JPY - Japanese Yen");

        lblResultDisplay = new JLabel("Converted Amount: ---", JLabel.CENTER);
        lblResultDisplay.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblResultDisplay.setForeground(new Color(173, 20, 87));

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Enter Amount:"), gbc);
        gbc.gridx = 1; formPanel.add(txtAmount, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("From Currency:"), gbc);
        gbc.gridx = 1; formPanel.add(cmbFromCurrency, gbc);

        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("To Currency:"), gbc);
        gbc.gridx = 1; formPanel.add(cmbToCurrency, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        formPanel.add(lblResultDisplay, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnConvert = new JButton("Convert");
        btnConvert.setBackground(new Color(216, 27, 96));
        btnReset = new JButton("Reset");

        buttonPanel.add(btnConvert);
        buttonPanel.add(btnReset);
        add(buttonPanel, BorderLayout.SOUTH);

        // Modern Lambda Expressions (concise, functional, zero boilerplate)
        btnConvert.addActionListener(e -> executeConversion());
        txtAmount.addActionListener(e -> executeConversion());

        btnReset.addActionListener(e -> {
            txtAmount.setText("");
            lblResultDisplay.setText("Converted Amount: ---");
            cmbFromCurrency.setSelectedIndex(0);
            cmbToCurrency.setSelectedItem("JPY - Japanese Yen");
            txtAmount.requestFocus();
        });

        // TODO 2 COMPLETED: Enter Key Default Button & WindowAdapter
        getRootPane().setDefaultButton(btnConvert);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int option = JOptionPane.showConfirmDialog(ConverterLambda.this,
                        "Exit Currency Converter?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    dispose();
                    System.exit(0);
                }
            }
        });
    }

    /**
     * TODO 1 COMPLETED: Validation, error checking and rate conversion.
     */
    private void executeConversion() {
        String input = txtAmount.getText().trim();
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an amount to convert.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            txtAmount.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(input);
            if (amount < 0) {
                JOptionPane.showMessageDialog(this, "Amount cannot be negative.",
                        "Negative Value", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number format: '" + input + "'.",
                    "Parse Error", JOptionPane.ERROR_MESSAGE);
            txtAmount.selectAll();
            txtAmount.requestFocus();
            return;
        }

        String fromCur = (String) cmbFromCurrency.getSelectedItem();
        String toCur = (String) cmbToCurrency.getSelectedItem();

        double fromRate = EXCHANGE_RATES.get(fromCur);
        double toRate = EXCHANGE_RATES.get(toCur);
        double converted = (amount / fromRate) * toRate;

        String resultMsg = String.format("%.2f %s = %.2f %s",
                amount, fromCur.substring(0, 3), converted, toCur.substring(0, 3));

        lblResultDisplay.setText("Result: " + resultMsg);
        JOptionPane.showMessageDialog(this, resultMsg, "Conversion Result", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ConverterLambda().setVisible(true);
        });
    }
}
