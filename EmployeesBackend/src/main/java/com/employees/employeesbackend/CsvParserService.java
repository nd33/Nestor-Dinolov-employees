package com.employees.employeesbackend;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CsvParserService {

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("MM-dd-yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("MM.dd.yyyy")
    );

    public List<EmployeeProject> parseCsv(String fileName) throws Exception {
        List<EmployeeProject> records = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            int lineNumber = 0;

            // Skip header line
            br.readLine();
            lineNumber++;

            while ((line = br.readLine()) != null) {
                lineNumber++;
                String[] employmentDetails = line.split(",");

                if (employmentDetails.length < 4) {
                    throw new IllegalArgumentException("Line " + lineNumber + " has insufficient columns");
                }

                try {
                    Long empId = Long.parseLong(employmentDetails[0].trim());
                    Long projectId = Long.parseLong(employmentDetails[1].trim());
                    LocalDate dateFrom = parseDate(employmentDetails[2].trim());
                    LocalDate dateTo = parseDateOrToday(employmentDetails[3].trim());

                    records.add(new EmployeeProject(empId, projectId, dateFrom, dateTo));

                } catch (Exception e) {
                    throw new IllegalArgumentException("Error parsing line " + lineNumber + ": " + e.getMessage());
                }
            }
        }

        return records.stream().distinct().toList();
    }

    private LocalDate parseDate(String dateStr) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException ignored) {
                //try all date formatters in a loop, do not fail on first one
            }
        }
        throw new IllegalArgumentException("Unsupported date format: " + dateStr);
    }

    private LocalDate parseDateOrToday(String dateStr) {
        String trimmed = dateStr.trim();
        if (trimmed.equalsIgnoreCase("NULL")) {
            return LocalDate.now();
        }
        return parseDate(trimmed);
    }
}
