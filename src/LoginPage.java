package ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

public class LoginPage extends JFrame {

    private boolean darkMode = false;

    public LoginPage() {
        setTitle("Attendance System - Login");
        setSize(1000, 800);
        setMinimumSize(new Dimension(360, 450));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Background panel with custom paint
        JPanel backgroundPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (!darkMode) {
                    Graphics2D g2 = (Graphics2D) g;
                    GradientPaint gp = new GradientPaint(0, 0, new Color(240, 245, 255),
                            0, getHeight(), new Color(200, 220, 255));
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    setBackground(new Color(30, 30, 30));
                }
            }
        };
        backgroundPanel.setLayout(new BorderLayout());
        setContentPane(backgroundPanel);

        // Logo
        ImageIcon logoIcon = new ImageIcon("assets/logo.png");
        Image scaledLogo = logoIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledLogo));
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Title
        JLabel titleLabel = new JLabel("                  Select Login Role", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        titleLabel.setBackground(new Color(70, 130, 180));
        titleLabel.setForeground(Color.RED);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        titleLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Dark mode toggle
        JButton darkModeToggle = new JButton("DARK");
        darkModeToggle.setFocusPainted(false);
        darkModeToggle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        darkModeToggle.setContentAreaFilled(false);
        darkModeToggle.setBorderPainted(false);
        darkModeToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        darkModeToggle.setToolTipText("Toggle Dark Mode");

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(darkModeToggle, BorderLayout.EAST);
        topPanel.add(titleLabel, BorderLayout.CENTER);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);
        northPanel.add(logoLabel, BorderLayout.NORTH);
        northPanel.add(topPanel, BorderLayout.SOUTH);
        backgroundPanel.add(northPanel, BorderLayout.NORTH);

        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 20, 20));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 60, 20, 60));

        JButton studentBtn = createStyledButton("Student Login");
        JButton professorBtn = createStyledButton("Professor Login");
        JButton adminBtn = createStyledButton("Admin Login");

        buttonPanel.add(studentBtn);
        buttonPanel.add(professorBtn);
        buttonPanel.add(adminBtn);
        backgroundPanel.add(buttonPanel, BorderLayout.CENTER);

        studentBtn.addActionListener(e -> showLoginDialog("Student"));
        professorBtn.addActionListener(e -> showLoginDialog("Professor"));
        adminBtn.addActionListener(e -> showLoginDialog("Admin"));

        // Dark mode logic
        darkModeToggle.addActionListener(e -> {
            darkMode = !darkMode;
            SwingUtilities.updateComponentTreeUI(this);
            repaint();
        });

        // Responsive font scaling
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                int width = getWidth();
                int fontSize = Math.max(14, width / 30);
                Font buttonFont = new Font("Segoe UI", Font.BOLD, fontSize);
                studentBtn.setFont(buttonFont);
                professorBtn.setFont(buttonFont);
                adminBtn.setFont(buttonFont);
                titleLabel.setFont(new Font("Segoe UI", Font.BOLD, fontSize + 4));
            }
        });

        setVisible(true);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(60, 110, 160));
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(70, 130, 180));
            }
        });
        return button;
    }

    public boolean validateLogin(String role, String username, String password) {
        String filename = switch (role) {
            case "Student" -> "students.txt";
            case "Professor" -> "professors.txt";
            case "Admin" -> "admin.txt";
            default -> null;
        };

        if (filename == null) return false;

        try (BufferedReader br = new BufferedReader(new FileReader("data/" + filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2 && parts[0].equals(username) && parts[1].equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void showLoginDialog(String role) {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        panel.add(new JLabel(role + " Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        int option = JOptionPane.showConfirmDialog(
                this,
                panel,
                role + " Login",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (validateLogin(role, username, password)) {
                dispose();
                switch (role) {
                    case "Student" -> new StudentDashboard(username);
                    case "Professor" -> new ProfessorDashboard(username).setVisible(true);
                    case "Admin" -> new AdminDashboard();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginPage::new);
    }
}
