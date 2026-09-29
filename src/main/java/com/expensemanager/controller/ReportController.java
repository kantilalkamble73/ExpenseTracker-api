package com.expensemanager.controller;

import com.expensemanager.security.CustomUserDetails;
import com.expensemanager.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> downloadPdf(@AuthenticationPrincipal CustomUserDetails principal,
                                               @RequestParam Integer month,
                                               @RequestParam Integer year) {
        byte[] pdf = reportService.generateMonthlyPdf(principal.getUser(), month, year);
        String filename = "expense-report-" + year + "-" + String.format("%02d", month) + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(pdf);
    }

    @GetMapping("/csv")
    public ResponseEntity<byte[]> downloadCsv(@AuthenticationPrincipal CustomUserDetails principal,
                                               @RequestParam Integer month,
                                               @RequestParam Integer year) {
        byte[] csv = reportService.generateMonthlyCsv(principal.getUser(), month, year);
        String filename = "expenses-" + year + "-" + String.format("%02d", month) + ".csv";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(csv);
    }
}
