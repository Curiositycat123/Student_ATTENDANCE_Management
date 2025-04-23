package ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class AdminDashboard extends JFrame {
    private JTextArea holidayArea;
    private JTextField usernameField, passwordField, courseField;
    private JComboBox<String> roleCombo;
    private JComboBox<String> workingDayCombo, workingAsCombo;
    private JComboBox<String> professorCourseCombo;
    private final File userFile = new File("users.txt");
    private final File holidayFile = new File("data/holidays.txt");
    private final File weekendFile = new File("data/weekend_overrides.txt");
    private final File studentFile = new File("data/students.txt");
    private final File professorFile = new File("data/professors.txt");

    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setSize(1000, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        initUI();
        setVisible(true);
    }

    private void initUI() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(4, 1, 10, 10));
        sidebar.setBackground(Color.decode("#2563EB"));
        sidebar.setPreferredSize(new Dimension(200, getHeight()));

        JButton createUserBtn = new JButton("Create User");
        JButton manageHolidayBtn = new JButton("Manage Holidays");
        JButton logoutBtn = new JButton("Logout");

        for (JButton btn : new JButton[]{createUserBtn, manageHolidayBtn}) {
            btn.setBackground(Color.WHITE);
            btn.setForeground(Color.BLACK);
            sidebar.add(btn);
        }

        logoutBtn.setBackground(Color.decode("#DC3545"));
        logoutBtn.setForeground(Color.WHITE);
        sidebar.add(logoutBtn);

        JPanel contentPanel = new JPanel(new CardLayout());
        JPanel createUserPanel = createUserPanel();
        JPanel holidayPanel = createHolidayPanel();

        contentPanel.add(createUserPanel, "CreateUser");
        contentPanel.add(holidayPanel, "Holiday");

        createUserBtn.addActionListener(e -> ((CardLayout) contentPanel.getLayout()).show(contentPanel, "CreateUser"));
        manageHolidayBtn.addActionListener(e -> {
            loadHolidayData();
            ((CardLayout) contentPanel.getLayout()).show(contentPanel, "Holiday");
        });

        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginPage();
        });

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    private boolean isCourseAlreadyAssigned(String courseCode) {
        try (BufferedReader reader = new BufferedReader(new FileReader(professorFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3 && parts[2].trim().equalsIgnoreCase(courseCode.trim())) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private JPanel createUserPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBackground(Color.WHITE);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(15, 15, 15, 15);

    JLabel title = new JLabel("Create User Account");
    title.setFont(new Font("SansSerif", Font.BOLD, 20));
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 2;
    panel.add(title, gbc);

    gbc.gridwidth = 1;
    gbc.gridy++;

    // Username Field
    panel.add(new JLabel("Username:"), gbc);
    gbc.gridx = 1;
    usernameField = new JTextField(20);
    panel.add(usernameField, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    // Password Field
    panel.add(new JLabel("Password:"), gbc);
    gbc.gridx = 1;
    passwordField = new JTextField(20);
    panel.add(passwordField, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    // Role Dropdown
    panel.add(new JLabel("Role:"), gbc);
    gbc.gridx = 1;
    roleCombo = new JComboBox<>(new String[]{"Student", "Professor"});
    panel.add(roleCombo, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    // Course Field
    panel.add(new JLabel("Course Codes (semi-colon_separated):"), gbc);
    gbc.gridx = 1;
    courseField = new JTextField(20);
    panel.add(courseField, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    panel.add(new JLabel("Course LIST ->A: OOP, B: Physics, C: Elec, D: DSML, E: Math, F: Ecology"), gbc);
    gbc.gridx = 1;

    gbc.gridx = 0;
    gbc.gridy++;
    // Submit Button
    JButton createBtn = new JButton("Create User");
    createBtn.setBackground(Color.decode("#2563EB"));
    createBtn.setForeground(Color.WHITE);
    createBtn.setFont(new Font("Arial", Font.BOLD, 16));
    createBtn.setPreferredSize(new Dimension(200, 40));
    createBtn.setFocusPainted(false);
    createBtn.addActionListener(this::createUser);
    panel.add(createBtn, gbc);

    return panel;
}

private int countLines(File file) {
    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
        while (br.readLine() != null) count++;
    } catch (IOException e) {
        e.printStackTrace();
    }
    return count;
}

private boolean isUserWithSameNameAndCourses(String username, String courseInput) {
    try (BufferedReader reader = new BufferedReader(new FileReader(studentFile))) {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length >= 3) {
                String existingUsername = parts[0].trim();
                String[] existingCourses = parts[2].split(";");
                String[] newCourses = courseInput.split(";");

                // Sort both arrays to compare regardless of order
                Arrays.sort(existingCourses);
                Arrays.sort(newCourses);

                if (existingUsername.equalsIgnoreCase(username.trim()) &&
                    Arrays.equals(existingCourses, newCourses)) {
                    return true;
                }
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
    return false;
}

private boolean isUserExists(String username, String role) {
    username = username.toLowerCase().trim();
    File fileToCheck = role.equals("Student") ? studentFile : professorFile;

    try (BufferedReader br = new BufferedReader(new FileReader(fileToCheck))) {
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length >= 1 && parts[0].trim().equalsIgnoreCase(username)) {
                return true;
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
    return false;
}

private boolean isUserInFile(String username, File file) {
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length >= 1 && parts[0].trim().equalsIgnoreCase(username)) {
                return true;
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
    return false;
}

    private void createUser(ActionEvent e) {
    String username = usernameField.getText().trim();
    String password = passwordField.getText().trim();
    String role = (String) roleCombo.getSelectedItem();
    String courseInput = courseField.getText().trim();

    if (username.isEmpty() || password.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Username and Password cannot be empty.");
        return;
    }

    // Validate course codes input (only "A", "B", "C", "D", "E", "F" are allowed)
    if (!isValidCourseCode(courseInput)) {
        JOptionPane.showMessageDialog(this, "Please enter valid course codes (A-F).");
        return;
    }

        if (username.isEmpty() || password.isEmpty() || courseInput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

       if (isUserExists(username, role)) {
    JOptionPane.showMessageDialog(this, "Username already exists in the " + role + " records.");
    return;
}

if (isUserWithSameNameAndCourses(username, courseInput)) {
    JOptionPane.showMessageDialog(this,
        "A user with the same username and course codes already exists.",
        "Duplicate User",
        JOptionPane.ERROR_MESSAGE);
    return;
}

        if (role.equals("Professor")) {
    String[] courses = courseInput.split(";");
    if (courses.length != 1) {
        JOptionPane.showMessageDialog(this,
            "Professors can only be assigned to ONE course.",
            "Error",
            JOptionPane.ERROR_MESSAGE);
        return;
    }

    String courseCode = courses[0].trim();
    if (isCourseAlreadyAssigned(courseCode)) {
        JOptionPane.showMessageDialog(this,
                "Course code '" + courseCode + "' is already assigned to a professor.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        return;
    }
}

        if (role.equals("Professor")) {
            String courseCode = courseInput.trim();
            if (isCourseAlreadyAssigned(courseCode)) {
                JOptionPane.showMessageDialog(this,
                        "Course code '" + courseCode + "' is already assigned to a professor.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(userFile, true))) {
            bw.write(role + "," + username + "," + password);
            bw.newLine();
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error writing to users.txt");
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(role.equals("Student") ? studentFile : professorFile, true))) {
            bw.write(username + "," + password + "," + courseInput);
            bw.newLine();
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error writing to " + (role.equals("Student") ? "students.txt" : "professors.txt"));
            return;
        }

        JOptionPane.showMessageDialog(this, "User created successfully.");
        usernameField.setText("");
        passwordField.setText("");
        courseField.setText("");
    }

    private boolean isValidCourseCode(String input) {
    // Define valid course codes (A-F)
    String[] validCourseCodes = {"A", "B", "C", "D", "E", "F"};
    String[] courseCodes = input.split(";");
    
    for (String code : courseCodes) {
        code = code.trim().toUpperCase(); 
        if (!Arrays.asList(validCourseCodes).contains(code)) {
            return false; 
        }
    }
    return true; 
}

    private JPanel createHolidayPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        holidayArea = new JTextArea();
        holidayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(holidayArea);

        JPanel controls = new JPanel(new GridLayout(3, 1, 10, 10));
        controls.setBackground(Color.WHITE);

        JPanel declarePanel = new JPanel();
        declarePanel.setBackground(Color.WHITE);

        JTextField holidayNameField = new JTextField(15); // Field for holiday name
        JTextField dateField = new JTextField(10); // Field for date
        JButton declareBtn = new JButton("Declare Holiday");
        JButton revokeBtn = new JButton("Revoke Holiday");

        declarePanel.add(new JLabel("Holiday Name:"));
        declarePanel.add(holidayNameField);
        declarePanel.add(new JLabel("Date (yyyy-mm-dd):"));
        declarePanel.add(dateField);
        declarePanel.add(declareBtn);
        declarePanel.add(revokeBtn);

        declareBtn.addActionListener(e -> {
            String name = holidayNameField.getText().trim();
            String date = dateField.getText().trim();
            if (name.isEmpty() || date.isEmpty() || !isValidDate(date)) {
                JOptionPane.showMessageDialog(this, "Please provide a valid holiday name and date.");
                return;
            }
            declareHoliday(name, date);
        });

        revokeBtn.addActionListener(e -> {
            String name = holidayNameField.getText().trim();
            String date = dateField.getText().trim();
            if (name.isEmpty() || date.isEmpty() || !isValidDate(date)) {
                JOptionPane.showMessageDialog(this, "Please provide a valid holiday name and date.");
                return;
            }
            revokeHoliday(name, date);
        });

        controls.add(declarePanel);
        controls.add(new JLabel()); 
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(controls, BorderLayout.SOUTH);
        return panel;
    }

    private void declareHoliday(String name, String date) {
        // Check if the holiday date already has a holiday declared
        if (isDateAlreadyHasHoliday(date)) {
            JOptionPane.showMessageDialog(this, "A holiday is already declared for the date " + date + ".");
            return;
        }

        // Check for duplicate holiday name and date
        if (isHolidayAlreadyDeclared(name, date)) {
            JOptionPane.showMessageDialog(this, "Holiday '" + name + "' on " + date + " is already declared.");
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(holidayFile, true))) {
            bw.write(name + "," + date);  // Store both holiday name and date
            bw.newLine();

           
            JOptionPane.showMessageDialog(this, "Holiday declared: " + name + " on " + date);
            loadHolidayData();
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving holiday.");
        }
    }

    private boolean isHolidayAlreadyDeclared(String name, String date) {
        try {
            List<String> lines = readFile(holidayFile);
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length == 2 && parts[0].trim().equalsIgnoreCase(name) && parts[1].trim().equalsIgnoreCase(date)) {
                    return true;  // Holiday is already declared
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private boolean isDateAlreadyHasHoliday(String date) {
        try {
            List<String> lines = readFile(holidayFile);
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length == 2 && parts[1].trim().equalsIgnoreCase(date)) {
                    return true;  // Date already has a holiday
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private void revokeHoliday(String name, String date) {
        // Load the current holidays
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(holidayFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2 && parts[0].trim().equalsIgnoreCase(name) && parts[1].trim().equalsIgnoreCase(date)) {
                    continue;  // Skip the holiday to revoke
                }
                lines.add(line);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Write the updated holidays back to the file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(holidayFile))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
            JOptionPane.showMessageDialog(this, "Holiday revoked: " + name + " on " + date);
            loadHolidayData();
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error revoking holiday.");
        }
    }

    private void loadHolidayData() {
        StringBuilder holidayData = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(holidayFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                holidayData.append(line).append("\n");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        holidayArea.setText(holidayData.toString());
    }

    private boolean isValidDate(String date) {
        try {
            LocalDate.parse(date);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private List<String> readFile(File file) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }
}
