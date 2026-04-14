import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Dashboard extends JFrame {

    private JPanel contentArea;
    JTable studentTable;
    DefaultTableModel tableModel;
    private JLabel totalLabel, passLabel, failLabel;

    private static final Font BODY = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font BOLD = new Font("Segoe UI", Font.BOLD, 13);

    public Dashboard() {
        setTitle("Student Result System — Dashboard");
        setSize(1180, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        buildSidebar();
        buildMainArea();
        loadStudents();
        setVisible(true);
    }

    // ========================= SIDEBAR =========================
    private void buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(13, 17, 45),
                        0, getHeight(), new Color(20, 30, 70));
                g2.setPaint(gp); g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        sidebar.setLayout(null);
        sidebar.setPreferredSize(new Dimension(230, 720));

        // GH Raisoni painted logo
        JPanel logoPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                g2.setColor(new Color(80, 25, 130));
                g2.fillOval(6, 4, 44, 44);
                g2.setColor(new Color(255, 140, 0));
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(6, 4, 44, 44);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
                g2.setColor(Color.WHITE);
                g2.drawString("\uD83E\uDD81", 10, 36);

                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                g2.setColor(new Color(130, 60, 200));
                g2.drawString("GH", 58, 22);

                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                g2.setColor(new Color(100, 40, 170));
                g2.drawString("raisoni", 82, 22);

                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g2.setColor(new Color(110, 50, 180));
                g2.drawString("COLLEGE", 58, 38);

                g2.setColor(new Color(255, 130, 0));
                g2.setStroke(new BasicStroke(2.2f));
                g2.drawLine(58, 42, 214, 42);

                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g2.setColor(new Color(170, 170, 200));
                g2.drawString("Engineering and Management", 58, 56);

                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2.setColor(new Color(255, 140, 0));
                g2.drawString("Pune", 58, 70);
            }
        };
        logoPanel.setOpaque(false);
        logoPanel.setBounds(8, 8, 214, 80);
        sidebar.add(logoPanel);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(100, 60, 160));
        sep.setBounds(14, 94, 202, 1);
        sidebar.add(sep);

        String[] navLabels = {
                "Dashboard", "Add Student", "Add Marks",
                "Edit Marks", "View Results", "Search Student", "Logout"
        };
        String[] navEmoji = {
                "\uD83C\uDFE0", "\u2795", "\uD83D\uDCDD",
                "\u270F",       "\uD83D\uDCCA", "\uD83D\uDD0D", "\uD83D\uDEAA"
        };

        for (int i = 0; i < navLabels.length; i++) {
            JButton btn = createNavButton(navEmoji[i] + "  " + navLabels[i]);
            btn.setBounds(12, 104 + i * 52, 206, 44);
            int idx = i;
            btn.addActionListener(e -> handleNav(idx));
            sidebar.add(btn);
        }

        add(sidebar, BorderLayout.WEST);
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text) {
            boolean hovered = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (hovered) {
                    g2.setColor(new Color(100, 149, 237, 55));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(new Color(100, 149, 237, 140));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                }
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        btn.setForeground(new Color(200, 215, 240));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void handleNav(int idx) {
        switch (idx) {
            case 0 -> loadStudents();
            case 1 -> new AddStudentForm(this, -1);
            case 2 -> new AddMarksForm();
            case 3 -> new EditMarksForm();
            case 4 -> new ViewResult();
            case 5 -> searchStudent();
            case 6 -> { dispose(); new LoginScreen(); }
        }
    }

    // ========================= MAIN AREA =========================
    private void buildMainArea() {
        contentArea = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(18, 22, 50)); g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        JPanel header = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(22, 28, 60)); g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setPreferredSize(new Dimension(0, 70));

        JLabel title = new JLabel("  All Students \u2014 Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(100, 180, 255));
        title.setBounds(10, 17, 420, 36);
        header.add(title);

        JButton addBtn = createActionButton("+ Add Student", new Color(50, 150, 255));
        addBtn.setBounds(680, 15, 155, 38);
        addBtn.addActionListener(e -> new AddStudentForm(this, -1));
        header.add(addBtn);

        JButton refreshBtn = createActionButton("\u21BB  Refresh", new Color(40, 180, 120));
        refreshBtn.setBounds(850, 15, 115, 38);
        refreshBtn.addActionListener(e -> loadStudents());
        header.add(refreshBtn);

        contentArea.add(header, BorderLayout.NORTH);

        // Stats cards
        JPanel statsPanel = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(18, 22, 50)); g.fillRect(0,0,getWidth(),getHeight());
            }
        };
        statsPanel.setPreferredSize(new Dimension(0, 105));

        totalLabel = createStatCard("Total Students", "0", new Color(50, 130, 255));
        totalLabel.setBounds(20, 15, 210, 74);
        statsPanel.add(totalLabel);

        passLabel = createStatCard("Pass", "0", new Color(40, 200, 140));
        passLabel.setBounds(248, 15, 210, 74);
        statsPanel.add(passLabel);

        failLabel = createStatCard("Fail", "0", new Color(255, 80, 80));
        failLabel.setBounds(476, 15, 210, 74);
        statsPanel.add(failLabel);

        // Table
        String[] cols = {"#", "Name", "Reg No", "Roll No", "Batch", "Stream", "Section", "CGPA", "Actions"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 8; }
        };
        studentTable = new JTable(tableModel);
        styleTable();

        JScrollPane scroll = new JScrollPane(studentTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(new Color(22, 28, 60));

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(new Color(18, 22, 50));
        tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        tablePanel.add(scroll);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(new Color(18, 22, 50));
        center.add(statsPanel, BorderLayout.NORTH);
        center.add(tablePanel, BorderLayout.CENTER);

        contentArea.add(center, BorderLayout.CENTER);
        add(contentArea, BorderLayout.CENTER);
    }

    private JButton createActionButton(String text, Color color) {
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
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel createStatCard(String label, String value, Color accent) {
        JLabel card = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 38, 80));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 6, getHeight(), 6, 6);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.setColor(new Color(150, 180, 220));
                g2.drawString(label, 18, 24);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 30));
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), 18, 58);
            }
        };
        card.setText(value);
        return card;
    }

    private void styleTable() {
        studentTable.setBackground(new Color(22, 28, 60));
        studentTable.setForeground(new Color(200, 215, 240));
        studentTable.setFont(BODY);
        studentTable.setRowHeight(44);
        studentTable.setShowGrid(false);
        studentTable.setIntercellSpacing(new Dimension(0, 3));
        studentTable.getTableHeader().setBackground(new Color(30, 40, 90));
        studentTable.getTableHeader().setForeground(new Color(100, 180, 255));
        studentTable.getTableHeader().setFont(BOLD);
        studentTable.getTableHeader().setBorder(BorderFactory.createEmptyBorder());

        studentTable.addMouseMotionListener(new MouseMotionAdapter() {
            int lastRow = -1;
            @Override public void mouseMoved(MouseEvent e) {
                int row = studentTable.rowAtPoint(e.getPoint());
                if (row != lastRow) { lastRow = row; studentTable.repaint(); }
            }
        });
        studentTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { studentTable.repaint(); }
        });

        studentTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                Point mp = studentTable.getMousePosition();
                boolean hov = mp != null && studentTable.rowAtPoint(mp) == row;
                if (sel)      c.setBackground(new Color(55, 105, 210));
                else if (hov) c.setBackground(new Color(38, 60, 140));
                else          c.setBackground(row % 2 == 0 ? new Color(22, 28, 60) : new Color(28, 36, 75));
                c.setForeground(new Color(200, 215, 240));
                ((JLabel)c).setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        studentTable.getColumn("Actions").setCellRenderer(new ActionButtonRenderer());
        studentTable.getColumn("Actions").setCellEditor(new ActionButtonEditor(new JCheckBox(), this));
        studentTable.getColumnModel().getColumn(0).setMaxWidth(40);
        studentTable.getColumnModel().getColumn(8).setMinWidth(215);
        studentTable.getColumnModel().getColumn(8).setMaxWidth(215);
    }

    // ========================= CRUD =========================
    public void loadStudents() {
        tableModel.setRowCount(0);
        int total = 0, pass = 0, fail = 0;
        try (Connection con = DBConnection.getConnection()) {
            // Check for any FAIL result in courses for each student
            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM students ORDER BY name");
            int i = 1;
            while (rs.next()) {
                double cgpa   = rs.getDouble("cgpa");
                int    stuId  = rs.getInt("id");

                tableModel.addRow(new Object[]{
                        i++,
                        rs.getString("name"),
                        rs.getString("registration_no"),
                        rs.getString("roll_no"),
                        rs.getString("batch"),
                        rs.getString("stream"),
                        rs.getString("section"),
                        cgpa > 0 ? String.format("%.2f", cgpa) : "--",
                        "actions"
                });
                total++;

                // Pass/Fail: student has marks AND no FAIL result in any course
                PreparedStatement hasMarks = con.prepareStatement(
                        "SELECT COUNT(*) FROM courses c JOIN semesters s ON c.semester_id=s.id " +
                                "WHERE s.student_id=?");
                hasMarks.setInt(1, stuId);
                ResultSet hmRs = hasMarks.executeQuery();
                hmRs.next();
                int courseCount = hmRs.getInt(1);

                if (courseCount > 0) {
                    PreparedStatement failCheck = con.prepareStatement(
                            "SELECT COUNT(*) FROM courses c JOIN semesters s ON c.semester_id=s.id " +
                                    "WHERE s.student_id=? AND c.result='FAIL'");
                    failCheck.setInt(1, stuId);
                    ResultSet fcRs = failCheck.executeQuery();
                    fcRs.next();
                    int failCount = fcRs.getInt(1);
                    if (failCount > 0) fail++;
                    else               pass++;
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage());
        }
        totalLabel.setText(String.valueOf(total));
        passLabel.setText(String.valueOf(pass));
        failLabel.setText(String.valueOf(fail));
        totalLabel.repaint(); passLabel.repaint(); failLabel.repaint();
    }

    void deleteStudent(int row) {
        String name  = (String) tableModel.getValueAt(row, 1);
        String regNo = (String) tableModel.getValueAt(row, 2);
        int confirm = JOptionPane.showConfirmDialog(this,
                "<html>Delete <b>" + name + "</b> and ALL their records?</html>",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement psFetch = con.prepareStatement(
                    "SELECT id FROM students WHERE registration_no=?");
            psFetch.setString(1, regNo);
            ResultSet rs = psFetch.executeQuery();
            if (!rs.next()) return;
            int studentId = rs.getInt("id");

            // Delete courses → semesters → student (in order)
            PreparedStatement delCourses = con.prepareStatement(
                    "DELETE c FROM courses c JOIN semesters s ON c.semester_id=s.id WHERE s.student_id=?");
            delCourses.setInt(1, studentId);
            delCourses.executeUpdate();

            PreparedStatement delSems = con.prepareStatement(
                    "DELETE FROM semesters WHERE student_id=?");
            delSems.setInt(1, studentId);
            delSems.executeUpdate();

            PreparedStatement delStu = con.prepareStatement(
                    "DELETE FROM students WHERE id=?");
            delStu.setInt(1, studentId);
            delStu.executeUpdate();

            loadStudents();
            JOptionPane.showMessageDialog(this, "Student deleted successfully.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage());
        }
    }

    void editStudent(int row) {
        String regNo = (String) tableModel.getValueAt(row, 2);
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT id FROM students WHERE registration_no=?");
            ps.setString(1, regNo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) new AddStudentForm(this, rs.getInt("id"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void searchStudent() {
        String query = JOptionPane.showInputDialog(this, "Enter Roll No or Name:");
        if (query == null || query.isEmpty()) return;
        tableModel.setRowCount(0);
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM students WHERE name LIKE ? OR roll_no LIKE ?");
            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");
            ResultSet rs = ps.executeQuery();
            int i = 1;
            while (rs.next()) {
                double cgpa = rs.getDouble("cgpa");
                tableModel.addRow(new Object[]{
                        i++, rs.getString("name"), rs.getString("registration_no"),
                        rs.getString("roll_no"), rs.getString("batch"),
                        rs.getString("stream"), rs.getString("section"),
                        cgpa > 0 ? String.format("%.2f", cgpa) : "--", "actions"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Search failed: " + ex.getMessage());
        }
    }

    // ========================= ACTION BUTTONS =========================
    static class ActionButtonRenderer extends JPanel implements TableCellRenderer {
        private final JButton viewBtn, editBtn, delBtn;
        ActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 4, 8));
            setOpaque(true);
            viewBtn = pill("View",   new Color(50, 130, 255));
            editBtn = pill("Edit",   new Color(200, 130, 30));
            delBtn  = pill("Delete", new Color(200, 50,  50));
            add(viewBtn); add(editBtn); add(delBtn);
        }
        private JButton pill(String label, Color color) {
            JButton b = new JButton(label) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(color); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                    super.paintComponent(g);
                }
            };
            b.setFont(new Font("Segoe UI", Font.BOLD, 11));
            b.setForeground(Color.WHITE); b.setFocusPainted(false);
            b.setBorderPainted(false); b.setContentAreaFilled(false);
            b.setPreferredSize(new Dimension(58, 26));
            return b;
        }
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            setBackground(row % 2 == 0 ? new Color(22, 28, 60) : new Color(28, 36, 75));
            return this;
        }
    }

    static class ActionButtonEditor extends DefaultCellEditor {
        private final JPanel panel;
        private final JButton viewBtn, editBtn, delBtn;
        private Dashboard dash;
        private int currentRow = -1;

        ActionButtonEditor(JCheckBox cb, Dashboard d) {
            super(cb); this.dash = d; setClickCountToStart(1);
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 8));
            panel.setOpaque(true);
            viewBtn = pill("View",   new Color(50, 130, 255));
            editBtn = pill("Edit",   new Color(200, 130, 30));
            delBtn  = pill("Delete", new Color(200, 50,  50));
            panel.add(viewBtn); panel.add(editBtn); panel.add(delBtn);

            viewBtn.addActionListener(e -> {
                fireEditingStopped();
                new ViewResult((String) dash.tableModel.getValueAt(currentRow, 2));
            });
            editBtn.addActionListener(e -> { fireEditingStopped(); dash.editStudent(currentRow); });
            delBtn.addActionListener(e  -> { fireEditingStopped(); dash.deleteStudent(currentRow); });
        }

        private JButton pill(String label, Color color) {
            JButton b = new JButton(label) {
                boolean h = false;
                { addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { h = true; repaint(); }
                    public void mouseExited(MouseEvent e)  { h = false; repaint(); }
                }); }
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(h ? color.brighter() : color);
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                    super.paintComponent(g);
                }
            };
            b.setFont(new Font("Segoe UI", Font.BOLD, 11));
            b.setForeground(Color.WHITE); b.setFocusPainted(false);
            b.setBorderPainted(false); b.setContentAreaFilled(false);
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            b.setPreferredSize(new Dimension(58, 26));
            return b;
        }

        @Override public Component getTableCellEditorComponent(
                JTable t, Object v, boolean sel, int row, int col) {
            currentRow = row; panel.setBackground(new Color(38, 60, 140)); return panel;
        }
        @Override public Object getCellEditorValue() { return "actions"; }
    }
}