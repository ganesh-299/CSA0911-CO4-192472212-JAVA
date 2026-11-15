# TASK 4: EVENT-HANDLING FORENSICS (DrawPad)

## 1. Aim
To conduct an in-depth forensic investigation of an erroneous Java GUI graphics application (`DrawPad.java`), diagnose three critical defects (method signature mismatch, lost drawings due to `getGraphics()`, and missing label updates), implement the corrected model-driven solution (`DrawPadCompleted.java`), complete TODO 1 and TODO 2, and evaluate offscreen rendering alternatives (`BufferedImage`).

---

## 2. Requirements
- **Language / Runtime:** Java Standard Edition (JDK 8+ / OpenJDK 26 tested)
- **GUI Toolkit:** Java Swing (`JFrame`, `JPanel`, `JLabel`, `JButton`, `JComboBox`) and Java AWT Graphics (`Graphics`, `Graphics2D`, `Point`, `Color`, `RenderingHints`)
- **Key Files:**
  - `DrawPad.java`: Buggy starter code illustrating common beginner pitfalls.
  - `DrawPadCompleted.java`: Fully corrected, robust, model-driven graphics application.

---

## 3. Forensic Analysis: Three Critical Defects

### 3.1 The `mouseClicked` Spelling Mistake & Why `@Override` Matters
- **The Defect:** In `DrawPad.java`, the listener method was spelled as `public void mouseClick(MouseEvent e)`.
- **Why It Fails Silently:**
  - `MouseAdapter` implements `MouseListener`, which defines the exact method `public void mouseClicked(MouseEvent e)`.
  - When the user clicks the mouse, the JVM Event Dispatcher specifically calls `mouseClicked(e)`.
  - Because `mouseClick` was spelled with a missing `'ed'`, Java's compiler treated it as an ordinary, unrelated method that just happens to exist.
  - The default empty `mouseClicked()` inside `MouseAdapter` was called instead, causing user clicks to be completely ignored without any runtime exception or warning!
- **The Fix with `@Override`:**
  - Annotating the method with `@Override` tells the Java compiler: *"Ensure this method signature overrides a method in a superclass or interface."*
  - If a developer misspells `mouseClick` with `@Override`, javac immediately halts with a compiler error:
    `error: method does not override or implement a method from a supertype`
  - This turns an insidious silent runtime bug into a clear compile-time error.

---

### 3.2 Why the Click Counter Label Does Not Update
- **The Defect:** In `DrawPad.java`, the variable `clickCount` was incremented via `clickCount++`, but the GUI label `lblCount` never reflected the change.
- **Why It Fails:**
  - Swing components do not automatically bind to primitive Java variables.
  - Mutating an integer variable in memory does not trigger any GUI repaint or text update on `JLabel`.
- **The Fix:**
  - Every time `clickCount` is modified, explicitly call:
    ```java
    lblCount.setText("Clicks Recorded: " + clickCount + " | Total Points: " + pointList.size());
    ```

---

### 3.3 Why Drawing Directly Using `getGraphics()` is Catastrophic
- **The Defect:** In `DrawPad.java`, drawing was executed directly via:
  ```java
  Graphics g = canvasPanel.getGraphics();
  g.fillOval(x, y, 10, 10);
  g.dispose();
  ```
- **Why It Fails (The Painting Cycle in OS/Swing):**
  - `getGraphics()` yields an ephemeral, temporary graphics context writing directly to the current screen pixel buffer.
  - Desktop operating systems do not preserve component pixels. Whenever a window is:
    1. Minimized and restored,
    2. Resized,
    3. Covered/obscured by another application window, or
    4. Scrolled or dragged across monitors,
    the OS Window Manager invalidates the damaged region and sends a repaint request (`PAINT` event) to the AWT EventQueue.
  - Swing calls the component's `paintComponent(Graphics g)` method to redraw the component. The default `paintComponent()` fills the canvas with the background color (`Color.WHITE`).
  - Because drawings made via `getGraphics()` were never recorded in a data model, **every dot previously drawn is permanently lost!**
- **The Correct Architecture:**
  - Separate **State** from **Rendering**:
    1. Store coordinates in an in-memory collection (`ArrayList<Point> pointList`).
    2. When a click occurs, append `e.getPoint()` to `pointList` and call `canvasPanel.repaint()`.
    3. Override `paintComponent(Graphics g)` on the custom `JPanel`. In `paintComponent`, call `super.paintComponent(g)` and iterate through `pointList`, redrawing every point.
    4. Even if the window is minimized or resized 100 times, `paintComponent` reads from `pointList` and perfectly restores every circle.

---

## 4. Completion of TODO 1 and TODO 2

### TODO 1: Data Structure to Store State
```java
// TODO 1 COMPLETED: In DrawPadCompleted.java
private final ArrayList<Point> pointList = new ArrayList<>();
private final ArrayList<Color> colorList = new ArrayList<>();
```

### TODO 2: Capture Event and Trigger Paint Cycle
```java
// TODO 2 COMPLETED: Inside mouseClicked()
@Override
public void mouseClicked(MouseEvent e) {
    pointList.add(e.getPoint());
    colorList.add(getSelectedColor());
    clickCount++;
    lblCount.setText("Clicks Recorded: " + clickCount + " | Total Points on Canvas: " + pointList.size());
    canvasPanel.repaint();
}
```

---

## 5. Alternative Concept: `BufferedImage` Off-Screen Canvas vs `ArrayList<Point>`

Instead of storing an ever-growing list of `Point` objects, an alternative design is to maintain an offscreen raster image using `java.awt.image.BufferedImage`.

```java
// Alternative Offscreen Buffer
BufferedImage offscreenImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
Graphics2D g2d = offscreenImage.createGraphics();

// On click:
g2d.fillOval(x, y, radius, radius);
canvas.repaint();

// In paintComponent(Graphics g):
super.paintComponent(g);
g.drawImage(offscreenImage, 0, 0, null);
```

### Big-O Complexity Comparison:

| Metric | Model-Driven `ArrayList<Point>` | Off-Screen `BufferedImage` |
|--------|---------------------------------|----------------------------|
| **Add Point Time Complexity** | **$\mathcal{O}(1)$** (append to list) | **$\mathcal{O}(1)$** (rasterize pixel circle into buffer) |
| **Repaint Time Complexity** | **$\mathcal{O}(N)$** (must iterate through all $N$ points in the list) | **$\mathcal{O}(1)$** (constant time bit-block transfer / blit of the raster image regardless of how many dots exist!) |
| **Space Complexity** | **$\mathcal{O}(N)$** (grows linearly with number of points $N$; each `Point` consumes $\approx 24$ bytes) | **$\mathcal{O}(W \times H)$** fixed space (proportional to canvas dimensions; e.g. $800 \times 600 \times 4$ bytes $\approx 1.92$ MB fixed) |
| **Scalability** | For $N = 100,000$ points, repaints slow down noticeably ($\mathcal{O}(N)$). | Retains uniform 60 FPS repainting even after millions of paint clicks. |
| **Editability / Undo** | Trivial to remove the last point (`pointList.remove(pointList.size() - 1)`). | Harder to undo individual strokes without storing full undo image states. |

---

## 6. Sample Output

```
Clicks Recorded: 5 | Total Points on Canvas: 5
Canvas rendered with dots at:
Point 1: (124, 88)  - Crimson Red
Point 2: (210, 145) - Royal Blue
Point 3: (340, 290) - Forest Green
Point 4: (415, 112) - Purple
Point 5: (180, 320) - Dark Orange
```

---

## 7. Test Cases

| Test ID | Test Scenario | Actions Performed | Expected Behavior | Pass/Fail |
|---------|---------------|-------------------|-------------------|-----------|
| **TC-01** | Mouse Click Detection | Click anywhere on canvas | Dot appears at exact mouse coordinates; click counter increments by 1. | PASS |
| **TC-02** | Color Switching | Choose "Forest Green", click canvas | Subsequent dots render in green; previous dots retain their original colors. | PASS |
| **TC-03** | Window Minimization | Click 10 dots, minimize window, restore | All 10 dots remain intact and crisp. (Proves `paintComponent` model persistence). | PASS |
| **TC-04** | Canvas Resizing | Click dots, expand window from $700\times550$ to $1000\times800$ | Dots maintain position; white canvas fills space; no dots are erased. | PASS |
| **TC-05** | Clear Canvas | Click "Clear Canvas" button | Canvas wiped clean; counter resets to 0. | PASS |

---

## 8. Compilation, Execution & Screenshot Instructions

### Compilation:
```bash
javac Task04/DrawPad.java Task04/DrawPadCompleted.java
```

### Execution:
```bash
# Run the corrected application
java Task04.DrawPadCompleted
```

### Screenshot Steps to Capture:
1. **Initial Screen:** Launch `DrawPadCompleted`; capture initial empty state showing counter at 0.
2. **Multiple Colored Dots:** Click in several locations using different colors; capture canvas showing colored dots and updated counter.
3. **Repaint Verification:** Minimize the application window to the taskbar, then restore it; capture the window proving all dots persist.
