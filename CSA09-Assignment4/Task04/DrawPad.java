package Task04;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * DrawPad - Buggy / Forensic Starter File for Task 4.
 * 
 * FORENSIC ANALYSIS BUGS PRESENT IN THIS STARTER FILE:
 * 1. Misspelled callback method: 'mouseClick' instead of 'mouseClicked' without @Override.
 *    Result: The event listener is never invoked by the JVM when user clicks.
 * 2. Drawing directly with getGraphics():
 *    Result: Points drawn vanish permanently whenever the window is minimized or resized.
 * 3. Click count label is never updated:
 *    Result: The counter on the UI remains stuck at 'Clicks: 0'.
 * 
 * See DrawPadCompleted.java for the fully fixed and complete implementation.
 */
public class DrawPad extends JFrame {

    private int clickCount = 0;
    private JLabel lblCount;
    private JPanel canvasPanel;

    // TODO 1: Data structure to retain drawing state across repaints
    // private ArrayList<Point> pointList = new ArrayList<>();

    public DrawPad() {
        super("DrawPad (Buggy Forensic Starter Version)");
        initComponents();
    }

    private void initComponents() {
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        lblCount = new JLabel("Clicks: 0", JLabel.CENTER);
        lblCount.setFont(new Font("SansSerif", Font.BOLD, 14));
        add(lblCount, BorderLayout.NORTH);

        canvasPanel = new JPanel();
        canvasPanel.setBackground(Color.WHITE);

        // --- BUGGY LISTENER IMPLEMENTATION ---
        canvasPanel.addMouseListener(new MouseAdapter() {
            // BUG #1: Misspelled method name (mouseClick instead of mouseClicked).
            // Without @Override, the compiler treats this as a regular method and not an event handler!
            public void mouseClick(MouseEvent e) {
                // BUG #2: Drawing directly via getGraphics() instead of paintComponent()
                Graphics g = canvasPanel.getGraphics();
                if (g != null) {
                    g.setColor(Color.RED);
                    g.fillOval(e.getX() - 5, e.getY() - 5, 10, 10);
                    g.dispose();
                }

                // BUG #3: clickCount is incremented but lblCount.setText() is omitted!
                clickCount++;

                // TODO 2: Store clicked point in ArrayList and call repaint()
            }
        });

        add(canvasPanel, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DrawPad pad = new DrawPad();
            pad.setVisible(true);
        });
    }
}
