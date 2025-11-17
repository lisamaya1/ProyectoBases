package com.example.fitware.web.dto;

import java.util.List;

public record ReportSectionDTO(
    String id,
    String category,
    String title,
    String description,
    List<String> headers,
    List<List<String>> rows
) {}
