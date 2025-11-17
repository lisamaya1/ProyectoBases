package com.example.fitware.web.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record ReportSummaryDTO(
    LocalDate from,
    LocalDate to,
    Instant generatedAt,
    List<ReportSectionDTO> sections
) {}
