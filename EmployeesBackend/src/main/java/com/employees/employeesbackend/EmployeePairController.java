package com.employees.employeesbackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "http://localhost:5173")
public class EmployeePairController {

    @Autowired
    private CsvParserService csvParserService;

    @Autowired
    private EmployeePairService employeePairService;

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeCsv(@RequestParam("file") MultipartFile file) {
        try {
            var records = csvParserService.parseCsv(file.getOriginalFilename());
            EmployeePairResponse result = employeePairService.findLongestWorkingPair(records);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
