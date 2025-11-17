package com.example.fitware.web;

import com.example.fitware.service.ReportService;
import com.example.fitware.web.dto.ReportSummaryDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReportController {

    private static final DateTimeFormatter FILE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ReportSummaryDTO obtenerResumen(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
        @RequestParam(required = false) List<String> sections
    ) {
        return reportService.generarResumen(desde, hasta, sections);
    }

    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> descargarPdf(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
        @RequestParam(required = false) List<String> sections
    ) {
        ReportSummaryDTO resumen = reportService.generarResumen(desde, hasta, sections);
        byte[] pdf = reportService.generarPdf(resumen);
        String fileName = String.format(
            "reportes-fitware-%s-a-%s.pdf",
            FILE_FORMAT.format(resumen.from()),
            FILE_FORMAT.format(resumen.to())
        );
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }
}
