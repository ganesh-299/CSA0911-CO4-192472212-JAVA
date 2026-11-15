# TASK 1: GUI DESIGN REVIEW & REDESIGN

## 1. Aim
To perform a critical forensic evaluation of a poorly structured, monolithic Java GUI application (`FeeWindow`), identify five major design deficiencies and their potential failure modes, and engineer a robust, decoupled, and maintainable redesign utilizing Object-Oriented principles, the Model-View-Controller (MVC) pattern (`FeeWindow` and `FeeService`), and distinct event handlers.

---

## 2. Requirements
- **Language / Runtime:** Java Standard Edition (JDK 8+ / OpenJDK 26 tested)
- **GUI Toolkit:** Java Swing (`javax.swing.*`) and AWT Layouts (`java.awt.*`)
- **Key Classes:**
  - `FeeService`: Encapsulates business logic, arithmetic calculation, validation, and receipt formatting.
  - `FeeWindow`: Presentation view managing inputs, panels, layout managers, and user interactions.
  - `PayHandler`, `ClearHandler`, `ReceiptHandler`: Dedicated `ActionListener` implementations.

---

## 3. Explanation: Five Design Deficiencies in Legacy GUI

| # | Design Deficiency | What Could Go Wrong (Failure Mode) |
|---|-------------------|-----------------------------------|
| **1** | **Monolithic Architecture / SRP Violation**<br>Business logic, fee calculations, validation, and GUI components were tightly coupled in a single class. | Any modification to fee calculation formulas or tax rules risks breaking the GUI layout. Unit testing the calculation logic without instantiating a heavy GUI window is impossible. |
| **2** | **Fragile Monolithic Action Listener**<br>A single `actionPerformed(ActionEvent e)` handler with nested `if-else if` blocks matching string commands (e.g., `e.getActionCommand().equals("Pay")`). | If a button label is modified (e.g., from "Pay" to "Pay Fee" or for localization/internationalization), the event matching fails silently. The button click does nothing without any compiler warning. |
| **3** | **Lack of Input Validation & Unhandled Exceptions**<br>Text field inputs were directly parsed with `Double.parseDouble(txtField.getText())` without checks. | Entering empty strings, whitespace, or letters triggers unhandled `NumberFormatException`. The program crashes or leaves the GUI in an unresponsive, corrupt state. |
| **4** | **Hardcoded Magic Numbers**<br>Discount percentages and base fees were hardcoded literals inside event blocks. | Changing fee structures requires hunting down arbitrary magic numbers across the codebase, introducing human error and inconsistent calculations. |
| **5** | **Thread Safety Violation (Ignoring EDT)**<br>Swing components were instantiated and made visible directly on the `main` thread instead of the Event Dispatch Thread (`SwingUtilities.invokeLater`). | Can lead to race conditions, visual rendering glitches, deadlocks, and unpredictable component behavior under multi-threaded operating systems. |

---

## 4. Architecture & Class Diagram

```
+-------------------------------------------------------------+
|                         FeeWindow                           |
|-------------------------------------------------------------|
| - txtRollNo : JTextField                                   |
| - txtName : JTextField                                      |
| - cmbCourse : JComboBox<String>                             |
| - cmbSemester : JComboBox<String>                           |
| - txtBaseTuition : JTextField                               |
| - txtScholarship : JTextField                               |
| - txtLateFine : JTextField                                  |
| - txtReceiptArea : JTextArea                                |
| - btnPay, btnClear, btnReceipt : JButton                    |
| - lblStatus : JLabel                                        |
| - feeService : FeeService                                   |
|-------------------------------------------------------------|
| + FeeWindow()                                               |
| - initComponents() : void                                   |
| + main(args : String[]) : void                              |
+-------------------------------------------------------------+
        |                 |                  |
   uses |            uses |             uses |
        v                 v                  v
+---------------+ +----------------+ +------------------+
|  PayHandler   | |  ClearHandler  | |  ReceiptHandler  |
+---------------+ +----------------+ +------------------+
        \                 |                 /
         -----> Delegates calculations to -
                          |
                          v
        +-----------------------------------+
        |            FeeService             |
        |-----------------------------------|
        | - lastPaymentSuccessful : boolean |
        | - lastReceiptText : String        |
        |-----------------------------------|
        | + validateInputs(...) : String    |
        | + calculateNetFee(...) : double   |
        | + processPayment(...) : boolean   |
        | + generateReceipt(...) : String   |
        | + reset() : void                  |
        +-----------------------------------+
```

---

## 5. Algorithm

### Algorithm for Pay Action (`PayHandler`)
1. Read `rollNo` and `name` from input text fields.
2. Call `feeService.validateInputs(rollNo, name)`. If an error string is returned, show `JOptionPane.WARNING_MESSAGE` and exit.
3. Attempt to parse `baseTuition`, `scholarshipPercent`, and `lateFine` to `double` inside a `try-catch` block:
   - If parsing fails, catch `NumberFormatException`, display an error dialog, and exit.
   - If numbers are negative or scholarship > 100%, display validation warning and exit.
4. Delegate calculation to `feeService.calculateNetFee(...)`.
5. Display confirmation dialog (`JOptionPane.showConfirmDialog`) asking student confirmation.
6. If confirmed:
   - Invoke `feeService.processPayment(...)`.
   - Enable `btnReceipt`.
   - Invoke `feeService.generateReceipt(...)` and update `txtReceiptArea`.
   - Update `lblStatus` to show success.

### Algorithm for Clear Action (`ClearHandler`)
1. Reset all `JTextField` values to default or empty strings.
2. Reset `JComboBox` selections to index 0.
3. Reset `txtReceiptArea` and disable `btnReceipt`.
4. Call `feeService.reset()`.
5. Request focus on `txtRollNo`.

---

## 6. Pseudocode

```text
CLASS FeeService:
    FUNCTION calculateNetFee(baseTuition, scholarship, lateFine):
        discount = (baseTuition * scholarship) / 100.0
        netAmount = (baseTuition - discount) + lateFine
        RETURN netAmount
    END FUNCTION

CLASS PayHandler IMPLEMENTS ActionListener:
    METHOD actionPerformed(event):
        rollNo = txtRollNo.getText().trim()
        name = txtName.getText().trim()
        
        IF NOT valid(rollNo, name) THEN
            SHOW ERROR "Invalid student details"
            RETURN
        END IF
        
        TRY:
            tuition = PARSE_DOUBLE(txtBaseTuition.getText())
            scholarship = PARSE_DOUBLE(txtScholarship.getText())
            fine = PARSE_DOUBLE(txtLateFine.getText())
        CATCH NumberFormatException:
            SHOW ERROR "Please enter valid numeric figures"
            RETURN
        END TRY
        
        netFee = FeeService.calculateNetFee(tuition, scholarship, fine)
        IF USER_CONFIRMED("Pay $" + netFee) THEN
            FeeService.processPayment(rollNo, netFee)
            txtReceiptArea.setText(FeeService.generateReceipt(...))
            btnReceipt.setEnabled(TRUE)
        END IF
    END METHOD
```

---

## 7. Sample Text Output

```
====================================================
            OFFICIAL COLLEGE FEE RECEIPT             
====================================================
Date & Time   : 15-Nov-2026 13:30:00
Roll Number   : 21CS101
Student Name  : John Doe
Course/Dept   : Computer Science & Engg
Semester      : Semester 4
----------------------------------------------------
Base Tuition  : $ 45000.00
Scholarship   : 10.0% (-$ 4500.00)
Late Fine     : +$ 500.00
----------------------------------------------------
Total Amount  : $ 41000.00
Payment Status: SUCCESSFUL / PAID
Transaction ID: TXN1763193600000
====================================================
```

---

## 8. Test Cases

| Test ID | Test Scenario | Inputs (Roll, Name, Tuition, Sch%, Fine) | Expected Outcome | Pass/Fail |
|---------|---------------|------------------------------------------|------------------|-----------|
| **TC-01** | Standard Normal Payment | `21CS042`, `Alice Smith`, `50000`, `10`, `0` | Calculated: $45000.00. Confirmation shown, payment successful, receipt populated. | PASS |
| **TC-02** | Empty Student Roll/Name | `""`, `""`, `50000`, `0`, `0` | Warning: "Roll Number cannot be empty." No calculation executed. | PASS |
| **TC-03** | Non-Numeric Amount Input | `21CS042`, `Alice`, `"abc"`, `0`, `0` | Caught `NumberFormatException`. Error dialog: "Please enter valid numeric values". | PASS |
| **TC-04** | Invalid Scholarship Range | `21CS042`, `Alice`, `50000`, `150`, `0` | Warning: "Scholarship must be between 0 and 100%." | PASS |
| **TC-05** | Form Clear Action | Click "Clear Form" button | All text boxes wiped, receipt cleared, Receipt button disabled. | PASS |

---

## 9. Advantages of the Redesign
1. **Maintainability:** Business logic in `FeeService` can be updated (e.g. changing tax calculation) without touching UI code.
2. **Testability:** `FeeService` can be unit tested using JUnit independently of Swing components.
3. **Robustness:** Handles illegal inputs gracefully through explicit validation and exception handling.
4. **Decoupled Event Dispatch:** Independent `ActionListener` classes prevent single point of failure and eliminate fragile string matching.

---

## 10. Alternative Concepts & Big-O Complexity

### Alternative Listener Concepts:
- **Anonymous Classes or Lambdas:** Instead of named inner classes, one can bind `btnPay.addActionListener(e -> handlePay())`. While concise, named inner classes provide better modularity when action handlers are complex.
- **Action Interface (`javax.swing.Action`):** Allows sharing the same action across a button, a menu item, and a keyboard accelerator shortcut.

### Big-O Complexity Analysis:
- **Time Complexity:**
  - **Input Validation:** $\mathcal{O}(L)$ where $L$ is length of the input string. For typical inputs ($L < 100$), this runs in effective $\mathcal{O}(1)$ constant time.
  - **Fee Calculation:** $\mathcal{O}(1)$ constant time arithmetic operations.
  - **Event Dispatch:** $\mathcal{O}(1)$ direct method invocation on registered listener.
- **Space Complexity:**
  - $\mathcal{O}(1)$ auxiliary memory; simple primitive variables and a single receipt string buffer.

---

## 11. Compilation, Execution & Screenshot Instructions

### Compilation:
```bash
javac Task01/FeeWindow.java
```

### Execution:
```bash
java Task01.FeeWindow
```

### Screenshot Steps to Capture:
1. **Initial Screen:** Launch the application; capture the default form state.
2. **Invalid Input Error:** Enter letters in "Base Tuition Fee" and click "Pay Fee"; capture the error dialog.
3. **Successful Payment & Receipt:** Enter valid student information and click "Pay Fee"; capture the confirmation popup and generated receipt in the right-hand preview panel.
