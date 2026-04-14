import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ViewResult extends JFrame {

    private String regNo;

    public ViewResult(String regNo) {
        this.regNo = regNo;
        init();
        loadResult();
    }

    public ViewResult() {
        regNo = JOptionPane.showInputDialog(null, "Enter Registration No or Roll No:");
        if (regNo == null || regNo.isEmpty()) { dispose(); return; }
        init();
        loadResult();
    }

    private void init() {
        setTitle("Student Result — " + regNo);
        setSize(1050, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private void loadResult() {
        JPanel root = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(245, 248, 255));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        root.setLayout(new BorderLayout(0, 0));
        setContentPane(root);

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM students WHERE registration_no=? OR roll_no=?");
            ps.setString(1, regNo);
            ps.setString(2, regNo);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Student not found!");
                dispose();
                return;
            }

            int studentId = rs.getInt("id");
            buildHeader(root, rs);
            buildSemesterTabs(root, con, studentId);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
        revalidate();
        repaint();
    }

    private void buildHeader(JPanel root, ResultSet rs) throws SQLException {
        JPanel topBar = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(22, 48, 100));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        topBar.setPreferredSize(new Dimension(0, 55));

        JLabel sysTitle = new JLabel("  🎓 GHRCEM — Student Result Portal");
        sysTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        sysTitle.setForeground(Color.WHITE);
        sysTitle.setBounds(10, 12, 500, 30);
        topBar.add(sysTitle);

        JLabel welcomeLabel = new JLabel("Welcome " + rs.getString("name") + "  ", SwingConstants.RIGHT);
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        welcomeLabel.setForeground(new Color(180, 210, 255));
        welcomeLabel.setBounds(600, 15, 430, 25);
        topBar.add(welcomeLabel);

        // Student Info Card
        JPanel infoCard = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(220, 230, 250));
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            }
        };
        infoCard.setPreferredSize(new Dimension(0, 175));

        JLabel infoTitle = new JLabel("  Student Information");
        infoTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        infoTitle.setForeground(new Color(20, 80, 180));
        infoTitle.setBounds(10, 8, 300, 25);
        infoCard.add(infoTitle);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(200, 215, 245));
        sep.setBounds(10, 35, 1010, 1);
        infoCard.add(sep);

        Object[][] info = {
                {"Name :", rs.getString("name"), "Stream :", rs.getString("stream"), "Academic Session :", "Summer 2026"},
                {"Registration Number :", rs.getString("registration_no"), "Class Section :", rs.getString("section"), "Total Components :", "11"},
                {"Academic Batch :", rs.getString("batch"), "Roll Number :", rs.getString("roll_no"), "CGPA :", String.format("%.2f", rs.getDouble("cgpa"))},
                {"Major :", "--", "Minor :", "--", "", ""},
        };

        int[][] positions = {{10, 50}, {10, 80}, {10, 110}, {10, 140}};
        for (int row = 0; row < info.length; row++) {
            int[] pos = positions[row];
            int xOff = 0;
            for (int col = 0; col < info[row].length; col++) {
                boolean isLabel = col % 2 == 0;
                JLabel lbl = new JLabel(String.valueOf(info[row][col]));
                lbl.setFont(new Font("Segoe UI", isLabel ? Font.PLAIN : Font.BOLD, 13));
                lbl.setForeground(isLabel ? new Color(100, 120, 160) : new Color(20, 30, 70));
                lbl.setBounds(pos[0] + xOff, pos[1], isLabel ? 180 : 200, 22);
                infoCard.add(lbl);
                xOff += isLabel ? 180 : 210;
            }
        }

        JPanel northStack = new JPanel(new BorderLayout());
        northStack.add(topBar, BorderLayout.NORTH);
        northStack.add(infoCard, BorderLayout.CENTER);
        root.add(northStack, BorderLayout.NORTH);
    }

    private void buildSemesterTabs(JPanel root, Connection con, int studentId) throws SQLException {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.setBackground(new Color(245, 248, 255));

        PreparedStatement semPs = con.prepareStatement(
                "SELECT * FROM semesters WHERE student_id=? ORDER BY sem_number");
        semPs.setInt(1, studentId);
        ResultSet semRs = semPs.executeQuery();

        boolean hasSems = false;
        while (semRs.next()) {
            hasSems = true;
            int semId = semRs.getInt("id");
            int semNo = semRs.getInt("sem_number");
            String session = semRs.getString("session");
            double sgpa = semRs.getDouble("sgpa");

            JPanel semContent = buildSemPanel(con, semId, semNo, session, sgpa);
            tabs.addTab("Semester " + toRoman(semNo) + " — " + session, semContent);
        }

        if (!hasSems) {
            JLabel noData = new JLabel("No semester data found. Add marks first.", SwingConstants.CENTER);
            noData.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            noData.setForeground(new Color(150, 150, 200));
            root.add(noData, BorderLayout.CENTER);
            return;
        }

        root.add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildSemPanel(Connection con, int semId, int semNo, String session, double sgpa) throws SQLException {

        JPanel panel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(245, 248, 255));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // Header (UNCHANGED UI)
        JPanel semHeader = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(235, 241, 255));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        semHeader.setPreferredSize(new Dimension(0, 40));

        JLabel semTitle = new JLabel("  Semester " + toRoman(semNo) + ", " + session);
        semTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        semTitle.setForeground(new Color(20, 60, 150));
        semTitle.setBounds(10, 8, 400, 25);
        semHeader.add(semTitle);

        JLabel sgpaLabel = new JLabel("SGPA : " + sgpa + "  ▲", SwingConstants.RIGHT);
        sgpaLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sgpaLabel.setForeground(new Color(20, 60, 150));
        sgpaLabel.setBounds(700, 8, 300, 25);
        semHeader.add(sgpaLabel);

        panel.add(semHeader, BorderLayout.NORTH);

        // Table (UNCHANGED UI)
        String[] cols = {"S.No", "Course Code", "Course Name", "Component", "Session", "Credits",
                "Marks", "Percentage", "Grade", "Grade Point", "Result", "Reg Type", "Status"};

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        styleResultTable(table);

        // 🔥 FIXED LOGIC START
        PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM courses WHERE semester_id=? ORDER BY course_code, component");
        ps.setInt(1, semId);

        ResultSet rs = ps.executeQuery();

        int sno = 1;

        if (!rs.next()) {
            JLabel noData = new JLabel("No subjects found.", SwingConstants.CENTER);
            panel.add(noData, BorderLayout.CENTER);
            return panel;
        }

        do {
            double pct = rs.getDouble("percentage");

            // fallback calculation
            if (pct == 0) {
                int m = rs.getInt("marks_obtained");
                int t = rs.getInt("marks_total");
                pct = (t > 0) ? (m * 100.0 / t) : 0;
            }

            model.addRow(new Object[]{
                    sno++,
                    rs.getString("course_code"),
                    rs.getString("course_name"),
                    rs.getString("component"),
                    session,
                    rs.getInt("credits"),
                    rs.getInt("marks_obtained") + " / " + rs.getInt("marks_total"),
                    String.format("%.1f%%", pct),
                    rs.getString("grade"),
                    rs.getInt("grade_point"),
                    rs.getString("result"),
                    rs.getString("register_type"),
                    rs.getString("status")
            });

        } while (rs.next());
        // 🔥 FIXED LOGIC END

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void styleResultTable(JTable table) {

        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(36);

        // HEADER FIX 🔥
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(30, 60, 140));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setOpaque(true);

        // Force renderer (IMPORTANT)
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {

                JLabel lbl = new JLabel(value.toString());
                lbl.setOpaque(true);
                lbl.setBackground(new Color(30, 60, 140));
                lbl.setForeground(Color.WHITE);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                return lbl;
            }
        });

        // BODY
        table.setBackground(Color.WHITE);
        table.setForeground(new Color(30, 40, 80));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                           boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);

                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 248, 255));
                c.setForeground(new Color(30, 40, 80));

                if (col == 10) {
                    String val = v == null ? "" : v.toString();
                    if (val.equals("PASS")) {
                        c.setBackground(new Color(200, 255, 220));
                        c.setForeground(new Color(0, 120, 60));
                    } else if (val.equals("FAIL")) {
                        c.setBackground(new Color(255, 210, 210));
                        c.setForeground(new Color(180, 0, 0));
                    }
                }

                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
    }

    private String toRoman(int n) {
        return switch (n) {
            case 1 -> "I";   case 2 -> "II";   case 3 -> "III";  case 4 -> "IV";
            case 5 -> "V";   case 6 -> "VI";   case 7 -> "VII";  case 8 -> "VIII";
            default -> String.valueOf(n);
        };
    }
}