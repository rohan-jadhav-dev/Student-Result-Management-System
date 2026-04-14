import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class EditMarksForm extends JFrame {

    private JComboBox<String> studentCombo;
    private JTable coursesTable;
    private DefaultTableModel tableModel;
    private java.util.Map<String, String> studentMap = new java.util.LinkedHashMap<>();
    private java.util.List<Integer> courseIds = new java.util.ArrayList<>();

    private static final Font  BODY       = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font  BOLD       = new Font("Segoe UI", Font.BOLD, 13);
    private static final Color BG_DARK    = new Color(18, 22, 50);
    private static final Color BG_FIELD   = new Color(30, 38, 80);
    private static final Color BG_SEL     = new Color(60, 90, 180);
    private static final Color FG_WHITE   = Color.WHITE;
    private static final Color CLR_BORDER = new Color(60, 100, 180);

    public EditMarksForm() {
        setTitle("Edit Marks / Course Entry");
        setSize(1050, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
        loadStudents();
        setVisible(true);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  CORE dark combo — identical approach to AddMarksForm (BasicComboBoxUI)
    // ═══════════════════════════════════════════════════════════════════
    private void applyDarkCombo(JComboBox<?> c) {
        c.setFont(BODY);
        c.setForeground(FG_WHITE);
        c.setBackground(BG_FIELD);
        c.setOpaque(true);
        c.setBorder(BorderFactory.createLineBorder(CLR_BORDER, 1));

        c.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton() {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(BG_FIELD);
                        g2.fillRect(0, 0, getWidth(), getHeight());
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

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(BG_FIELD);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }

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

    private JComboBox<String> darkCombo(String[] items, String selected) {
        JComboBox<String> c = new JComboBox<>(items);
        applyDarkCombo(c);
        for (int i = 0; i < items.length; i++) {
            if (items[i].equalsIgnoreCase(selected)) { c.setSelectedIndex(i); break; }
        }
        return c;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  UI BUILD
    // ═══════════════════════════════════════════════════════════════════
    private void buildUI() {
        JPanel bg = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BG_DARK); g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        setContentPane(bg);

        // ---- Top bar ----
        JPanel top = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(22, 28, 65)); g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        top.setPreferredSize(new Dimension(0, 70));

        JLabel title = new JLabel("  Edit Marks / Course Entry");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(255, 180, 60));
        title.setBounds(10, 12, 400, 30);
        top.add(title);

        JLabel selLbl = new JLabel("Student:");
        selLbl.setFont(BODY);
        selLbl.setForeground(new Color(160, 190, 230));
        selLbl.setBounds(400, 18, 70, 26);
        top.add(selLbl);

        studentCombo = new JComboBox<>();
        applyDarkCombo(studentCombo);
        studentCombo.setBounds(475, 15, 420, 36);
        studentCombo.addActionListener(e -> loadCourses());
        top.add(studentCombo);

        bg.add(top, BorderLayout.NORTH);

        // ---- Table ----
        String[] cols = {"ID", "Sem", "Session", "Code", "Course Name", "Comp",
                "Credits", "Marks", "Total", "Grade", "GP", "Result", "Reg Type", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        coursesTable = new JTable(tableModel);
        styleTable();

        JScrollPane scroll = new JScrollPane(coursesTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 15));
        scroll.getViewport().setBackground(new Color(22, 28, 60));
        bg.add(scroll, BorderLayout.CENTER);

        // ---- Bottom buttons ----
        JPanel bottom = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BG_DARK); g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        bottom.setPreferredSize(new Dimension(0, 60));

        JButton editBtn = createBtn("\u270F  Edit Selected", new Color(220, 130, 30));
        editBtn.setBounds(15, 10, 200, 40);
        editBtn.addActionListener(e -> openEditDialog());
        bottom.add(editBtn);

        JButton delBtn = createBtn("\uD83D\uDDD1  Delete Selected", new Color(200, 50, 50));
        delBtn.setBounds(230, 10, 200, 40);
        delBtn.addActionListener(e -> deleteSelected());
        bottom.add(delBtn);

        JButton refreshBtn = createBtn("\u21BB  Refresh", new Color(50, 130, 255));
        refreshBtn.setBounds(445, 10, 150, 40);
        refreshBtn.addActionListener(e -> loadCourses());
        bottom.add(refreshBtn);

        JButton closeBtn = createBtn("\u2716  Close", new Color(100, 100, 130));
        closeBtn.setBounds(610, 10, 140, 40);
        closeBtn.addActionListener(e -> dispose());
        bottom.add(closeBtn);

        bg.add(bottom, BorderLayout.SOUTH);
    }

    private void styleTable() {
        coursesTable.setBackground(new Color(22, 28, 60));
        coursesTable.setForeground(new Color(200, 215, 240));
        coursesTable.setFont(BODY);
        coursesTable.setRowHeight(38);
        coursesTable.setShowGrid(false);
        coursesTable.setIntercellSpacing(new Dimension(0, 3));
        coursesTable.getTableHeader().setBackground(new Color(30, 40, 90));
        coursesTable.getTableHeader().setForeground(new Color(100, 180, 255));
        coursesTable.getTableHeader().setFont(BOLD);

        // Hide ID column (col 0)
        coursesTable.getColumnModel().getColumn(0).setMinWidth(0);
        coursesTable.getColumnModel().getColumn(0).setMaxWidth(0);
        coursesTable.getColumnModel().getColumn(0).setWidth(0);

        coursesTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (sel) c.setBackground(new Color(60, 100, 200));
                else     c.setBackground(row % 2 == 0 ? new Color(22, 28, 60) : new Color(28, 36, 75));
                c.setForeground(new Color(200, 215, 240));
                ((JLabel)c).setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });

        coursesTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openEditDialog();
            }
        });
    }

    private JButton createBtn(String text, Color color) {
        JButton btn = new JButton(text) {
            boolean h = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { h = true; repaint(); }
                public void mouseExited(MouseEvent e)  { h = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(h ? color.brighter() : color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
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
            JOptionPane.showMessageDialog(this, "Load error: " + ex.getMessage());
        }
        // Manually trigger first load since ActionListener may not fire if only 1 item
        if (studentCombo.getItemCount() > 0) loadCourses();
    }

    private void loadCourses() {
        tableModel.setRowCount(0);
        courseIds.clear();
        String sel = (String) studentCombo.getSelectedItem();
        if (sel == null || sel.isEmpty()) return;
        String regNo = studentMap.get(sel);
        if (regNo == null) return;

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT c.id, s.sem_number, s.session, c.course_code, c.course_name, " +
                            "c.component, c.credits, c.marks_obtained, c.marks_total, " +
                            "c.grade, c.grade_point, c.result, c.register_type, c.status " +
                            "FROM courses c " +
                            "JOIN semesters s ON c.semester_id = s.id " +
                            "JOIN students st ON s.student_id = st.id " +
                            "WHERE st.registration_no=? " +
                            "ORDER BY s.sem_number, c.course_code, c.component"
            );
            ps.setString(1, regNo);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                courseIds.add(rs.getInt("id"));
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        "Sem " + rs.getInt("sem_number"),
                        rs.getString("session"),
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getString("component"),
                        rs.getInt("credits"),
                        rs.getInt("marks_obtained"),
                        rs.getInt("marks_total"),
                        rs.getString("grade"),
                        rs.getInt("grade_point"),
                        rs.getString("result"),
                        rs.getString("register_type"),
                        rs.getString("status")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + ex.getMessage());
        }
    }

    private void openEditDialog() {
        int row = coursesTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a course row to edit."); return; }
        int courseId = (int) tableModel.getValueAt(row, 0);
        showEditDialog(courseId, row);
    }

    private void showEditDialog(int courseId, int row) {
        JDialog dlg = new JDialog(this, "Edit Course", true);
        dlg.setSize(520, 580);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel bg = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BG_DARK); g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        dlg.setContentPane(bg);

        JLabel hdr = new JLabel("Edit Course Details");
        hdr.setFont(new Font("Segoe UI", Font.BOLD, 16));
        hdr.setForeground(new Color(255, 180, 60));
        hdr.setBounds(20, 12, 400, 28);
        bg.add(hdr);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(60, 80, 140));
        sep.setBounds(20, 45, 470, 1);
        bg.add(sep);

        // Read current values
        String curCode    = String.valueOf(tableModel.getValueAt(row, 3));
        String curName    = String.valueOf(tableModel.getValueAt(row, 4));
        String curComp    = String.valueOf(tableModel.getValueAt(row, 5));
        String curCredits = String.valueOf(tableModel.getValueAt(row, 6));
        String curObt     = String.valueOf(tableModel.getValueAt(row, 7));
        String curTotal   = String.valueOf(tableModel.getValueAt(row, 8));
        String curRegType = String.valueOf(tableModel.getValueAt(row, 12));
        String curStatus  = String.valueOf(tableModel.getValueAt(row, 13));

        String[]     labels = {"Course Code","Course Name","Component","Credits",
                "Marks Obtained","Marks Total","Register Type","Status"};
        JComponent[] inputs = new JComponent[8];

        inputs[0] = darkField(curCode);
        inputs[1] = darkField(curName);
        inputs[2] = darkCombo(new String[]{"TH","PR","TW","OR"}, curComp);
        inputs[3] = darkField(curCredits);
        inputs[4] = darkField(curObt);
        inputs[5] = darkField(curTotal);
        inputs[6] = darkCombo(new String[]{"Regular","Ex-Student","ATKT"}, curRegType);
        inputs[7] = darkCombo(new String[]{"Registered","Not Registered","Detained"}, curStatus);

        for (int i = 0; i < labels.length; i++) {
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lbl.setForeground(new Color(160, 190, 230));
            lbl.setBounds(25, 60 + i * 56, 200, 18);
            bg.add(lbl);
            inputs[i].setBounds(25, 78 + i * 56, 460, 34);
            bg.add(inputs[i]);
        }

        JButton saveBtn = createBtn("\uD83D\uDCBE  Save Changes", new Color(40, 180, 120));
        saveBtn.setBounds(25, 520, 210, 40);
        saveBtn.addActionListener(e -> {
            try {
                String code    = ((JTextField) inputs[0]).getText().trim().toUpperCase();
                String name    = ((JTextField) inputs[1]).getText().trim();
                String comp    = (String)((JComboBox<?>) inputs[2]).getSelectedItem();
                int credits    = Integer.parseInt(((JTextField) inputs[3]).getText().trim());
                int obtained   = Integer.parseInt(((JTextField) inputs[4]).getText().trim());
                int total      = Integer.parseInt(((JTextField) inputs[5]).getText().trim());
                String regType = (String)((JComboBox<?>) inputs[6]).getSelectedItem();
                String status  = (String)((JComboBox<?>) inputs[7]).getSelectedItem();

                if (obtained > total) {
                    JOptionPane.showMessageDialog(dlg, "Marks obtained cannot exceed total!"); return;
                }

                double pct    = GradeCalculator.calcPercentage(obtained, total);
                String grade  = GradeCalculator.getGrade(pct, false);
                int gp        = GradeCalculator.getGradePoint(grade);
                String result = GradeCalculator.getResult(grade);

                try (Connection con = DBConnection.getConnection()) {
                    PreparedStatement ps = con.prepareStatement(
                            "UPDATE courses SET course_code=?, course_name=?, component=?, credits=?, " +
                                    "marks_obtained=?, marks_total=?, percentage=?, grade=?, grade_point=?, " +
                                    "result=?, register_type=?, status=? WHERE id=?");
                    ps.setString(1, code);  ps.setString(2, name);   ps.setString(3, comp);
                    ps.setInt(4, credits);  ps.setInt(5, obtained);  ps.setInt(6, total);
                    ps.setDouble(7, pct);   ps.setString(8, grade);  ps.setInt(9, gp);
                    ps.setString(10, result); ps.setString(11, regType); ps.setString(12, status);
                    ps.setInt(13, courseId);
                    ps.executeUpdate();

                    ResultSet rs2 = con.createStatement().executeQuery(
                            "SELECT semester_id FROM courses WHERE id=" + courseId);
                    if (rs2.next()) {
                        int semId = rs2.getInt("semester_id");
                        recalcSGPA(con, semId);
                        ResultSet rs3 = con.createStatement().executeQuery(
                                "SELECT student_id FROM semesters WHERE id=" + semId);
                        if (rs3.next()) recalcCGPA(con, rs3.getInt("student_id"));
                    }
                }

                JOptionPane.showMessageDialog(dlg,
                        "Updated!  Grade: " + grade + "  GP: " + gp + "  Result: " + result);
                dlg.dispose();
                loadCourses();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Enter valid numbers for credits and marks.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Error: " + ex.getMessage());
            }
        });
        bg.add(saveBtn);

        JButton cancelBtn = createBtn("\u2716  Cancel", new Color(180, 60, 60));
        cancelBtn.setBounds(255, 520, 210, 40);
        cancelBtn.addActionListener(e -> dlg.dispose());
        bg.add(cancelBtn);

        dlg.setVisible(true);
    }

    private JTextField darkField(String value) {
        JTextField f = new JTextField(value);
        f.setFont(BODY);
        f.setForeground(FG_WHITE);
        f.setBackground(BG_FIELD);
        f.setCaretColor(FG_WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CLR_BORDER, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return f;
    }

    private void deleteSelected() {
        int row = coursesTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a course row to delete."); return; }
        int courseId = (int) tableModel.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this course entry? SGPA/CGPA will be recalculated.",
                "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = DBConnection.getConnection()) {
            // Fetch semId and studentId BEFORE deleting
            int semId = -1, studentId = -1;
            ResultSet rs2 = con.createStatement().executeQuery(
                    "SELECT semester_id FROM courses WHERE id=" + courseId);
            if (rs2.next()) {
                semId = rs2.getInt("semester_id");
                ResultSet rs3 = con.createStatement().executeQuery(
                        "SELECT student_id FROM semesters WHERE id=" + semId);
                if (rs3.next()) studentId = rs3.getInt("student_id");
            }

            // Now delete
            PreparedStatement del = con.prepareStatement("DELETE FROM courses WHERE id=?");
            del.setInt(1, courseId);
            del.executeUpdate();

            if (semId != -1)    recalcSGPA(con, semId);
            if (studentId != -1) recalcCGPA(con, studentId);

            loadCourses();
            JOptionPane.showMessageDialog(this, "Course deleted and grades recalculated.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void recalcSGPA(Connection con, int semId) throws SQLException {
        ResultSet rs = con.createStatement().executeQuery(
                "SELECT credits, grade_point FROM courses WHERE semester_id=" + semId);
        int tc = 0, ws = 0;
        while (rs.next()) {
            int cr = rs.getInt("credits");
            if (cr > 0) { tc += cr; ws += cr * rs.getInt("grade_point"); }
        }
        double sgpa = tc > 0 ? Math.round((double) ws / tc * 100.0) / 100.0 : 0;
        PreparedStatement ps = con.prepareStatement("UPDATE semesters SET sgpa=? WHERE id=?");
        ps.setDouble(1, sgpa); ps.setInt(2, semId); ps.executeUpdate();
    }

    private void recalcCGPA(Connection con, int studentId) throws SQLException {
        ResultSet rs = con.createStatement().executeQuery(
                "SELECT sgpa FROM semesters WHERE student_id=" + studentId);
        double total = 0; int count = 0;
        while (rs.next()) {
            double s = rs.getDouble("sgpa");
            if (s > 0) { total += s; count++; }
        }
        double cgpa = count > 0 ? Math.round(total / count * 100.0) / 100.0 : 0;
        PreparedStatement ps = con.prepareStatement("UPDATE students SET cgpa=? WHERE id=?");
        ps.setDouble(1, cgpa); ps.setInt(2, studentId); ps.executeUpdate();
    }
}