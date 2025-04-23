package ui;

import java.awt.*;
import java.io.*;
import java.time.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class StudentDashboard extends JFrame {
    private String username;
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JLabel enrolledLabel, todayClassesLabel, nextClassLabel;
    private JTable attendanceTable;

    public StudentDashboard(String username) {
        this.username = username;
        setTitle("AttendEase - Student Dashboard");
        setSize(1000, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        initSidebar();
        initHeader();
        initContent();

        setVisible(true);
    }

    private void initHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel profile = new JLabel("Profile: " + username + " | Password: ******");
        profile.setHorizontalAlignment(SwingConstants.RIGHT);
        header.add(profile, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    private void initSidebar() {
        JPanel sidebar = new JPanel();
        
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.decode("#2563EB"));
        sidebar.setPreferredSize(new Dimension(180, getHeight()));

        Font buttonFont = new Font("SansSerif", Font.BOLD, 14);

        JButton dashboardBtn = new JButton("Dashboard");
        JButton attendanceBtn = new JButton("Attendance");
        JButton scheduleBtn = new JButton("Schedule");
        JButton logoutBtn = new JButton("Logout");

        JButton[] buttons = {dashboardBtn, attendanceBtn, scheduleBtn};

        for (JButton btn : buttons) {
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(160, 40));
            btn.setBackground(Color.WHITE);
            btn.setFont(buttonFont);
            sidebar.add(Box.createVerticalStrut(20));
            sidebar.add(btn);
        }

        dashboardBtn.addActionListener(e -> {
            loadEnrolledCourses();
            loadTodayAndNextClasses();
            cardLayout.show(contentPanel, "Dashboard");
        });

        attendanceBtn.addActionListener(e -> {
            loadAttendance();
            cardLayout.show(contentPanel, "Attendance");
        });

        scheduleBtn.addActionListener(e -> {
            cardLayout.show(contentPanel, "Schedule");
        });

        logoutBtn.setMaximumSize(new Dimension(160, 40));
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.setBackground(new Color(220, 53, 69));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFont(buttonFont);
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginPage();
        });

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(logoutBtn);

        add(sidebar, BorderLayout.WEST);
    }

    private void initContent() {
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);

        contentPanel.add(createDashboardPanel(), "Dashboard");
        contentPanel.add(createAttendancePanel(), "Attendance");
        contentPanel.add(new TimetablePanel(), "Schedule");

        add(contentPanel, BorderLayout.CENTER);
        cardLayout.show(contentPanel, "Dashboard");
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        enrolledLabel = new JLabel("Enrolled Courses: ");
        todayClassesLabel = new JLabel("Classes Today: ");
        nextClassLabel = new JLabel("Next Class: ");

        Font font = new Font("SansSerif", Font.BOLD, 18);
        enrolledLabel.setFont(font);
        todayClassesLabel.setFont(font);
        nextClassLabel.setFont(font);

        panel.add(enrolledLabel);
        panel.add(todayClassesLabel);
        panel.add(nextClassLabel);

        loadEnrolledCourses();
        loadTodayAndNextClasses();

        return panel;
    }

    private JPanel createAttendancePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] columns = {"Course", "Total Classes", "Attended", "Missed",  "Percentage (%)"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        List<String> enrolledCourses = getEnrolledCourseCodes();

        for (String course : enrolledCourses) {
            int total = getTotalClassesFromFile(course);
            int attended = getAttendedClasses(course);
            int missed = getMissedClasses(course);
            int percentage = total == 0 ? 0 : (int) ((attended / (double) total) * 100);
            model.addRow(new Object[]{getCourseName(course), total, attended, percentage + "%"});
        }

        attendanceTable = new JTable(model);
        attendanceTable.setDefaultRenderer(Object.class, new AttendanceCellRenderer());
        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton calculatorBtn = new JButton("Open Calculator");
        calculatorBtn.addActionListener(e -> new CalculatorWindow());
        panel.add(calculatorBtn, BorderLayout.SOUTH);

        JButton pieChartBtn = new JButton("View Pie Chart");
        pieChartBtn.addActionListener(e -> new MultiPieChartWindow());
        panel.add(pieChartBtn, BorderLayout.NORTH);

        return panel;
    }

class AttendanceCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {

        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        String percentStr = table.getValueAt(row, 4).toString(); // "Percentage (%)" column
        int percent = Integer.parseInt(percentStr.replace("%", ""));

        if (percent < 75) {
            c.setBackground(new Color(255, 204, 204)); // Light red
        } else {
            c.setBackground(Color.WHITE);
        }

        return c;
    }
}

    private void loadEnrolledCourses() {
        try (BufferedReader reader = new BufferedReader(new FileReader("data/students.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(username)) {
                    if (parts.length >= 3) {
                        String[] courseCodes = parts[2].split(";");
                        List<String> courseNames = new ArrayList<>();
                        for (String code : courseCodes) {
                            courseNames.add(getCourseName(code));
                        }
                        enrolledLabel.setText("Enrolled Courses: " + String.join(", ", courseNames));
                        return;
                    }
                }
            }
        } catch (IOException e) {
            enrolledLabel.setText("Enrolled Courses: [Error loading]");
        }
    }
    
    private void loadAttendance() {
        DefaultTableModel model = (DefaultTableModel) attendanceTable.getModel();
        model.setRowCount(0); // Clear previous rows
    
        List<String> enrolledCourses = getEnrolledCourseCodes();
    
        for (String course : enrolledCourses) {
            int total = getTotalClassesFromFile(course);
            int attended = getAttendedClasses(course);
            int missed = getMissedClasses(course);
            int percentage = total == 0 ? 0 : (int) ((attended / (double) total) * 100);
            model.addRow(new Object[]{getCourseName(course), total, attended, missed,  percentage + "%"});
        }
    }

    private int getTotalClassesFromFile(String courseCode) {
        try (BufferedReader reader = new BufferedReader(new FileReader("data/class_totals.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2 && parts[0].equals(courseCode)) {
                    return Integer.parseInt(parts[1]);
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private String getCourseName(String code) {
        switch (code) {
            case "A": return "OOP";
            case "B": return "Physics";
            case "C": return "Elec";
            case "D": return "DSML";
            case "E": return "Math";
            case "F": return "Ecology";
            default: return code;
        }
    }

    private boolean isTodayHoliday() {
        LocalDate today = LocalDate.now();
        String todayStr = today.toString();
        String day = today.getDayOfWeek().name();

        if (day.equals("SATURDAY") || day.equals("SUNDAY")) {
            try (BufferedReader reader = new BufferedReader(new FileReader("data/working_days.txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith(todayStr + ",")) {
                        return false;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader("data/holidays.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(todayStr)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private String getOverrideDay() {
        String today = LocalDate.now().toString();
        try (BufferedReader reader = new BufferedReader(new FileReader("data/working_days.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(today + ",")) {
                    return line.split(",")[1];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void loadTodayAndNextClasses() {
        
        if (isTodayHoliday()) {
            todayClassesLabel.setText("Classes Today: No classes today 🎉");
            nextClassLabel.setText("Next Class: No classes today 🎉");
            return;
        }
    
        String day = mapToShortDay(LocalDate.now().getDayOfWeek().name());

        String override = getOverrideDay();
        if (override != null) day = override;
    
        LocalTime now = LocalTime.now();
        List<String> todayClasses = new ArrayList<>();
        TreeMap<LocalTime, String> classMap = new TreeMap<>();
    
        try (BufferedReader reader = new BufferedReader(new FileReader("data/timetable.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equalsIgnoreCase(day)) {
                    LocalTime classTime = LocalTime.parse(parts[1]);
                    String course = parts[2];
                    todayClasses.add(course + " at " + parts[1]);
                    classMap.put(classTime, course);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        // Determine next class (first one after or equal to now)
        String nextClass = "No more classes today 🎉";
        for (Map.Entry<LocalTime, String> entry : classMap.entrySet()) {
            if (!entry.getKey().isBefore(now)) {
                nextClass = entry.getValue() + " at " + entry.getKey().toString();
                break;
            }
        }
    
        todayClassesLabel.setText("Classes Today: " + (todayClasses.isEmpty() ? "None 🎉" : String.join(" | ", todayClasses)));
        nextClassLabel.setText("Next Class: " + nextClass);
    }

    private String mapToShortDay(String day) {
        switch (day.toLowerCase()) {
            case "monday": return "MON";
            case "tuesday": return "TUE";
            case "wednesday": return "WED";
            case "thursday": return "THU";
            case "friday": return "FRI";
            case "saturday": return "SAT";
            case "sunday": return "SUN";
            default: return day;
        }
    }
    
    private List<String> getEnrolledCourseCodes() {
        List<String> courses = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("data/students.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(username) && parts.length >= 3) {
                    courses.addAll(Arrays.asList(parts[2].split(";")));
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return courses;
    }

    private int getAttendedClasses(String courseCode) {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader("data/attendance.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 4 && parts[0].equals(username) && parts[2].equals(courseCode) && parts[3].equals("1")) {
                    count++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }

    private int getMissedClasses(String courseCode) {
        int missed = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("data/attendance.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 4 && parts[0].equals(username) && parts[2].equals(courseCode) && parts[3].equals("0")) {
                    missed++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return missed;
    }

    private int getTotalClasses(String courseCode) {
        int count = 0;
        Set<String> holidays = new HashSet<>();
        Set<String> workingOverrides = new HashSet<>();
        try (BufferedReader hReader = new BufferedReader(new FileReader("data/holidays.txt"))) {
            String line;
            while ((line = hReader.readLine()) != null) {
                holidays.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedReader wReader = new BufferedReader(new FileReader("data/working_days.txt"))) {
            String line;
            while ((line = wReader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    workingOverrides.add(parts[0].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        LocalDate startDate = LocalDate.of(2024, 1, 1); // Adjust semester start
        LocalDate today = LocalDate.now();

        try (BufferedReader reader = new BufferedReader(new FileReader("data/timetable.txt"))) {
            Map<String, List<String>> courseSchedule = new HashMap<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3 && parts[2].equals(courseCode)) {
                    courseSchedule.computeIfAbsent(parts[0].toUpperCase(), k -> new ArrayList<>()).add(parts[1]);
                }
            }

            for (LocalDate date = startDate; date.isBefore(today) || date.isEqual(today); date = date.plusDays(1)) {
                String day = date.getDayOfWeek().toString();
                if (holidays.contains(date.toString())) continue;
                if ((day.equals("SATURDAY") || day.equals("SUNDAY")) && !workingOverrides.contains(date.toString()))
                    continue;
                if (courseSchedule.containsKey(day)) {
                    count += courseSchedule.get(day).size();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }

    class MultiPieChartWindow extends JDialog {
    public MultiPieChartWindow() {
        setTitle("Attendance Pie Charts by Course");
        setSize(600, 600);
        setLocationRelativeTo(null);
        setModal(true);

        JPanel chartPanel = new JPanel();
        chartPanel.setLayout(new GridLayout(0, 2, 20, 20)); // 2 columns, dynamic rows

        List<String> courseCodes = getEnrolledCourseCodes();
        for (String code : courseCodes) {
            String courseName = getCourseName(code);
            int attended = getAttendedClasses(code);
            int missed = getMissedClasses(code); // You must have this method

            chartPanel.add(new SinglePieChartPanel(courseName, attended, missed));
        }

        JScrollPane scrollPane = new JScrollPane(chartPanel);
        add(scrollPane);
        setVisible(true);
    }

    class SinglePieChartPanel extends JPanel {
        String courseName;
        int attended, missed;

        public SinglePieChartPanel(String courseName, int attended, int missed) {
            this.courseName = courseName;
            this.attended = attended;
            this.missed = missed;
            setPreferredSize(new Dimension(250, 250));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int total = attended + missed;
            if (total == 0) return;

            int attendedAngle = (int) Math.round((attended / (double) total) * 360);
            int missedAngle = 360 - attendedAngle;

            // Draw Pie
            g2.setColor(Color.GREEN);
            g2.fillArc(20, 40, 150, 150, 0, attendedAngle);
            g2.setColor(Color.RED);
            g2.fillArc(20, 40, 150, 150, attendedAngle, missedAngle);

            // Draw legend
            g2.setColor(Color.BLACK);
            g2.drawString(courseName, 20, 20);
            g2.setColor(Color.GREEN);
            g2.fillRect(180, 60, 10, 10);
            g2.setColor(Color.BLACK);
            g2.drawString("Attended: " + attended, 200, 70);
            g2.setColor(Color.RED);
            g2.fillRect(180, 90, 10, 10);
            g2.setColor(Color.BLACK);
            g2.drawString("Missed: " + missed, 200, 100);
        }
    }
}


    class CalculatorWindow extends JDialog {
        public CalculatorWindow() {
            setTitle("Attendance Calculator");
            setSize(300, 200);
            setLayout(new GridLayout(4, 2));

            JLabel totalLabel = new JLabel("Total Classes:");
            JTextField totalField = new JTextField();

            JLabel attendedLabel = new JLabel("Attended:");
            JTextField attendedField = new JTextField();

            JButton calcBtn = new JButton("Calculate");
            JLabel resultLabel = new JLabel("Percentage: ");

            calcBtn.addActionListener(e -> {
                try {
                    int total = Integer.parseInt(totalField.getText());
                    int attended = Integer.parseInt(attendedField.getText());
                    int percent = (int) ((attended / (double) total) * 100);
                    resultLabel.setText("Percentage: " + percent + "%");
                } catch (NumberFormatException ex) {
                    resultLabel.setText("Invalid input!");
                }
            });

            add(totalLabel); add(totalField);
            add(attendedLabel); add(attendedField);
            add(new JLabel()); add(calcBtn);
            add(new JLabel()); add(resultLabel);

            setVisible(true);
        }
    }

    public static void main(String[] args) {
        new StudentDashboard("Yashwin");
    }
}
