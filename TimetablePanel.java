package ui;

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class TimetablePanel extends JPanel {

    private JTable timetableTable;

    public TimetablePanel() {
        setLayout(new BorderLayout());

        String[] times = {"09:00", "10:00", "11:00", "12:00"};
        String[] days = {"MON", "TUE", "WED", "THU", "FRI"};

        // Prepare table model with days as rows, times as columns
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Day");
        for (String time : times) {
            model.addColumn(time);
        }

        // Initialize empty timetable
        Map<String, Map<String, String>> timetableData = new HashMap<>();
        for (String day : days) {
            timetableData.put(day, new HashMap<>());
            for (String time : times) {
                timetableData.get(day).put(time, "FREE");
            }
        }

        // Read timetable.txt
        try (BufferedReader br = new BufferedReader(new FileReader("data/timetable.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String day = parts[0].trim();
                    String time = parts[1].trim();
                    String subject = parts[2].trim();
                    String room = parts[3].trim();

                    if (timetableData.containsKey(day) && timetableData.get(day).containsKey(time)) {
                        timetableData.get(day).put(time, subject + " (" + room + ")");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error reading timetable.txt", "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Fill table rows
        for (String day : days) {
            Vector<String> row = new Vector<>();
            row.add(day);
            for (String time : times) {
                row.add(timetableData.get(day).get(time));
            }
            model.addRow(row);
        }

        timetableTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(timetableTable);

        // Styling (optional)
        timetableTable.setRowHeight(40);
        timetableTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        timetableTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        add(scrollPane, BorderLayout.CENTER);
    }
}
