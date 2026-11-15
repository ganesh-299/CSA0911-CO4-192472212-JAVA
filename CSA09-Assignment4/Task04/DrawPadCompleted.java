package Task04;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * DrawPadCompleted - Fully corrected and robust implementation of Task 4.
 * 
 * RESOLVED FORENSIC DEFECTS:
 * 1. Corrected 'mouseClick' spelling to 'mouseClicked' and annotated with @Override.
 *    The @Override annotation forces the compiler to verify that the method matches
 *    the exact signature of MouseListener / MouseAdapter.
 * 2. Eliminated getGraphics() direct drawing. Replaced with model-driven rendering:
 *    Clicked coordinates are stored in an ArrayList<Point> (TODO 1), and painted
 *    inside an overridden paintComponent(Graphics g) method.
 * 3. Fixed click counter label update:
 *    lblCount.setText(...) is now called on every click to reflect live click tally.
 * 4. Completed TODO 1 and TODO 2.
 */
public class DrawPadCompleted extends JFrame {

    private int clickCount = 0;
    private JLabel lblCount;
    private DrawingCanvas canvasPanel;
    private JButton btnClear;
    private JComboBox<String> cmbColor;

    // TODO 1 COMPLETED: Data structure to store persistent drawing coordinates across repaints
    private final ArrayList<Point> pointList = new ArrayList<>();
    private final ArrayList<Color> colorList = new ArrayList<>();

    public DrawPadCompleted() {
        super("DrawPad (Completed & Corrected Event-Driven GUI)");
        initComponents();
    }

    private void initComponents() {
        setSize(700, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // North: Top Counter Banner
        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        northPanel.setBackground(new Color(240, 244, 248));
        lblCount = new JLabel("Clicks Recorded: 0 | Total Points on Canvas: 0", JLabel.CENTER);
        lblCount.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblCount.setForeground(new Color(33, 33, 33));
        northPanel.add(lblCount);
        add(northPanel, BorderLayout.NORTH);

        // Center: Custom Drawing Canvas
        canvasPanel = new DrawingCanvas();
        canvasPanel.setBackground(Color.WHITE);

        // Mouse Listener with correct method name and @Override
        canvasPanel.addMouseListener(new MouseAdapter() {
            /**
             * @Override ensures compiler checks that mouseClicked correctly overrides
             * MouseAdapter's callback method. If misspelled, compilation fails immediately.
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO 2 COMPLETED:
                // 1. Capture click point and chosen drawing color
                pointList.add(e.getPoint());
                colorList.add(getSelectedColor());

                // 2. Increment counter and update GUI label
                clickCount++;
                lblCount.setText("Clicks Recorded: " + clickCount + " | Total Points on Canvas: " + pointList.size());

                // 3. Request JVM to repaint canvas via paintComponent()
                canvasPanel.repaint();
            }
        });

        add(canvasPanel, BorderLayout.CENTER);

        // South: Control Bar
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        southPanel.setBackground(new Color(240, 244, 248));

        southPanel.add(new JLabel("Dot Color:"));
        cmbColor = new JComboBox<>(new String[]{"Crimson Red", "Royal Blue", "Forest Green", "Purple", "Dark Orange"});
        southPanel.add(cmbColor);

        btnClear = new JButton("Clear Canvas");
        btnClear.addActionListener(e -> {
            pointList.clear();
            colorList.clear();
            clickCount = 0;
            lblCount.setText("Clicks Recorded: 0 | Total Points on Canvas: 0");
            canvasPanel.repaint();
        });
        southPanel.add(btnClear);

        add(southPanel, BorderLayout.SOUTH);
    }

    private Color getSelectedColor() {
        String choice = (String) cmbColor.getSelectedItem();
        if (choice == null) return Color.RED;
        switch (choice) {
            case "Royal Blue": return new Color(25, 118, 210);
            case "Forest Green": return new Color(56, 142, 60);
            case "Purple": return new Color(123, 31, 162);
            case "Dark Orange": return new Color(245, 124, 0);
            default: return new Color(211, 47, 47); // Crimson Red
        }
    }

    /**
     * Custom JPanel overriding paintComponent() to ensure persistent, robust rendering.
     */
    private class DrawingCanvas extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            // Must always invoke super.paintComponent(g) to clear previous background
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;
            // Enable smooth anti-aliased circle edges
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int radius = 10;
            // Iterate over all stored points and draw each circle
            for (int i = 0; i < pointList.size(); i++) {
                Point p = pointList.get(i);
                Color c = (i < colorList.size()) ? colorList.get(i) : Color.RED;

                g2d.setColor(c);
                g2d.fillOval(p.x - radius / 2, p.y - radius / 2, radius, radius);

                // Optional subtle border around each dot
                g2d.setColor(c.darker());
                g2d.drawOval(p.x - radius / 2, p.y - radius / 2, radius, radius);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DrawPadCompleted pad = new DrawPadCompleted();
            pad.setVisible(true);
        });
    }
}
