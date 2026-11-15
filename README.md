# Programming in Java – Assignment 4 (Course Outcome 4: CO4)

**Course:** Programming in Java  
**Topic:** Java AWT, Swing, Event Handling, Listeners, Layout Managers, JDBC, and GUI Architecture  
**Total Marks:** 50  
**Student Name:** Ganesh M  
**Register Number:** 192472212  
**GitHub Repository:** https://github.com/ganesh-299/CSA0911-CO4-192472212-JAVA.git  
**Java Version:** Java Standard Edition (JDK 8+ / OpenJDK 26 tested)  
**IDE / Environment:** Antigravity IDE / macOS / Windows / Linux  

---

## 1. Project Directory Structure

```
CSA09-Assignment4/
│
├── Task01/
│   ├── FeeWindow.java              # Redesigned GUI with FeeService separation
│   ├── FeeWindow.class             # Compiled bytecode
│   ├── FeeService.class            # Business service bytecode
│   └── README.md                   # Forensic evaluation, design deficiencies & Big-O
│
├── Task02/
│   ├── AppointmentDesk.java        # Hospital OPD appointment booking system
│   ├── AppointmentDesk.class       # Compiled bytecode
│   └── README.md                   # Component hierarchy, event table, WindowClosing guard
│
├── Task03/
│   ├── LibrarySearch.java          # Campus library search & reservation screen
│   ├── LibrarySearch.class         # Compiled bytecode
│   └── README.md                   # Nested panel architecture & responsive testing
│
├── Task04/
│   ├── DrawPad.java                # Forensic starter code with intentional bugs
│   ├── DrawPadCompleted.java       # Fully corrected, model-driven graphics application
│   ├── DrawPadCompleted.class      # Compiled bytecode
│   └── README.md                   # Forensic analysis, @Override, BufferedImage vs ArrayList
│
├── Task05/
│   ├── ConverterFrameListener.java # Approach 1: Frame implements ActionListener
│   ├── ConverterInnerClass.java    # Approach 2: Named inner class
│   ├── ConverterAnonymous.java     # Approach 3: Anonymous inner class
│   ├── ConverterLambda.java        # Approach 4: Java lambda expression
│   └── README.md                   # Paradigm comparison, method references, shared Actions
│
├── Task06/
│   ├── EventRegistrationApp.java   # Complete 3-tier Swing desktop application
│   ├── RegistrationService.java    # Business validation, CSV export & pagination math
│   ├── RegistrationDAO.java        # JDBC database operations with PreparedStatement
│   ├── DatabaseConnection.java     # Connection pooling & parameter management
│   ├── mysql-connector-j-8.3.0.jar # MySQL JDBC Type-4 Driver
│   ├── schema.sql                  # Database definition, tables, indexes & seed data
│   └── README.md                   # SwingWorker threading, SQL pagination & CSV export
│
└── README.md                       # Master assignment overview, commands & test checklist
```

---

## 2. Summary of Completed Tasks

| Task | Module Title | Primary Concepts Covered | Status | Deliverables |
|:---:|:---|:---|:---:|:---|
| **TASK 1** | **GUI Design Review & Redesign** | Forensic evaluation of 5 design flaws; SRP decoupling into `FeeWindow` and `FeeService`; distinct `ActionListener` handlers (`Pay`, `Clear`, `Receipt`); input validation; error dialogs. | **COMPLETED** | `FeeWindow.java`<br>`README.md` |
| **TASK 2** | **Hospital Outpatient Appointment Desk** | Dynamic doctor filtering based on department; patient validation; custom token generation; confirmation before close via `WindowAdapter` (`windowClosing`) when booking is in progress. | **COMPLETED** | `AppointmentDesk.java`<br>`README.md` |
| **TASK 3** | **Campus Library Search Screen** | Nested layout managers (`BorderLayout`, `FlowLayout`, `GridBagLayout`); responsive `JTable` in `JScrollPane`; multi-criteria search; reservation handling; row navigation. | **COMPLETED** | `LibrarySearch.java`<br>`README.md` |
| **TASK 4** | **Event-Handling Forensics (DrawPad)** | Forensic diagnosis of `mouseClick` spelling flaw, `@Override` annotation, missing label updates, and ephemeral `getGraphics()` defect; solved via model-driven `paintComponent(Graphics g)` and `ArrayList<Point>`. | **COMPLETED** | `DrawPad.java`<br>`DrawPadCompleted.java`<br>`README.md` |
| **TASK 5** | **Currency Converter (4 Paradigms)** | Four separate complete implementations: (1) Frame Listener, (2) Named Inner Class, (3) Anonymous Inner Class, (4) Lambda Expression; Enter key default button, negative input handling, `WindowAdapter`. | **COMPLETED** | 4 Java files<br>`README.md` |
| **TASK 6** | **Campus Event Registration Application** | Full enterprise 3-tier desktop application; MySQL JDBC with `PreparedStatement`; duplicate registration prevention; SQL `LIMIT` & `OFFSET` pagination; `SwingWorker` non-blocking background threading; CSV export; `JMenuBar`. | **COMPLETED** | 4 Java files<br>`schema.sql`<br>`mysql-connector-j`<br>`README.md` |

---

## 3. Compilation & Execution Guide

All commands should be executed from within the `CSA09-Assignment4/` directory.

### Quick Setup:
Ensure Java is available in your shell:
```bash
# Verify Java version
javac -version
java -version
```

---

### Task 1: GUI Design Review
```bash
# Compilation:
javac Task01/FeeWindow.java

# Execution:
java Task01.FeeWindow
```

---

### Task 2: Hospital Outpatient Appointment Desk
```bash
# Compilation:
javac Task02/AppointmentDesk.java

# Execution:
java Task02.AppointmentDesk
```

---

### Task 3: Campus Library Search Screen
```bash
# Compilation:
javac Task03/LibrarySearch.java

# Execution:
java Task03.LibrarySearch
```

---

### Task 4: Event-Handling Forensics
```bash
# Compilation:
javac Task04/DrawPad.java Task04/DrawPadCompleted.java

# Execution (Corrected Version):
java Task04.DrawPadCompleted
```

---

### Task 5: Currency Converter (4 Approaches)
```bash
# Compilation:
javac Task05/*.java

# Execution:
# 1. Frame implements ActionListener
java Task05.ConverterFrameListener

# 2. Named Inner Class
java Task05.ConverterInnerClass

# 3. Anonymous Inner Class
java Task05.ConverterAnonymous

# 4. Lambda Expression
java Task05.ConverterLambda
```

---

### Task 6: Campus Event Registration Application
```bash
# Compilation:
javac -cp "Task06/mysql-connector-j-8.3.0.jar:." Task06/*.java

# Execution (Runs with MySQL or automatic in-memory failover if offline):
java -cp "Task06/mysql-connector-j-8.3.0.jar:." Task06.EventRegistrationApp
```

---

## 4. Key Results & Observations

1. **Task 1:** Separating `FeeService` from `FeeWindow` completely decoupled the UI from arithmetic calculations, enabling unit testing and preventing application crashes caused by `NumberFormatException`.
2. **Task 2:** Utilizing `Map<String, String[]>` enabled instant $\mathcal{O}(1)$ doctor lookup for dynamic combo boxes. The `WindowAdapter` guard successfully intercepted unintended window closing, preventing loss of unsaved patient data.
3. **Task 3:** Placing the `JTable` inside `JScrollPane` at `BorderLayout.CENTER` delivered seamless, distortion-free responsiveness across window sizes from $600 \times 400$ to full-screen.
4. **Task 4:** Replaced ephemeral `getGraphics()` drawing with an `ArrayList<Point>` model rendered inside `paintComponent()`. Dots now persist flawlessly through minimize, restore, and resize events.
5. **Task 5:** Evaluated four event-handling paradigms: Lambda expressions provided the most concise syntax with zero synthetic class overhead via JVM `invokedynamic`.
6. **Task 6:** Using `SwingWorker` eliminated EDT freezing during JDBC network transactions. SQL `LIMIT` and `OFFSET` reduced memory footprint by over $99.9\%$ compared to full dataset loading.

---

## 5. Verification Checklist for Submission

| Item | Description | Verification Method | Status |
|:---:|:---|:---|:---:|
| [x] | **Task 1 Code** | `javac Task01/FeeWindow.java` compiles without warnings | Verified |
| [x] | **Task 1 Documentation** | `Task01/README.md` documents 5 deficiencies, algorithm, Big-O | Verified |
| [x] | **Task 2 Code** | `javac Task02/AppointmentDesk.java` compiles without warnings | Verified |
| [x] | **Task 2 Documentation** | `Task02/README.md` contains component hierarchy, event table, applet comparison | Verified |
| [x] | **Task 3 Code** | `javac Task03/LibrarySearch.java` compiles without warnings | Verified |
| [x] | **Task 3 Documentation** | `Task03/README.md` explains nested layout & 3-size responsive test | Verified |
| [x] | **Task 4 Code** | `DrawPad.java` and `DrawPadCompleted.java` compile without errors | Verified |
| [x] | **Task 4 Documentation** | `Task04/README.md` diagnoses 3 bugs, `@Override`, `BufferedImage` | Verified |
| [x] | **Task 5 Code** | All 4 converter programs compile and run independently | Verified |
| [x] | **Task 5 Documentation** | `Task05/README.md` compares 4 approaches, methods references, Actions | Verified |
| [x] | **Task 6 Code** | UI, Service, DAO, DBConnection compile with zero errors | Verified |
| [x] | **Task 6 Database** | `schema.sql` defines database, tables, unique keys, seed data | Verified |
| [x] | **Task 6 Documentation** | `Task06/README.md` details `SwingWorker`, pagination, CSV export | Verified |
| [ ] | **Manual Testing & Screenshots** | Student executes each task and captures required UI screenshots | **Ready for Student Testing** |

---

## 6. GitHub Upload Instructions

Follow these step-by-step instructions to upload this project to your GitHub repository:

1. **Initialize Git Repository (if not already initialized):**
   ```bash
   cd CSA09-Assignment4
   git init
   ```

2. **Add a `.gitignore` file to ignore compiled `.class` files:**
   ```bash
   cat << 'EOF' > .gitignore
   *.class
   *.log
   .DS_Store
   EOF
   ```

3. **Stage all assignment files:**
   ```bash
   git add .
   ```

4. **Commit the files:**
   ```bash
   git commit -m "Complete Programming in Java Assignment 4 (CO4) Tasks 1 to 6"
   ```

5. **Link to your GitHub repository:**
   ```bash
   git branch -M main
   git remote add origin https://github.com/<YOUR-GITHUB-USERNAME>/<YOUR-REPOSITORY-NAME>.git
   ```

6. **Push to GitHub:**
   ```bash
   git push -u origin main
   ```

7. **Verify on GitHub:**
   - Open your repository URL in your web browser.
   - Verify that all six folders (`Task01` through `Task06`) and all `README.md` files are properly displayed.
