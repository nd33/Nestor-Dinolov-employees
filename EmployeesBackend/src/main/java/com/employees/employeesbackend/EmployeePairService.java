package com.employees.employeesbackend;

import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class EmployeePairService {

    /**
     * Finds the pair of employees who worked together the longest across all projects
     */
    public EmployeePairResponse findLongestWorkingPair(List<EmployeeProject> records) {
        if (records == null || records.isEmpty()) {
            throw new IllegalArgumentException("No records to analyze. Please provide valid employee data.");
        }

        // Step 1: Group records by project ID
        Map<Long, List<EmployeeProject>> projectsMap = groupByProjectId(records);

        // Step 2: For each project, find all employee pairs and their overlapping days
        Map<String, Long> pairTotalDays = new HashMap<>();
        Map<String, List<EmployeePairResponse.ProjectDetails>> pairProjectsMap = new HashMap<>();

        for (Map.Entry<Long, List<EmployeeProject>> entry : projectsMap.entrySet()) {
            Long projectId = entry.getKey();
            List<EmployeeProject> projectEmployees = entry.getValue();

            // Compare each pair of employees in this project
            for (int i = 0; i < projectEmployees.size(); i++) {
                for (int j = i + 1; j < projectEmployees.size(); j++) {
                    EmployeeProject emp1 = projectEmployees.get(i);
                    EmployeeProject emp2 = projectEmployees.get(j);

                    // Calculate overlapping days for this pair on this project
                    long overlappingDays = calculateOverlappingDays(emp1, emp2);

                    if (overlappingDays > 0) {
                        String pairKey = getPairKey(emp1.getEmpId(), emp2.getEmpId());

                        // Add to total days
                        pairTotalDays.put(pairKey, pairTotalDays.getOrDefault(pairKey, 0L) + overlappingDays);

                        // Add to project details
                        pairProjectsMap.computeIfAbsent(pairKey, k -> new ArrayList<>())
                                .add(new EmployeePairResponse.ProjectDetails(projectId, overlappingDays));
                    }
                }
            }
        }

        // Step 3: Find the pair with maximum total days
        if (pairTotalDays.isEmpty()) {
            throw new IllegalArgumentException("No overlapping work periods found between any employees.");
        }

        String bestPairKey = null;
        Long maxDays = 0L;

        for (Map.Entry<String, Long> entry : pairTotalDays.entrySet()) {
            if (entry.getValue() > maxDays) {
                maxDays = entry.getValue();
                bestPairKey = entry.getKey();
            }
        }

        // Step 4: Extract employee IDs from the key and prepare response
        String[] ids = bestPairKey.split("-");
        Long empId1 = Long.parseLong(ids[0]);
        Long empId2 = Long.parseLong(ids[1]);

        List<EmployeePairResponse.ProjectDetails> commonProjects = pairProjectsMap.get(bestPairKey);

        // Sort projects by days worked (descending) for better display
        commonProjects.sort((a, b) -> Long.compare(b.getDaysWorked(), a.getDaysWorked()));

        return new EmployeePairResponse(empId1, empId2, maxDays, commonProjects);
    }

    /**
     * Groups all employee records by project ID
     */
    private Map<Long, List<EmployeeProject>> groupByProjectId(List<EmployeeProject> records) {
        Map<Long, List<EmployeeProject>> projectsMap = new HashMap<>();

        for (EmployeeProject record : records) {
            projectsMap.computeIfAbsent(record.getProjectId(), k -> new ArrayList<>()).add(record);
        }

        return projectsMap;
    }

    /**
     * Calculates how many days two employees worked together on the same project
     */
    private long calculateOverlappingDays(EmployeeProject emp1, EmployeeProject emp2) {
        // Find the later start date
        LocalDate startDate = emp1.getDateFrom().isAfter(emp2.getDateFrom())
                ? emp1.getDateFrom()
                : emp2.getDateFrom();

        // Find the earlier end date
        LocalDate endDate = emp1.getDateTo().isBefore(emp2.getDateTo())
                ? emp1.getDateTo()
                : emp2.getDateTo();

        // If start date is after end date, no overlap
        if (startDate.isAfter(endDate)) {
            return 0;
        }

        // Calculate days between (inclusive)
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * Creates a unique key for an employee pair (sorted to ensure consistency)
     */
    private String getPairKey(Long empId1, Long empId2) {
        return empId1 < empId2
                ? empId1 + "-" + empId2
                : empId2 + "-" + empId1;
    }
}
