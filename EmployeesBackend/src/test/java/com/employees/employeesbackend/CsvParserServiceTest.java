package com.employees.employeesbackend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvParserServiceTest {

    private CsvParserService csvParserService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        csvParserService = new CsvParserService();
    }

    @Test
    void testParseValidCsvFile() throws Exception {
        File testFile = tempDir.resolve("test.csv").toFile();
        try (PrintWriter writer = new PrintWriter(new FileWriter(testFile))) {
            writer.println("EmpID,ProjectID,DateFrom,DateTo");  // Header
            writer.println("143,12,2013-11-01,2014-01-05");
            writer.println("218,10,2012-05-16,NULL");
            writer.println("143,10,2009-01-01,2011-04-27");
        }

        List<EmployeeProject> records = csvParserService.parseCsv(testFile.getAbsolutePath());

        assertEquals(3, records.size());
        assertEquals(143L, records.get(0).getEmpId());
        assertEquals(12L, records.get(0).getProjectId());
        assertEquals(LocalDate.of(2013, 11, 1), records.get(0).getDateFrom());
        assertEquals(LocalDate.of(2014, 1, 5), records.get(0).getDateTo());
    }

    @Test
    void testParseCsvWithDifferentDateFormats() throws Exception {
        File testFile = tempDir.resolve("mixed_dates.csv").toFile();
        try (PrintWriter writer = new PrintWriter(new FileWriter(testFile))) {
            writer.println("EmpID,ProjectID,DateFrom,DateTo");  // Header
            writer.println("143,12,2013-11-01,2014/01/05");
            writer.println("218,10,31-12-2023,NULL");
            writer.println("143,10,01/15/2023,2024-12-31");
        }

        List<EmployeeProject> records = csvParserService.parseCsv(testFile.getAbsolutePath());

        assertEquals(3, records.size());
        assertNotNull(records.get(0).getDateFrom());
        assertNotNull(records.get(1).getDateFrom());
        assertNotNull(records.get(2).getDateFrom());

        // Verify specific dates
        assertEquals(LocalDate.of(2013, 11, 1), records.get(0).getDateFrom());
        assertEquals(LocalDate.of(2023, 12, 31), records.get(1).getDateFrom());
        assertEquals(LocalDate.of(2023, 1, 15), records.get(2).getDateFrom());
    }

    @Test
    void testParseCsvWithDuplicateRows() throws Exception {
        File testFile = tempDir.resolve("duplicates.csv").toFile();
        try (PrintWriter writer = new PrintWriter(new FileWriter(testFile))) {
            writer.println("EmpID,ProjectID,DateFrom,DateTo");  // Header
            writer.println("143,12,2013-11-01,2014-01-05");
            writer.println("143,12,2013-11-01,2014-01-05"); // duplicate
            writer.println("218,10,2012-05-16,NULL");
        }

        List<EmployeeProject> records = csvParserService.parseCsv(testFile.getAbsolutePath());

        // Should remove duplicate (3 rows -> 2 unique)
        assertEquals(2, records.size());
    }

    @Test
    void testParseCsvWithoutHeader() throws Exception {
        File testFile = tempDir.resolve("no_header.csv").toFile();
        try (PrintWriter writer = new PrintWriter(new FileWriter(testFile))) {
            // No header line, just data
            writer.println("143,12,2013-11-01,2014-01-05");
            writer.println("218,10,2012-05-16,NULL");
        }

        // This will fail because first line of data will be skipped as header
        List<EmployeeProject> records = csvParserService.parseCsv(testFile.getAbsolutePath());

        // The header skipping will eat the first data row, so only 1 record remains
        assertEquals(1, records.size());
        assertEquals(218L, records.get(0).getEmpId()); // Second row becomes first
    }

    @Test
    void testParseCsvWithNullDateTo() throws Exception {
        File testFile = tempDir.resolve("null_dates.csv").toFile();
        try (PrintWriter writer = new PrintWriter(new FileWriter(testFile))) {
            writer.println("EmpID,ProjectID,DateFrom,DateTo");  // Header
            writer.println("143,12,2013-11-01, NULL");
            writer.println("218,10,2012-05-16, NULL");
            writer.println("143,10,2009-01-01, NULL");
        }

        List<EmployeeProject> records = csvParserService.parseCsv(testFile.getAbsolutePath());

        assertEquals(3, records.size());
        // All should have today's date for DateTo
        assertEquals(LocalDate.now(), records.get(0).getDateTo());
        assertEquals(LocalDate.now(), records.get(1).getDateTo());
        assertEquals(LocalDate.now(), records.get(2).getDateTo());
    }

    @Test
    void testParseCsvWithSpaces() throws Exception {
        File testFile = tempDir.resolve("spaces.csv").toFile();
        try (PrintWriter writer = new PrintWriter(new FileWriter(testFile))) {
            writer.println("EmpID,ProjectID,DateFrom,DateTo");  // Header
            writer.println(" 143 , 12 , 2013-11-01 , 2014-01-05 ");
            writer.println("218, 10, 2012-05-16, NULL");
        }

        List<EmployeeProject> records = csvParserService.parseCsv(testFile.getAbsolutePath());

        assertEquals(2, records.size());
        assertEquals(143L, records.get(0).getEmpId());
        assertEquals(12L, records.get(0).getProjectId());
    }

    @Test
    void testParseCsvWithInvalidNumberFormat() throws Exception {
        File testFile = tempDir.resolve("invalid_number.csv").toFile();
        try (PrintWriter writer = new PrintWriter(new FileWriter(testFile))) {
            writer.println("EmpID,ProjectID,DateFrom,DateTo");  // Header
            writer.println("abc,12,2013-11-01,2014-01-05");  // Invalid EmpID
        }

        Exception exception = assertThrows(Exception.class, () ->
                csvParserService.parseCsv(testFile.getAbsolutePath())
        );

        assertTrue(exception.getMessage().contains("Error parsing line 2"));
    }

    @Test
    void testParseCsvWithInvalidDateFormat() throws Exception {
        File testFile = tempDir.resolve("invalid_date.csv").toFile();
        try (PrintWriter writer = new PrintWriter(new FileWriter(testFile))) {
            writer.println("EmpID,ProjectID,DateFrom,DateTo");  // Header
            writer.println("143,12,invalid-date,2014-01-05");
        }

        Exception exception = assertThrows(Exception.class, () ->
                csvParserService.parseCsv(testFile.getAbsolutePath())
        );

        assertTrue(exception.getMessage().contains("Unsupported date format"));
    }

    @Test
    void testParseCsvWithInsufficientColumns() throws Exception {
        File testFile = tempDir.resolve("insufficient.csv").toFile();
        try (PrintWriter writer = new PrintWriter(new FileWriter(testFile))) {
            writer.println("EmpID,ProjectID,DateFrom,DateTo");  // Header
            writer.println("143,12,2013-11-01");  // Only 3 columns
        }

        Exception exception = assertThrows(Exception.class, () ->
                csvParserService.parseCsv(testFile.getAbsolutePath())
        );

        assertTrue(exception.getMessage().contains("insufficient columns"));
    }

    @Test
    void testParseCsvWithEmptyFile() throws Exception {
        File testFile = tempDir.resolve("empty.csv").toFile();
        try (PrintWriter writer = new PrintWriter(new FileWriter(testFile))) {
            writer.println("EmpID,ProjectID,DateFrom,DateTo");  // Header only
        }

        List<EmployeeProject> records = csvParserService.parseCsv(testFile.getAbsolutePath());

        // Header only, no data rows
        assertEquals(0, records.size());
    }

    @Test
    void testParseCsvWithBlankLines() throws Exception {
        File testFile = tempDir.resolve("blank_lines.csv").toFile();
        try (PrintWriter writer = new PrintWriter(new FileWriter(testFile))) {
            writer.println("EmpID,ProjectID,DateFrom,DateTo");  // Header
            writer.println("143,12,2013-11-01,2014-01-05");
            writer.println("218,10,2012-05-16,NULL");
            writer.println("");
        }

        Exception exception = assertThrows(Exception.class, () ->
                csvParserService.parseCsv(testFile.getAbsolutePath())
        );


        // Should fail on blank lines
        assertTrue(exception.getMessage().contains("Line 4 has insufficient columns"));
    }

    @Test
    void testParseCsvWithLargeFile() throws Exception {
        File testFile = tempDir.resolve("large.csv").toFile();
        try (PrintWriter writer = new PrintWriter(new FileWriter(testFile))) {
            writer.println("EmpID,ProjectID,DateFrom,DateTo");  // Header
            // Generate 1000 records
            for (int i = 1; i <= 1000; i++) {
                writer.printf("%d,%d,2023-01-01,2023-12-31%n", i, i % 100 + 1);
            }
        }

        List<EmployeeProject> records = csvParserService.parseCsv(testFile.getAbsolutePath());

        assertEquals(1000, records.size());
    }
}