import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AddStudentForm extends JDialog {

    private JTextField nameField, regField, rollField, batchField, streamField, sectionField;
    private Dashboard parent;
    private int studentId;   // -1 = Add mode, else = Edit mode

    /**
     * @param parent    Dashboard reference (for refresh)
     * @param studentId Pass -1 for Add, pass actual DB id for Edit
     */
    public AddStudentForm(Dashboard parent, int studentId) {
        super(parent, studentId == -1 ? "Add New Student" : "Edit Student", true);
        this.parent    = parent;
        this.studentId = studentId;
        setSize(520, 530);
        setLocationRelativeTo(parent);
        setResizable(false);
        buildUI();
        if (studentId != -1) prefillData();
        setVisible(true);
    }

    private void buildUI() {
        JPanel bg = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(18, 22, 50));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        setContentPane(bg);

        boolean isEdit = studentId != -1;

        JLabel title = new JLabel(isEdit ? "Edit Student Details" : "Add New Student");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(isEdit ? new Color(255, 180, 60) : new Color(100, 180, 255));
        title.setBounds(20, 15, 440, 35);
        bg.add(title);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(60, 80, 140));
        sep.setBounds(20, 55, 470, 1);
        bg.add(sep);

        String[] labels   = {"Full Name", "Registration No", "Roll Number", "Batch", "Stream", "Section"};
        String[] defaults = {"ROHAN RAJABHAU JADHAV", "25DCOE1101190", "SCOD19", "2025-2028", "Bachelor of Technology", "D"};
        JTextField[] fields = new JTextField[6];

        for (int i = 0; i < labels.length; i++) {
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lbl.setForeground(new Color(160, 190, 230));
            lbl.setBounds(30, 75 + i * 58, 300, 20);
            bg.add(lbl);

            fields[i] = new JTextField(defaults[i]);
            styleField(fields[i]);
            fields[i].setBounds(30, 97 + i * 58, 450, 38);
            bg.add(fields[i]);
        }

        nameField   = fields[0]; regField  = fields[1]; rollField   = fields[2];
        batchField  = fields[3]; streamField = fields[4]; sectionField = fields[5];

        // Reg No non-editable in Edit mode
        if (isEdit) {
            regField.setEditable(false);
            regField.setForeground(new Color(150, 150, 180));
        }

        // Save Button
        String saveLabel = isEdit ? "Save Changes" : "Save Student";
        Color  saveColor = isEdit ? new Color(220, 130, 30) : new Color(40, 180, 120);
        JButton saveBtn = createBtn(saveLabel, saveColor);
        saveBtn.setBounds(30, 455, 210, 44);
        saveBtn.addActionListener(e -> { if (isEdit) updateStudent(); else saveStudent(); });
        bg.add(saveBtn);

        JButton cancelBtn = createBtn("Cancel", new Color(180, 60, 60));
        cancelBtn.setBounds(260, 455, 210, 44);
        cancelBtn.addActionListener(e -> dispose());
        bg.add(cancelBtn);
    }

    // Pre-fill fields when editing
    private void prefillData() {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM students WHERE id=?");
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                regField.setText(rs.getString("registration_no"));
                rollField.setText(rs.getString("roll_no"));
                batchField.setText(rs.getString("batch"));
                streamField.setText(rs.getString("stream"));
                sectionField.setText(rs.getString("section"));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not load data: " + ex.getMessage());
        }
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(Color.WHITE);
        field.setBackground(new Color(30, 38, 80));
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 100, 180), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
    }

    private JButton createBtn(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ==================== ADD STUDENT ====================
    private void saveStudent() {
        String name    = nameField.getText().trim().toUpperCase();
        String reg     = regField.getText().trim();
        String roll    = rollField.getText().trim().toUpperCase();
        String batch   = batchField.getText().trim();
        String stream  = streamField.getText().trim();
        String section = sectionField.getText().trim().toUpperCase();

        if (name.isEmpty() || reg.isEmpty() || roll.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, Reg No, and Roll No are required!");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement check = con.prepareStatement(
                    "SELECT id FROM students WHERE registration_no=?");
            check.setString(1, reg);
            if (check.executeQuery().next()) {
                JOptionPane.showMessageDialog(this, "Student with this Reg No already exists!");
                return;
            }

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO students (name, registration_no, roll_no, batch, stream, section, cgpa) " +
                            "VALUES (?,?,?,?,?,?,0)");
            ps.setString(1, name); ps.setString(2, reg); ps.setString(3, roll);
            ps.setString(4, batch); ps.setString(5, stream); ps.setString(6, section);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Student added successfully!");
            parent.loadStudents();
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // ==================== EDIT / UPDATE STUDENT ====================
    private void updateStudent() {
        String name    = nameField.getText().trim().toUpperCase();
        String roll    = rollField.getText().trim().toUpperCase();
        String batch   = batchField.getText().trim();
        String stream  = streamField.getText().trim();
        String section = sectionField.getText().trim().toUpperCase();

        if (name.isEmpty() || roll.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and Roll No are required!");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE students SET name=?, roll_no=?, batch=?, stream=?, section=? WHERE id=?");
            ps.setString(1, name); ps.setString(2, roll); ps.setString(3, batch);
            ps.setString(4, stream); ps.setString(5, section); ps.setInt(6, studentId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Student updated successfully!");
            parent.loadStudents();
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Update failed: " + ex.getMessage());
        }
    }
}