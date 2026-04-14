import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AddMarksForm extends JFrame {

    private JComboBox<String>  studentCombo;
    private JComboBox<Integer> semCombo;
    private JComboBox<String>  sessionCombo, courseCodeCombo, courseNameCombo,
            componentCombo, regTypeCombo, statusCombo;
    private JTextField creditsField, marksField, totalField;
    private JCheckBox  auditCheck;

    private java.util.Map<String, String> studentMap = new java.util.LinkedHashMap<>();

    private static final Font  BODY      = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font  LBL       = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Color BG_DARK   = new Color(18, 22, 50);
    private static final Color BG_FIELD  = new Color(30, 38, 80);
    private static final Color BG_SEL    = new Color(60, 90, 180);
    private static final Color FG_WHITE  = Color.WHITE;
    private static final Color CLR_BORDER = new Color(60, 100, 180);

    private static final String[] COURSE_CODES = {
            "BT101","BT102","BT103","BT201","BT202","BT203",
            "CS301","CS302","CS303","CS401","CS402","CS403",
            "MA101","MA201","PH101","CH101","ME201","EC301"
    };
    private static final String[] COURSE_NAMES = {
            "Engineering Mathematics I","Engineering Mathematics II",
            "Engineering Physics","Engineering Chemistry",
            "Data Structures","Algorithms","Operating Systems",
            "Database Management","Computer Networks","Software Engineering",
            "Object Oriented Programming","Web Technology",
            "Artificial Intelligence","Machine Learning",
            "Digital Electronics","Microprocessors",
            "Compiler Design","Theory of Computation"
    };

    public AddMarksForm() {
        setTitle("Add Marks / Course Entry");
        setSize(580, 660);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        buildUI();
        loadStudents();
        setVisible(true);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  CORE: Apply fully-dark styling using BasicComboBoxUI override
    //  This fixes the Windows L&F issue where closed combo stays white
    // ═══════════════════════════════════════════════════════════════════
    private void applyDarkCombo(JComboBox<?> c) {
        c.setFont(BODY);
        c.setForeground(FG_WHITE);
        c.setBackground(BG_FIELD);
        c.setOpaque(true);
        c.setBorder(BorderFactory.createLineBorder(CLR_BORDER, 1));

        c.setUI(new BasicComboBoxUI() {

            // Arrow button — dark
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton() {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(BG_FIELD);
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        // Draw arrow
                        int w = getWidth(), h = getHeight();
                        int[] xp = {w/2 - 4, w/2 + 4, w/2};
                        int[] yp = {h/2 - 2, h/2 - 2, h/2 + 3};
                        g2.setColor(new Color(120, 160, 255));
                        g2.fillPolygon(xp, yp, 3);
                    }
                };
                btn.setBorder(BorderFactory.createEmptyBorder());
                btn.setContentAreaFilled(false);
                btn.setFocusPainted(false);
                return btn;
            }

            // Selected-value area — dark background, white text
            @Override
            public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(BG_FIELD);
                g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

                Object sel = comboBox.getSelectedItem();
                if (sel != null) {
                    g2.setColor(FG_WHITE);
                    g2.setFont(BODY);
                    FontMetrics fm = g2.getFontMetrics();
                    int textY = bounds.y + (bounds.height + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(sel.toString(), bounds.x + 8, textY);
                }
            }

            // Background of the whole combo — dark
            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(BG_FIELD);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }

            // Popup list — dark
            @Override
            protected ComboPopup createPopup() {
                BasicComboPopup popup = new BasicComboPopup(comboBox) {
                    @Override protected void configureList() {
                        super.configureList();
                        list.setBackground(BG_FIELD);
                        list.setForeground(FG_WHITE);
                        list.setSelectionBackground(BG_SEL);
                        list.setSelectionForeground(FG_WHITE);
                        list.setFont(BODY);
                    }
                };
                popup.setBorder(BorderFactory.createLineBorder(CLR_BORDER, 1));
                return popup;
            }
        });

        // Renderer for items inside the open popup
        c.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? BG_SEL : BG_FIELD);
                setForeground(FG_WHITE);
                setFont(BODY);
                setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                setText(value == null ? "" : value.toString());
                return this;
            }
        });

        // For editable combos, style the embedded text field
        if (c.isEditable()) {
            Component ed = c.getEditor().getEditorComponent();
            if (ed instanceof JTextField tf) {
                tf.setBackground(BG_FIELD);
                tf.setForeground(FG_WHITE);
                tf.setCaretColor(FG_WHITE);
                tf.setFont(BODY);
                tf.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
            }
        }
    }

    private <T> JComboBox<T> darkCombo(T[] items) {
        JComboBox<T> c = new JComboBox<>(items);
        applyDarkCombo(c);
        return c;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  UI BUILD
    // ═══════════════════════════════════════════════════════════════════
    private void buildUI() {
        JPanel bg = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BG_DARK);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        setContentPane(bg);

        JLabel title = new JLabel("  Add Marks / Course Entry");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(100, 180, 255));
        title.setBounds(20, 12, 500, 30);
        bg.add(title);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(60, 80, 140));
        sep.setBounds(20, 48, 530, 1);
        bg.add(sep);

        int y = 58;

        // Student
        addLabel(bg, "Student", 20, y);
        studentCombo = new JComboBox<>();
        applyDarkCombo(studentCombo);
        studentCombo.setBounds(20, y + 20, 530, 36);
        bg.add(studentCombo);
        y += 66;

        // Semester
        addLabel(bg, "Semester", 20, y);
        semCombo = darkCombo(new Integer[]{1,2,3,4,5,6,7,8});
        semCombo.setBounds(20, y + 20, 250, 36);
        bg.add(semCombo);

        // Session
        addLabel(bg, "Session", 290, y);
        sessionCombo = darkCombo(new String[]{
                "Summer 2024","Winter 2024",
                "Summer 2025","Winter 2025",
                "Summer 2026","Winter 2026"
        });
        sessionCombo.setSelectedItem("Winter 2025");
        sessionCombo.setBounds(290, y + 20, 260, 36);
        bg.add(sessionCombo);
        y += 66;

        // Course Code (editable)
        addLabel(bg, "Course Code", 20, y);
        courseCodeCombo = new JComboBox<>(COURSE_CODES);
        courseCodeCombo.setEditable(true);
        applyDarkCombo(courseCodeCombo);
        courseCodeCombo.setBounds(20, y + 20, 250, 36);
        bg.add(courseCodeCombo);

        // Course Name (editable)
        addLabel(bg, "Course Name", 290, y);
        courseNameCombo = new JComboBox<>(COURSE_NAMES);
        courseNameCombo.setEditable(true);
        applyDarkCombo(courseNameCombo);
        courseNameCombo.setBounds(290, y + 20, 260, 36);
        bg.add(courseNameCombo);
        y += 66;

        // Component
        addLabel(bg, "Component", 20, y);
        componentCombo = darkCombo(new String[]{"TH","PR","TW","OR"});
        componentCombo.setBounds(20, y + 20, 120, 36);
        bg.add(componentCombo);

        // Credits
        addLabel(bg, "Credits", 160, y);
        creditsField = styledField("4");
        creditsField.setBounds(160, y + 20, 100, 36);
        bg.add(creditsField);

        // Audit
        auditCheck = new JCheckBox("Audit Course");
        auditCheck.setFont(LBL);
        auditCheck.setForeground(new Color(160, 190, 230));
        auditCheck.setOpaque(false);
        auditCheck.setBounds(280, y + 25, 130, 26);
        bg.add(auditCheck);
        y += 66;

        // Marks Obtained
        addLabel(bg, "Marks Obtained", 20, y);
        marksField = styledField("75");
        marksField.setBounds(20, y + 20, 250, 36);
        bg.add(marksField);

        // Marks Total
        addLabel(bg, "Marks Total", 290, y);
        totalField = styledField("100");
        totalField.setBounds(290, y + 20, 260, 36);
        bg.add(totalField);
        y += 66;

        // Register Type
        addLabel(bg, "Register Type", 20, y);
        regTypeCombo = darkCombo(new String[]{"Regular","Ex-Student","ATKT"});
        regTypeCombo.setBounds(20, y + 20, 250, 36);
        bg.add(regTypeCombo);

        // Status
        addLabel(bg, "Status", 290, y);
        statusCombo = darkCombo(new String[]{"Registered","Not Registered","Detained"});
        statusCombo.setBounds(290, y + 20, 260, 36);
        bg.add(statusCombo);
        y += 70;

        // Buttons
        JButton saveBtn = createBtn("\uD83D\uDCBE  Save", new Color(40, 180, 120));
        saveBtn.setBounds(20, y, 160, 44);
        saveBtn.addActionListener(e -> saveMarks());
        bg.add(saveBtn);

        JButton refreshBtn = createBtn("\u21BB  Refresh", new Color(50, 130, 255));
        refreshBtn.setBounds(195, y, 160, 44);
        refreshBtn.addActionListener(e -> { loadStudents(); clearForm(); });
        bg.add(refreshBtn);

        JButton cancelBtn = createBtn("\u2716  Cancel", new Color(180, 60, 60));
        cancelBtn.setBounds(370, y, 180, 44);
        cancelBtn.addActionListener(e -> dispose());
        bg.add(cancelBtn);
    }

    private void clearForm() {
        semCombo.setSelectedIndex(0);
        sessionCombo.setSelectedItem("Winter 2025");
        courseCodeCombo.setSelectedIndex(0);
        courseNameCombo.setSelectedIndex(0);
        componentCombo.setSelectedIndex(0);
        creditsField.setText("4");
        marksField.setText("75");
        totalField.setText("100");
        auditCheck.setSelected(false);
        regTypeCombo.setSelectedIndex(0);
        statusCombo.setSelectedIndex(0);
    }

    private void addLabel(JPanel bg, String text, int x, int y) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(LBL);
        lbl.setForeground(new Color(160, 190, 230));
        lbl.setBounds(x, y, 260, 18);
        bg.add(lbl);
    }

    private JTextField styledField(String def) {
        JTextField f = new JTextField(def);
        f.setFont(BODY);
        f.setForeground(FG_WHITE);
        f.setBackground(BG_FIELD);
        f.setCaretColor(FG_WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CLR_BORDER, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return f;
    }

    private JButton createBtn(String text, Color color) {
        JButton btn = new JButton(text) {
            boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? color.brighter() : color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        btn.setForeground(FG_WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  DATA LOGIC
    // ═══════════════════════════════════════════════════════════════════
    private void loadStudents() {
        studentCombo.removeAllItems();
        studentMap.clear();
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery(
                    "SELECT name, registration_no FROM students ORDER BY name");
            while (rs.next()) {
                String display = rs.getString("name") + "  [" + rs.getString("registration_no") + "]";
                studentMap.put(display, rs.getString("registration_no"));
                studentCombo.addItem(display);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not load students: " + ex.getMessage());
        }
    }

    // ONLY showing UPDATED saveMarks() (replace this method)

    private void saveMarks() {
        try (Connection con = DBConnection.getConnection()) {

            String student = (String) studentCombo.getSelectedItem();
            String regNo = studentMap.get(student);

            int sem = (int) semCombo.getSelectedItem();
            String session = (String) sessionCombo.getSelectedItem();

            String code = courseCodeCombo.getSelectedItem().toString();
            String name = courseNameCombo.getSelectedItem().toString();
            String comp = componentCombo.getSelectedItem().toString();

            int credits = Integer.parseInt(creditsField.getText());
            int marks = Integer.parseInt(marksField.getText());
            int total = Integer.parseInt(totalField.getText());

            double percentage = (marks * 100.0) / total;
            String grade = GradeCalculator.getGrade(percentage, false);
            int gp = GradeCalculator.getGradePoint(grade);
            String result = GradeCalculator.getResult(grade);

            String regType = (String) regTypeCombo.getSelectedItem();
            String status = (String) statusCombo.getSelectedItem();

            // Student ID
            PreparedStatement ps1 = con.prepareStatement(
                    "SELECT id FROM students WHERE registration_no=?");
            ps1.setString(1, regNo);
            ResultSet rs1 = ps1.executeQuery();
            rs1.next();
            int studentId = rs1.getInt("id");

            // Semester
            PreparedStatement checkSem = con.prepareStatement(
                    "SELECT id FROM semesters WHERE student_id=? AND sem_number=?");
            checkSem.setInt(1, studentId);
            checkSem.setInt(2, sem);
            ResultSet rsSem = checkSem.executeQuery();

            int semId;
            if (rsSem.next()) {
                semId = rsSem.getInt("id");
            } else {
                PreparedStatement insertSem = con.prepareStatement(
                        "INSERT INTO semesters (student_id, sem_number, session, sgpa) VALUES (?,?,?,0)",
                        Statement.RETURN_GENERATED_KEYS);
                insertSem.setInt(1, studentId);
                insertSem.setInt(2, sem);
                insertSem.setString(3, session);
                insertSem.executeUpdate();

                ResultSet gen = insertSem.getGeneratedKeys();
                gen.next();
                semId = gen.getInt(1);
            }

            // Prevent duplicate
            PreparedStatement checkCourse = con.prepareStatement(
                    "SELECT id FROM courses WHERE semester_id=? AND course_code=? AND component=?");
            checkCourse.setInt(1, semId);
            checkCourse.setString(2, code);
            checkCourse.setString(3, comp);

            if (checkCourse.executeQuery().next()) {
                JOptionPane.showMessageDialog(this, "⚠ Course already exists!");
                return;
            }

            // Insert
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO courses (semester_id, course_code, course_name, component, credits, " +
                            "marks_obtained, marks_total, percentage, grade, grade_point, result, register_type, status) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");

            ps.setInt(1, semId);
            ps.setString(2, code);
            ps.setString(3, name);
            ps.setString(4, comp);
            ps.setInt(5, credits);
            ps.setInt(6, marks);
            ps.setInt(7, total);
            ps.setDouble(8, percentage);
            ps.setString(9, grade);
            ps.setInt(10, gp);
            ps.setString(11, result);
            ps.setString(12, regType);
            ps.setString(13, status);

            ps.executeUpdate();

            // Update SGPA + CGPA
            recalcSGPA(con, semId);
            recalcCGPA(con, studentId);

            JOptionPane.showMessageDialog(this, "✅ Marks Saved!");
            clearForm();


        }catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter valid numbers for Credits and Marks.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void recalcSGPA(Connection con, int semId) throws SQLException {
        ResultSet rs = con.createStatement().executeQuery(
                "SELECT credits, grade_point FROM courses WHERE semester_id=" + semId);
        int totalCredits = 0, weightedSum = 0;
        while (rs.next()) {
            int cr = rs.getInt("credits"); int gp = rs.getInt("grade_point");
            if (cr > 0) { totalCredits += cr; weightedSum += cr * gp; }
        }
        double sgpa = totalCredits > 0 ? Math.round((double) weightedSum / totalCredits * 100.0) / 100.0 : 0;
        PreparedStatement ps = con.prepareStatement("UPDATE semesters SET sgpa=? WHERE id=?");
        ps.setDouble(1, sgpa); ps.setInt(2, semId); ps.executeUpdate();
    }

    private void recalcCGPA(Connection con, int studentId) throws SQLException {
        ResultSet rs = con.createStatement().executeQuery(
                "SELECT sgpa FROM semesters WHERE student_id=" + studentId);
        double total = 0; int count = 0;
        while (rs.next()) { double s = rs.getDouble("sgpa"); if (s > 0) { total += s; count++; } }
        double cgpa = count > 0 ? Math.round(total / count * 100.0) / 100.0 : 0;
        PreparedStatement ps = con.prepareStatement("UPDATE students SET cgpa=? WHERE id=?");
        ps.setDouble(1, cgpa); ps.setInt(2, studentId); ps.executeUpdate();
    }
}