package com.employees.employeesbackend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmployeePairServiceTest {

    private EmployeePairService employeePairService;

    @BeforeEach
    void setUp() {
        employeePairService = new EmployeePairService();
    }

    @Test
    void testFindLongestWorkingPairWithBasicData() {
        List<EmployeeProject> records = Arrays.asList(
                new EmployeeProject(143L, 12L, LocalDate.of(2013, 11, 1), LocalDate.of(2014, 1, 5)),
                new EmployeeProject(218L, 12L, LocalDate.of(2013, 11, 20), LocalDate.of(2014, 2, 1)),
                new EmployeeProject(143L, 10L, LocalDate.of(2009, 1, 1), LocalDate.of(2011, 4, 27)),
                new EmployeeProject(218L, 10L, LocalDate.of(2012, 5, 16), LocalDate.now())
        );

        EmployeePairResponse response = employeePairService.findLongestWorkingPair(records);

        assertNotNull(response);
        assertTrue(response.getTotalDaysWorked() > 0);
    }

    @Test
    void testEmployeesWorkedTogetherOnSameProject() {
        // Employees 143 and 217 worked together on project 15
        List<EmployeeProject> records = Arrays.asList(
                new EmployeeProject(143L, 15L, LocalDate.of(2010, 6, 1), LocalDate.of(2012, 12, 31)),
                new EmployeeProject(217L, 15L, LocalDate.of(2011, 1, 15), LocalDate.of(2013, 6, 30))
        );

        EmployeePairResponse response = employeePairService.findLongestWorkingPair(records);

        assertEquals(143L, response.getEmpId1());
        assertEquals(217L, response.getEmpId2());
        assertEquals(1, response.getCommonProjects().size());
        assertEquals(15L, response.getCommonProjects().get(0).getProjectId());
        // Expected overlap: 2011-01-15 to 2012-12-31 = 717 days
        assertEquals(717L, response.getCommonProjects().get(0).getDaysWorked());
    }

    @Test
    void testEmployeesWorkedOnMultipleTimePeriodsSameProject() {
        // Same employees, same project, two different time periods
        List<EmployeeProject> records = Arrays.asList(
                new EmployeeProject(143L, 15L, LocalDate.of(2010, 6, 1), LocalDate.of(2012, 12, 31)),
                new EmployeeProject(217L, 15L, LocalDate.of(2011, 1, 15), LocalDate.of(2013, 6, 30)),
                new EmployeeProject(143L, 15L, LocalDate.of(2000, 6, 1), LocalDate.of(2000, 6, 30)),
                new EmployeeProject(217L, 15L, LocalDate.of(2000, 6, 1), LocalDate.of(2000, 6, 30))
        );

        EmployeePairResponse response = employeePairService.findLongestWorkingPair(records);

        assertEquals(1, response.getCommonProjects().size());
        // Should sum both periods: 717 + 30 = 747
        assertEquals(747L, response.getCommonProjects().get(0).getDaysWorked());
        assertEquals(747L, response.getTotalDaysWorked());
    }

    @Test
    void testNoOverlapReturnsProperException() {
        List<EmployeeProject> records = Arrays.asList(
                new EmployeeProject(143L, 15L, LocalDate.of(2010, 1, 1), LocalDate.of(2010, 6, 30)),
                new EmployeeProject(217L, 15L, LocalDate.of(2010, 7, 1), LocalDate.of(2010, 12, 31))
        );

        assertThrows(IllegalArgumentException.class, () ->
                employeePairService.findLongestWorkingPair(records)
        );
    }

    @Test
    void testEmployeesWorkedOnMultipleProjects() {
        List<EmployeeProject> records = Arrays.asList(
                // Project 15
                new EmployeeProject(143L, 15L, LocalDate.of(2010, 6, 1), LocalDate.of(2012, 12, 31)),
                new EmployeeProject(217L, 15L, LocalDate.of(2011, 1, 15), LocalDate.of(2013, 6, 30)),
                // Project 12
                new EmployeeProject(143L, 12L, LocalDate.of(2013, 11, 1), LocalDate.of(2014, 1, 5)),
                new EmployeeProject(217L, 12L, LocalDate.of(2013, 11, 15), LocalDate.of(2014, 1, 10))
        );

        EmployeePairResponse response = employeePairService.findLongestWorkingPair(records);

        assertEquals(2, response.getCommonProjects().size());
        assertTrue(response.getTotalDaysWorked() > 0);
    }

    @Test
    void testGetPairKeyConsistency() {
        // Using reflection to test private method
        // Alternatively, test through public behavior
        List<EmployeeProject> records1 = Arrays.asList(
                new EmployeeProject(143L, 1L, LocalDate.now(), LocalDate.now()),
                new EmployeeProject(217L, 1L, LocalDate.now(), LocalDate.now())
        );

        List<EmployeeProject> records2 = Arrays.asList(
                new EmployeeProject(217L, 1L, LocalDate.now(), LocalDate.now()),
                new EmployeeProject(143L, 1L, LocalDate.now(), LocalDate.now())
        );

        EmployeePairResponse response1 = employeePairService.findLongestWorkingPair(records1);
        EmployeePairResponse response2 = employeePairService.findLongestWorkingPair(records2);

        // Both should identify the same pair regardless of order
        assertEquals(response1.getEmpId1(), response2.getEmpId1());
        assertEquals(response1.getEmpId2(), response2.getEmpId2());
    }

    @Test
    void testEmptyRecordsThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                employeePairService.findLongestWorkingPair(Collections.emptyList())
        );
    }

    @Test
    void testNullRecordsThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                employeePairService.findLongestWorkingPair(null)
        );
    }

    @Test
    void testSingleEmployeeRecordThrowsException() {
        List<EmployeeProject> records = Collections.singletonList(
                new EmployeeProject(143L, 15L, LocalDate.now(), LocalDate.now())
        );

        assertThrows(IllegalArgumentException.class, () ->
                employeePairService.findLongestWorkingPair(records)
        );
    }

    @Test
    void testOverlappingWithMultiplePeriods() {
        List<EmployeeProject> records = Arrays.asList(
                new EmployeeProject(143L, 15L, LocalDate.of(2010, 1, 1), LocalDate.of(2010, 12, 31)),
                new EmployeeProject(143L, 15L, LocalDate.of(2011, 1, 1), LocalDate.of(2011, 12, 31)),
                new EmployeeProject(217L, 15L, LocalDate.of(2010, 6, 1), LocalDate.of(2011, 6, 30))
        );

        EmployeePairResponse response = employeePairService.findLongestWorkingPair(records);

        // Should merge employee 143's periods into one continuous period
        // Overlap with 217: 2010-06-01 to 2011-06-30 = 395 days
        assertEquals(395L, response.getTotalDaysWorked());
    }
}