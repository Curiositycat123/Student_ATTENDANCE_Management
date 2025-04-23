package ui;

import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class ProfessorDashboard extends JFrame {
    private List<JCheckBox> checkBoxes = new ArrayList<>();

    private String username;
    private String courseCode;

    public ProfessorDashboard(String username) {
        this.username = username;
        this.courseCode = getCourseForProfessor(username);
        setupUI();
    }

    private void setupUI() {
        setTitle("Professor Dashboard");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        String today = new SimpleDateFormat("yyyy-MM-dd (EEEE)").format(new Date());
        JLabel header = new JLabel("Welcome, Professor " + username + " — " + today, SwingConstants.CENTER);
        header.setOpaque(true);
        header.setBackground(Color.decode("#2563EB"));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 20));
        add(header, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        JButton dashboardBtn = new JButton("Dashboard");
        JButton scheduleBtn = new JButton("Schedule");
        JButton logoutBtn = new JButton("Logout");

        for (JButton btn : Arrays.asList(dashboardBtn, scheduleBtn)) {
            btn.setBackground(Color.decode("#2563EB"));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            buttonPanel.add(btn);
        }

        logoutBtn.setMaximumSize(new Dimension(160, 40));
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.setBackground(new Color(220, 53, 69));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        buttonPanel.add(logoutBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        JPanel contentPanel = new JPanel(new CardLayout());
        JScrollPane dashboardPanel = createDashboardPanel();
        JScrollPane schedulePanel = createSchedulePanel();
        contentPanel.add(dashboardPanel, "Dashboard");
        contentPanel.add(schedulePanel, "Schedule");

        add(contentPanel, BorderLayout.CENTER);

        CardLayout cl = (CardLayout) contentPanel.getLayout();

        dashboardBtn.addActionListener(e -> cl.show(contentPanel, "Dashboard"));
        scheduleBtn.addActionListener(e -> cl.show(contentPanel, "Schedule"));
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginPage().setVisible(true);
        });
    }

    private JScrollPane createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Mark Attendance for Course: " + courseCode, SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(title, BorderLayout.NORTH);

        JPanel studentPanel = new JPanel();
        studentPanel.setLayout(new BoxLayout(studentPanel, BoxLayout.Y_AXIS));

        List<String> students = getStudentsInCourse(courseCode);
        checkBoxes.clear();

       

        for (String student : students) {
            JCheckBox checkBox = new JCheckBox(student);
            checkBox.setFont(new Font("Arial", Font.PLAIN, 14));
            studentPanel.add(checkBox);
            checkBoxes.add(checkBox);
        }

        JButton submitBtn = new JButton("Submit Attendance");
        submitBtn.setBackground(Color.decode("#2563EB"));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        

        submitBtn.addActionListener(e -> {
            String currentDate = new SimpleDateFormat("yyyy-MM-dd (EEEE)").format(new Date());
            Set<String> selectedStudents = new HashSet<>();
            for (JCheckBox cb : checkBoxes) {
                if (cb.isSelected()) {
                    selectedStudents.add(cb.getText());
                }
            }

            Path path = Paths.get("data/attendance.txt");
            Set<String> existingEntries = new HashSet<>();

            if (Files.exists(path)) {
                try (BufferedReader reader = Files.newBufferedReader(path)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split("\\|");
                        if (parts.length >= 3) {
                            existingEntries.add(parts[0] + "|" + parts[1] + "|" + parts[2]);
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                for (String student : students) {
                    String entryKey = student + "|" + currentDate + "|" + courseCode;
                    if (!existingEntries.contains(entryKey)) {
                        int present = selectedStudents.contains(student) ? 1 : 0;
                        writer.write(entryKey + "|" + present);
                        writer.newLine();
                    }
                }
                JOptionPane.showMessageDialog(this, "Attendance Recorded (excluding duplicates).");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving attendance.");
            }
        });

        JPanel btnPanel = new JPanel(new BorderLayout());

JButton invertBtn = new JButton("Invert");
invertBtn.setPreferredSize(new Dimension(80, 30));
invertBtn.setBackground(Color.LIGHT_GRAY);
invertBtn.setForeground(Color.BLACK);
invertBtn.setFocusPainted(false);

invertBtn.addActionListener(e -> {
    for (JCheckBox cb : checkBoxes) {
        cb.setSelected(!cb.isSelected());
    }
});

JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
rightPanel.add(invertBtn);

btnPanel.add(submitBtn, BorderLayout.CENTER);
btnPanel.add(rightPanel, BorderLayout.EAST);

panel.add(btnPanel, BorderLayout.SOUTH);


        panel.add(new JScrollPane(studentPanel), BorderLayout.CENTER);
        
        return new JScrollPane(panel);
    }

    private JScrollPane createSchedulePanel() {
        TimetablePanel timetablePanel = new TimetablePanel();
        return new JScrollPane(timetablePanel);
    }

    private List<String> getStudentsInCourse(String courseCode) {
        List<String> students = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("data/students.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String[] courses = parts[2].split(";");
                    for (String course : courses) {
                        if (course.trim().equalsIgnoreCase(courseCode)) {
                            students.add(parts[0]);
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return students;
    }

    private String getCourseForProfessor(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader("data/professors.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(username)) {
                    return parts[2];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }
}

