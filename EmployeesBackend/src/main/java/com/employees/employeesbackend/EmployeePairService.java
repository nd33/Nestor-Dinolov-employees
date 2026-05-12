package com.employees.employeesbackend;

import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmployeePairService {

    /**
     * Finds the pair of employees who worked together the longest across all projects.
     */
    public EmployeePairResponse findLongestWorkingPair(List<EmployeeProject> records) {
        validateRecords(records);

        var pairDayMaps = calculateAllPairDays(records);

        return buildBestPairResponse(pairDayMaps.totalDays, pairDayMaps.projectDays);
    }

    /**
     * Record to hold both maps returned from calculation
     */
    private record PairDayMaps(Map<String, Long> totalDays, Map<String, Long> projectDays) {}

    /**
     * Calculates all pair days across all projects
     */
    private PairDayMaps calculateAllPairDays(List<EmployeeProject> records) {
        Map<String, Long> totalDays = new HashMap<>();
        Map<String, Long> projectDays = new HashMap<>();

        groupByProjectId(records).forEach((projectId, employees) -> {
            for (int i = 0; i < employees.size(); i++) {
                for (int j = i + 1; j < employees.size(); j++) {
                    processPair(projectId, employees.get(i), employees.get(j), totalDays, projectDays);
                }
            }
        });

        return new PairDayMaps(totalDays, projectDays);
    }

    /**
     * Processes a single pair of employees on a project
     */
    private void processPair(Long projectId, EmployeeProject emp1, EmployeeProject emp2,
                             Map<String, Long> totalDays, Map<String, Long> projectDays) {
        long days = calculateOverlappingDays(emp1, emp2);
        if (days > 0) {
            String pairKey = getPairKey(emp1.getEmpId(), emp2.getEmpId());
            // merge() adds the days to existing value or puts if absent
            totalDays.merge(pairKey, days, Long::sum);
            projectDays.merge(pairKey + "-" + projectId, days, Long::sum);
        }
    }

    /**
     * Builds the final response with the best employee pair
     */
    private EmployeePairResponse buildBestPairResponse(Map<String, Long> totalDays, Map<String, Long> projectDays) {
        // Find the pair key with maximum days
        String bestKey = totalDays.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalArgumentException("No overlapping periods found"));

        // Extract employee IDs from the key
        String[] ids = bestKey.split("-");
        Long empId1 = Long.parseLong(ids[0]);
        Long empId2 = Long.parseLong(ids[1]);

        // Get all common projects for this pair
        var projects = projectDays.entrySet().stream()
                .filter(e -> e.getKey().startsWith(bestKey + "-"))
                .map(e -> new EmployeePairResponse.ProjectDetails(
                        extractProjectId(e.getKey()), e.getValue()))
                .sorted((a, b) -> Long.compare(b.getDaysWorked(), a.getDaysWorked()))
                .collect(Collectors.toList());

        // Calculate total days from all projects
        Long totalDaysSum = projects.stream()
                .mapToLong(EmployeePairResponse.ProjectDetails::getDaysWorked)
                .sum();

        return new EmployeePairResponse(empId1, empId2, totalDaysSum, projects);
    }

    /**
     * Validates the input records
     */
    private void validateRecords(List<EmployeeProject> records) {
        if (records == null || records.isEmpty()) {
            throw new IllegalArgumentException("No records to analyze. Please provide valid employee data.");
        }
    }

    /**
     * Groups all employee records by project ID
     */
    private Map<Long, List<EmployeeProject>> groupByProjectId(List<EmployeeProject> records) {
        return records.stream()
                .collect(Collectors.groupingBy(EmployeeProject::getProjectId));
    }

    /**
     * Extracts project ID from a project-pair key (format: "empId1-empId2-projectId")
     */
    private Long extractProjectId(String projectPairKey) {
        String[] parts = projectPairKey.split("-");
        return Long.parseLong(parts[2]);
    }

    /**
     * Calculates how many days two employees worked together on the same project
     */
    private long calculateOverlappingDays(EmployeeProject emp1, EmployeeProject emp2) {
        LocalDate startDate = getLaterDate(emp1.getDateFrom(), emp2.getDateFrom());
        LocalDate endDate = getEarlierDate(emp1.getDateTo(), emp2.getDateTo());

        if (startDate.isAfter(endDate)) {
            return 0;
        }

        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    private LocalDate getLaterDate(LocalDate date1, LocalDate date2) {
        return date1.isAfter(date2) ? date1 : date2;
    }

    private LocalDate getEarlierDate(LocalDate date1, LocalDate date2) {
        return date1.isBefore(date2) ? date1 : date2;
    }

    /**
     * Creates a unique key for an employee pair (sorted to ensure consistency)
     * Example: 143 and 217 -> "143-217"
     */
    private String getPairKey(Long empId1, Long empId2) {
        return empId1 < empId2
                ? empId1 + "-" + empId2
                : empId2 + "-" + empId1;
    }
}