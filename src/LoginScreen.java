    import javax.swing.*;
    import javax.swing.border.*;
    import java.awt.*;
    import java.awt.event.*;
    import java.sql.*;

    public class LoginScreen extends JFrame {

        private JTextField     userField;
        private JPasswordField passField;
        private JButton        loginBtn;
        private JLabel         statusLabel;
        private float          alpha = 0f;
        private Timer          fadeTimer;

        // Animated particles
        private int[] px = new int[30], py = new int[30], pspeed = new int[30];
        private Timer  particleTimer;

        public LoginScreen() {
            setTitle("Student Result Management System");
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setSize(480, 560);
            setLocationRelativeTo(null);
            setUndecorated(true);
            setResizable(false);
            initParticles();
            buildUI();
            startFadeIn();
            setVisible(true);
        }

        private void initParticles() {
            for (int i = 0; i < 30; i++) {
                px[i] = (int)(Math.random() * 480);
                py[i] = (int)(Math.random() * 560);
                pspeed[i] = 1 + (int)(Math.random() * 2);
            }
        }

        private void buildUI() {
            JPanel bg = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    GradientPaint gp = new GradientPaint(0, 0, new Color(13, 17, 35),
                            getWidth(), getHeight(), new Color(25, 35, 70));
                    g2.setPaint(gp); g2.fillRect(0, 0, getWidth(), getHeight());

                    // Particles
                    g2.setColor(new Color(100, 149, 237, 60));
                    for (int i = 0; i < 30; i++) g2.fillOval(px[i], py[i], 4, 4);

                    // Connecting lines
                    g2.setColor(new Color(100, 149, 237, 25));
                    g2.setStroke(new BasicStroke(1));
                    for (int i = 0; i < 30; i++) {
                        for (int j = i + 1; j < 30; j++) {
                            int dist = (int) Math.sqrt(Math.pow(px[i]-px[j],2) + Math.pow(py[i]-py[j],2));
                            if (dist < 100) g2.drawLine(px[i], py[i], px[j], py[j]);
                        }
                    }
                }
            };
            bg.setLayout(null);
            setContentPane(bg);

            particleTimer = new Timer(30, e -> {
                for (int i = 0; i < 30; i++) {
                    py[i] -= pspeed[i];
                    if (py[i] < -10) { py[i] = 570; px[i] = (int)(Math.random() * 480); }
                }
                bg.repaint();
            });
            particleTimer.start();

            // Card
            JPanel card = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(255, 255, 255, 15));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    g2.setColor(new Color(100, 149, 237, 80));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                }
            };
            card.setLayout(null); card.setOpaque(false);
            card.setBounds(60, 65, 360, 430);
            bg.add(card);

            // GHRCEM painted logo at top of card
            JPanel logoMini = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                    // Circle badge
                    int cx = getWidth()/2 - 10;
                    g2.setColor(new Color(80, 25, 130));
                    g2.fillOval(cx - 22, 4, 42, 42);
                    g2.setColor(new Color(255, 140, 0)); g2.setStroke(new BasicStroke(1.5f));
                    g2.drawOval(cx - 22, 4, 42, 42);
                    g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
                    g2.setColor(Color.WHITE);
                    g2.drawString("\uD83E\uDD81", cx - 17, 35);

                    // "GHraisoni COLLEGE"
                    FontMetrics fm;
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    g2.setColor(new Color(130, 60, 200));
                    int textX = cx + 24;
                    g2.drawString("GH", textX, 20);
                    fm = g2.getFontMetrics();
                    int ghW = fm.stringWidth("GH");
                    g2.setColor(new Color(100, 40, 170));
                    g2.drawString("raisoni", textX + ghW, 20);

                    g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    g2.setColor(new Color(110, 50, 180));
                    g2.drawString("COLLEGE", textX, 36);

                    g2.setColor(new Color(255, 130, 0));
                    g2.setStroke(new BasicStroke(1.8f));
                    g2.drawLine(textX, 40, textX + 110, 40);

                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                    g2.setColor(new Color(170, 170, 210));
                    g2.drawString("Engineering & Management, Pune", textX, 52);
                }
            };
            logoMini.setOpaque(false);
            logoMini.setBounds(0, 14, 360, 62);
            card.add(logoMini);

            JSeparator sep = new JSeparator();
            sep.setBounds(30, 80, 300, 1);
            sep.setForeground(new Color(100, 149, 237, 80));
            card.add(sep);

            // Subtitle
            JLabel subtitle = new JLabel("Result Management Portal", SwingConstants.CENTER);
            subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            subtitle.setForeground(new Color(180, 200, 230));
            subtitle.setBounds(0, 86, 360, 22);
            card.add(subtitle);

            // Username
            JLabel userLabel = new JLabel("Username");
            userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            userLabel.setForeground(new Color(160, 190, 230));
            userLabel.setBounds(40, 122, 280, 20);
            card.add(userLabel);

            userField = createStyledField("admin");
            userField.setBounds(40, 144, 280, 42);
            card.add(userField);

            // Password — case sensitive note
            JLabel passLabel = new JLabel("Password  (case-sensitive)");
            passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            passLabel.setForeground(new Color(160, 190, 230));
            passLabel.setBounds(40, 198, 280, 20);
            card.add(passLabel);

            passField = new JPasswordField();
            passField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            passField.setForeground(Color.WHITE);
            passField.setCaretColor(Color.WHITE);
            passField.setBackground(new Color(255, 255, 255, 20));
            passField.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(new Color(100, 149, 237, 120), 10),
                    BorderFactory.createEmptyBorder(5, 12, 5, 12)));
            passField.setOpaque(false);
            passField.setBounds(40, 220, 280, 42);
            passField.addActionListener(e -> doLogin());
            card.add(passField);

            // Show/hide password toggle
            JCheckBox showPass = new JCheckBox("Show password");
            showPass.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            showPass.setForeground(new Color(140, 170, 210));
            showPass.setOpaque(false);
            showPass.setBounds(40, 268, 160, 22);
            showPass.addActionListener(e -> {
                if (showPass.isSelected()) passField.setEchoChar((char)0);
                else passField.setEchoChar('•');
            });
            card.add(showPass);

            // Status label
            statusLabel = new JLabel("", SwingConstants.CENTER);
            statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            statusLabel.setForeground(new Color(255, 100, 100));
            statusLabel.setBounds(40, 298, 280, 20);
            card.add(statusLabel);

            // Login Button
            loginBtn = new JButton("LOGIN") {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    GradientPaint gp = new GradientPaint(0, 0, new Color(50, 130, 255),
                            getWidth(), 0, new Color(0, 200, 180));
                    g2.setPaint(gp); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    super.paintComponent(g);
                }
            };
            loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
            loginBtn.setForeground(Color.WHITE);
            loginBtn.setFocusPainted(false); loginBtn.setBorderPainted(false);
            loginBtn.setContentAreaFilled(false);
            loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            loginBtn.setBounds(40, 328, 280, 46);
            loginBtn.addActionListener(e -> doLogin());
            card.add(loginBtn);

            // Version tag
            JLabel ver = new JLabel("v1.0  \u2022  GHRCEM Internal", SwingConstants.CENTER);
            ver.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            ver.setForeground(new Color(100, 120, 160));
            ver.setBounds(0, 390, 360, 18);
            card.add(ver);

            // Drag to move (undecorated window)
            Point[] dragOrigin = {null};
            card.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) { dragOrigin[0] = e.getPoint(); }
            });
            card.addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    if (dragOrigin[0] != null) {
                        Point loc = getLocation();
                        setLocation(loc.x + e.getX() - dragOrigin[0].x,
                                loc.y + e.getY() - dragOrigin[0].y);
                    }
                }
            });
        }

        private JTextField createStyledField(String placeholder) {
            JTextField field = new JTextField(placeholder) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(255,255,255,20));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    super.paintComponent(g);
                }
            };
            field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            field.setForeground(Color.WHITE); field.setCaretColor(Color.WHITE); field.setOpaque(false);
            field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(new Color(100, 149, 237, 120), 10),
                    BorderFactory.createEmptyBorder(5, 12, 5, 12)));
            return field;
        }

        private void doLogin() {
            String user = userField.getText().trim();
            String pass = new String(passField.getPassword()); // preserve case exactly

            if (user.isEmpty() || pass.isEmpty()) {
                statusLabel.setText("Username and password required.");
                return;
            }

            loginBtn.setText("Logging in...");
            loginBtn.setEnabled(false);
            statusLabel.setText("");

            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override protected Boolean doInBackground() {
                    try (Connection con = DBConnection.getConnection()) {
                        // BINARY keyword ensures case-sensitive comparison in MySQL
                        PreparedStatement ps = con.prepareStatement(
                                "SELECT * FROM admin WHERE BINARY username=? AND BINARY password=?");
                        ps.setString(1, user);
                        ps.setString(2, pass);
                        return ps.executeQuery().next();
                    } catch (Exception ex) {
                        // Fallback for demo (case-sensitive comparison done in Java)
                        return user.equals("admin") && pass.equals("admin123");
                    }
                }
                @Override protected void done() {
                    try {
                        if (get()) {
                            particleTimer.stop();
                            dispose();
                            new Dashboard();
                        } else {
                            statusLabel.setText("Invalid credentials! (Password is case-sensitive)");
                            loginBtn.setText("LOGIN");
                            loginBtn.setEnabled(true);
                            shakeFrame();
                        }
                    } catch (Exception ex) {
                        loginBtn.setText("LOGIN"); loginBtn.setEnabled(true);
                    }
                }
            };
            worker.execute();
        }

        private void shakeFrame() {
            Point orig = getLocation();
            Timer t = new Timer(30, null);
            int[] count = {0};
            int[] offsets = {10, -10, 8, -8, 5, -5, 0};
            t.addActionListener(e -> {
                if (count[0] < offsets.length) setLocation(orig.x + offsets[count[0]++], orig.y);
                else { setLocation(orig); t.stop(); }
            });
            t.start();
        }

        private void startFadeIn() {
            setOpacity(0.0f);
            fadeTimer = new Timer(20, null);
            fadeTimer.addActionListener(e -> {
                alpha += 0.05f;
                if (alpha >= 1.0f) { alpha = 1.0f; fadeTimer.stop(); }
                setOpacity(alpha);
            });
            fadeTimer.start();
        }

        static class RoundedBorder implements Border {
            private Color color; private int radius;
            RoundedBorder(Color c, int r) { color = c; radius = r; }
            public Insets getBorderInsets(Component c) { return new Insets(4,4,4,4); }
            public boolean isBorderOpaque() { return false; }
            public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color); g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(x, y, w-1, h-1, radius, radius);
            }
        }

        public static void main(String[] args) {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            SwingUtilities.invokeLater(LoginScreen::new);
        }
    }