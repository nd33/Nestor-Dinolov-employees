package com.employees.employeesbackend;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class EmployeePairResponse {
    private Long empId1;
    private Long empId2;
    private Long totalDaysWorked;
    private List<ProjectDetails> commonProjects;

    @Data
    @AllArgsConstructor
    public static class ProjectDetails {
        private Long projectId;
        private Long daysWorked;
    }
}
