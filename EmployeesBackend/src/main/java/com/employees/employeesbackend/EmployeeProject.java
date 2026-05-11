package com.employees.employeesbackend;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeProject {
    private Long empId;
    private Long projectId;
    private LocalDate dateFrom;
    private LocalDate dateTo;
}
