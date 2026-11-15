# TASK 5: CURRENCY CONVERTER (FOUR EVENT-HANDLING PARADIGMS)

## 1. Aim
To construct four distinct, fully functioning Java Swing Currency Converter applications, each demonstrating a different foundational paradigm of event handling in Java:
1. **Frame Implements ActionListener** (`ConverterFrameListener.java`)
2. **Named Inner Class** (`ConverterInnerClass.java`)
3. **Anonymous Inner Class** (`ConverterAnonymous.java`)
4. **Lambda Expression** (`ConverterLambda.java`)

All four programs implement identical functional requirements: multi-currency conversion, numeric input validation, Enter-key default button triggering, formatted results dialogs, and window-closing confirmation via `WindowAdapter`.

---

## 2. Requirements
- **Language / Runtime:** Java Standard Edition (JDK 8+ / OpenJDK 26 tested)
- **GUI Toolkit:** Java Swing (`JFrame`, `JTextField`, `JComboBox`, `JButton`, `JLabel`, `JOptionPane`, `JRootPane`)
- **Event Classes:** `ActionEvent`, `ActionListener`, `WindowEvent`, `WindowAdapter`
- **Exchange Rates Supported:** USD, EUR, GBP, INR, JPY, CAD

---

## 3. Comprehensive Comparison of the Four Approaches

| Feature / Aspect | Approach 1: Frame Implements `ActionListener` | Approach 2: Named Inner Class | Approach 3: Anonymous Inner Class | Approach 4: Lambda Expression |
|------------------|-----------------------------------------------|------------------------------|-----------------------------------|-------------------------------|
| **Primary Code Style** | Monolithic `public class Frame implements ActionListener` | `private class Handler implements ActionListener` | `btn.addActionListener(new ActionListener() {...})` | `btn.addActionListener(e -> action())` |
| **Boilerplate Overhead** | Low to medium | Highest (requires full class declarations) | Medium (repeats interface and method signatures) | **Minimal** (cleanest single-line syntax) |
| **Event Discrimination** | Requires manual `if (e.getSource() == btn)` branching. | Distinct class per action; zero `if-else` branching. | Distinct block per component; zero `if-else` branching. | Direct method delegate; zero `if-else` branching. |
| **Reusability** | Limited to the enclosing frame. | High: inner class can be reused across multiple buttons/menus. | Zero: tightly coupled to the exact registration line. | High if delegated to a helper method or stored as a variable. |
| **Compilation Artifacts** | Only 1 class file (`ConverterFrameListener.class`). | Separate named class files (`ConverterInnerClass$ConvertHandler.class`). | Generates synthetic numbered class files (`ConverterAnonymous$1.class`). | **Zero separate class files** at compile time (uses JVM `invokedynamic` via `LambdaMetafactory`). |
| **Access to Enclosing Scope**| Direct `this` access. | Direct access to outer private fields via synthetic accessor. | Direct access to outer fields; local variables must be `final` / effectively final. | Direct access to outer fields; local variables must be effectively final. |

---

## 4. Advanced Concepts: Method References & Shared Action Objects

### 4.1 Method References (`ClassName::methodName`)
Introduced in Java 8 alongside Lambdas, method references provide an even more compact notation when a lambda simply forwards its argument to an existing method:
```java
// Lambda:
btnConvert.addActionListener(e -> executeConversion(e));

// Equivalent Method Reference:
btnConvert.addActionListener(this::executeConversion);
```
- **Advantages:** Eliminates redundant parameter naming, reduces visual clutter, and leverages JVM method handles for optimal execution speed.

### 4.2 Shared `Action` Objects (`javax.swing.Action` / `AbstractAction`)
In enterprise Swing development, an action often needs to be triggered from multiple UI entry points simultaneously:
1. A toolbar `JButton`
2. A dropdown `JMenuItem`
3. A keyboard accelerator (e.g. `Ctrl + Enter` or `Alt + C`)

Instead of writing separate `ActionListeners`, Swing provides the `Action` interface:
```java
Action convertAction = new AbstractAction("Convert Currency") {
    @Override
    public void actionPerformed(ActionEvent e) {
        executeConversion();
    }
};

// Bind identical action across multiple triggers:
btnConvert.setAction(convertAction);
menuItemConvert.setAction(convertAction);
rootPane.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "doConvert");
rootPane.getActionMap().put("doConvert", convertAction);
```
- When `convertAction.setEnabled(false)` is called, all associated buttons, menu items, and keyboard shortcuts disable in unison.

---

## 5. Completion of TODO 1 and TODO 2

### TODO 1: Validation and Exchange Calculation
Implemented consistently across all four programs:
```java
String input = txtAmount.getText().trim();
if (input.isEmpty()) {
    JOptionPane.showMessageDialog(this, "Please enter an amount.", "Input Error", JOptionPane.WARNING_MESSAGE);
    return;
}
double amount = Double.parseDouble(input);
if (amount < 0) throw new NumberFormatException("Negative amount");

double fromRate = EXCHANGE_RATES.get(fromCur);
double toRate = EXCHANGE_RATES.get(toCur);
double converted = (amount / fromRate) * toRate;
```

### TODO 2: Enter Key Triggering & Window Closing Handler
```java
// Default Button binds Enter key anywhere in the frame to btnConvert:
getRootPane().setDefaultButton(btnConvert);
txtAmount.addActionListener(e -> executeConversion());

// WindowAdapter intercepts window close:
setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
addWindowListener(new WindowAdapter() {
    @Override
    public void windowClosing(WindowEvent e) {
        int opt = JOptionPane.showConfirmDialog(Converter.this, "Exit?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }
});
```

---

## 6. Big-O Complexity Analysis

| Operation | Time Complexity | Space Complexity |
|-----------|-----------------|------------------|
| **Rate Map Lookup (`EXCHANGE_RATES.get`)** | $\mathcal{O}(1)$ average | $\mathcal{O}(K)$ where $K = 6$ currencies |
| **Number Parsing (`Double.parseDouble`)** | $\mathcal{O}(D)$ where $D$ is digit count ($\approx \mathcal{O}(1)$) | $\mathcal{O}(1)$ auxiliary memory |
| **Currency Conversion Arithmetic** | $\mathcal{O}(1)$ constant time | $\mathcal{O}(1)$ |
| **Event Dispatch Loop** | $\mathcal{O}(1)$ | $\mathcal{O}(1)$ |

---

## 7. Sample Output

### GUI Result Display Label:
```
Converted Amount: Result: 100.00 USD = 8325.00 INR
```

### Dialog Box Popup:
```
----------------------------------------
             Conversion Result          
----------------------------------------
        100.00 USD = 8325.00 INR        
                 [ OK ]                 
----------------------------------------
```

---

## 8. Test Cases

| Test ID | Test Scenario | Inputs | Expected Output | Pass/Fail |
|---------|---------------|--------|-----------------|-----------|
| **TC-01** | Valid USD to INR | Amount: `100`, From: USD, To: INR | Dialog & label: "100.00 USD = 8325.00 INR" | PASS |
| **TC-02** | Valid EUR to JPY | Amount: `50`, From: EUR, To: JPY | Correct cross-rate conversion via base USD. | PASS |
| **TC-03** | Blank Amount | Amount: `""` $\to$ Press Enter | Warning dialog: "Please enter an amount to convert." Focus restored to input. | PASS |
| **TC-04** | Alphanumeric Letters | Amount: `"50dollars"` | Error dialog: "Invalid number format: '50dollars'." Input highlighted. | PASS |
| **TC-05** | Negative Amount | Amount: `"-25"` | Error dialog: "Amount cannot be negative." | PASS |
| **TC-06** | Reset Button | Click "Reset" | Amount cleared, result reset to `---`, currency set to default. | PASS |
| **TC-07** | Window Close Confirmation | Click `[X]` | Dialog: "Exit Currency Converter?" $\to$ Yes closes, No stays open. | PASS |

---

## 9. Compilation, Execution & Screenshot Instructions

### Compilation (All 4 Programs):
```bash
javac Task05/*.java
```

### Execution:
```bash
# 1. Frame Listener
java Task05.ConverterFrameListener

# 2. Named Inner Class
java Task05.ConverterInnerClass

# 3. Anonymous Inner Class
java Task05.ConverterAnonymous

# 4. Lambda Expression
java Task05.ConverterLambda
```

### Screenshot Steps to Capture:
1. **Program 1 (Frame Listener):** Launch and enter `100` USD to INR; press Enter; capture the dialog box.
2. **Program 2 (Named Inner Class):** Launch and convert EUR to USD; capture the result display.
3. **Program 3 (Anonymous Inner Class):** Enter invalid characters (e.g. `abc`); capture the error dialog.
4. **Program 4 (Lambda Expression):** Click the close box `[X]`; capture the `WindowAdapter` exit confirmation dialog.
