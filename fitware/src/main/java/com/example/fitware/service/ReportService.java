package com.example.fitware.service;

import com.example.fitware.web.dto.ReportSectionDTO;
import com.example.fitware.web.dto.ReportSummaryDTO;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private static final Locale LOCALE_ES = new Locale("es", "ES");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy", LOCALE_ES);
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", LOCALE_ES);
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy", LOCALE_ES);

    private final EntityManager entityManager;

    public ReportService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public ReportSummaryDTO generarResumen(LocalDate desde, LocalDate hasta, List<String> requestedSections) {
        LocalDate start = desde != null ? desde : LocalDate.now().minusMonths(1);
        LocalDate end = hasta != null ? hasta : LocalDate.now();
        if (start.isAfter(end)) {
            LocalDate swap = start;
            start = end;
            end = swap;
        }

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endExclusive = end.plusDays(1).atStartOfDay();

        final LocalDate finalStart = start;
        final LocalDate finalEnd = end;
        final LocalDateTime finalStartDateTime = startDateTime;
        final LocalDateTime finalEndExclusive = endExclusive;

        List<String> normalizedFilter = normalizeSections(requestedSections);
        List<ReportSectionDTO> sections = new ArrayList<>();
        addSection(sections, normalizedFilter, "clientes-registrados", () -> clientesRegistrados(finalStart, finalEnd));
        addSection(sections, normalizedFilter, "sesiones-periodo", () -> sesionesRegistradas(finalStartDateTime, finalEndExclusive));
        addSection(sections, normalizedFilter, "progresos-periodo", () -> progresosRegistrados(finalStart, finalEnd));

        addSection(sections, normalizedFilter, "sesiones-estado", () -> sesionesPorEstado(finalStartDateTime, finalEndExclusive));
        addSection(sections, normalizedFilter, "top-clientes-sesiones", () -> topClientesPorSesiones(finalStartDateTime, finalEndExclusive));
        addSection(sections, normalizedFilter, "rutinas-entrenador", this::rutinasPorEntrenador);
        addSection(sections, normalizedFilter, "peso-promedio-mensual", () -> promedioPesoMensual(finalStart, finalEnd));

        addSection(sections, normalizedFilter, "mes-activo-calorias", () -> mesMasActivoEnCalorias(finalStartDateTime, finalEndExclusive));
        addSection(sections, normalizedFilter, "clientes-sin-progreso", () -> clientesSinProgresoReciente(finalEnd));
        addSection(sections, normalizedFilter, "variacion-peso", () -> variacionPesoClientes(finalStart, finalEnd));

        return new ReportSummaryDTO(start, end, Instant.now(), List.copyOf(sections));
    }

    public byte[] generarPdf(ReportSummaryDTO resumen) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10);

            document.add(new Paragraph("Reporte Fitware", titleFont));
            document.add(new Paragraph(
                "Periodo analizado: " + formatDate(resumen.from()) + " al " + formatDate(resumen.to()),
                bodyFont
            ));
            document.add(new Paragraph(
                "Generado: " + DATE_TIME_FORMAT.format(resumen.generatedAt().atZone(ZoneId.systemDefault())),
                smallFont
            ));
            document.add(new Paragraph(" "));

            for (ReportSectionDTO section : resumen.sections()) {
                document.add(new Paragraph(section.category() + " · " + section.title(), headerFont));
                document.add(new Paragraph(section.description(), smallFont));
                document.add(new Paragraph(" "));

                if (section.rows().isEmpty()) {
                    document.add(new Paragraph("Sin datos para el periodo indicado.", bodyFont));
                } else {
                    PdfPTable table = new PdfPTable(section.headers().size());
                    table.setWidthPercentage(100);
                    for (String header : section.headers()) {
                        PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                        table.addCell(cell);
                    }
                    for (List<String> row : section.rows()) {
                        for (String value : row) {
                            table.addCell(new Phrase(value, bodyFont));
                        }
                    }
                    document.add(table);
                }
                document.add(new Paragraph(" "));
            }

            document.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el PDF de reportes", e);
        }
    }

    private ReportSectionDTO clientesRegistrados(LocalDate desde, LocalDate hasta) {
        String sql = """
            SELECT c.usuario_cedula,
                   COALESCE(u.nombre, '') AS cliente_nombre,
                   COALESCE(u.apellidos, '') AS cliente_apellidos,
                   c.fecha_registro
            FROM cliente c
            JOIN usuario u ON u.cedula = c.usuario_cedula
            WHERE c.fecha_registro BETWEEN :desde AND :hasta
            ORDER BY c.fecha_registro ASC, cliente_nombre ASC
        """;
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("desde", desde);
        query.setParameter("hasta", hasta);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        List<List<String>> tabla = new ArrayList<>();
        for (Object[] row : rows) {
            String cedula = text(row[0]);
            String nombre = nombreCompleto(row[1], row[2], cedula);
            tabla.add(List.of(
                cedula,
                nombre,
                formatDate(toLocalDate(row[3]))
            ));
        }
        return section(
            "clientes-registrados",
            "SIMPLE",
            "Clientes inscritos en el periodo",
            "Altas registradas por fecha de inscripción.",
            List.of("Cédula", "Cliente", "Fecha de registro"),
            tabla
        );
    }

    private ReportSectionDTO sesionesRegistradas(LocalDateTime desde, LocalDateTime hasta) {
        String sql = """
            SELECT s.id,
                   s.estado,
                   s.fecha_inicio,
                   COALESCE(s.gasto_calorico, 0),
                   c.usuario_cedula,
                   COALESCE(u.nombre, '') AS cliente_nombre,
                   COALESCE(u.apellidos, '') AS cliente_apellidos
            FROM sesion s
            JOIN cliente c ON c.usuario_cedula = s.cliente_cedula
            JOIN usuario u ON u.cedula = c.usuario_cedula
            WHERE s.fecha_inicio >= :desde AND s.fecha_inicio < :hasta
            ORDER BY s.fecha_inicio DESC
            LIMIT 25
        """;
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("desde", desde);
        query.setParameter("hasta", hasta);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        List<List<String>> tabla = new ArrayList<>();
        for (Object[] row : rows) {
            String cedula = text(row[4]);
            String cliente = nombreCompleto(row[5], row[6], cedula);
            tabla.add(List.of(
                text(row[0]),
                cliente,
                text(row[1]),
                formatDateTime(toLocalDateTime(row[2])),
                formatNumber(row[3], 0) + " kcal"
            ));
        }
        return section(
            "sesiones-periodo",
            "SIMPLE",
            "Sesiones programadas en el rango",
            "Sesiones registradas (máximo 25 más recientes) para los filtros seleccionados.",
            List.of("Sesión", "Cliente", "Estado", "Inicio", "Calorías"),
            tabla
        );
    }

    private ReportSectionDTO progresosRegistrados(LocalDate desde, LocalDate hasta) {
        String sql = """
            SELECT p.id,
                   c.usuario_cedula,
                   COALESCE(u.nombre, '') AS cliente_nombre,
                   COALESCE(u.apellidos, '') AS cliente_apellidos,
                   p.fecha,
                   p.peso,
                   p.imc
            FROM progreso p
            JOIN cliente c ON c.usuario_cedula = p.cliente_cedula
            JOIN usuario u ON u.cedula = c.usuario_cedula
            WHERE p.fecha BETWEEN :desde AND :hasta
            ORDER BY p.fecha DESC
            LIMIT 25
        """;
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("desde", desde);
        query.setParameter("hasta", hasta);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        List<List<String>> tabla = new ArrayList<>();
        for (Object[] row : rows) {
            String cedula = text(row[1]);
            String cliente = nombreCompleto(row[2], row[3], cedula);
            tabla.add(List.of(
                text(row[0]),
                cliente,
                formatDate(toLocalDate(row[4])),
                formatNumber(row[5], 1) + " kg",
                formatNumber(row[6], 1)
            ));
        }
        return section(
            "progresos-periodo",
            "SIMPLE",
            "Registros de progreso",
            "Listado de métricas corporales capturadas (últimos 25 registros en el rango).",
            List.of("Registro", "Cliente", "Fecha", "Peso", "IMC"),
            tabla
        );
    }

    private ReportSectionDTO sesionesPorEstado(LocalDateTime desde, LocalDateTime hasta) {
        String sql = """
            SELECT COALESCE(UPPER(s.estado), 'SIN_ESTADO') AS estado,
                   COUNT(*) AS total
            FROM sesion s
            WHERE s.fecha_inicio >= :desde AND s.fecha_inicio < :hasta
            GROUP BY estado
            ORDER BY total DESC
        """;
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("desde", desde);
        query.setParameter("hasta", hasta);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        List<List<String>> tabla = new ArrayList<>();
        for (Object[] row : rows) {
            tabla.add(List.of(
                estadoLegible(row[0]),
                formatNumber(row[1], 0)
            ));
        }
        return section(
            "sesiones-estado",
            "INTERMEDIO",
            "Volumen de sesiones por estado",
            "Distribución agrupada por estado operativo para las sesiones del periodo.",
            List.of("Estado", "Total"),
            tabla
        );
    }

    private ReportSectionDTO topClientesPorSesiones(LocalDateTime desde, LocalDateTime hasta) {
        String sql = """
            SELECT c.usuario_cedula,
                   COALESCE(u.nombre, '') AS cliente_nombre,
                   COALESCE(u.apellidos, '') AS cliente_apellidos,
                   COUNT(*) AS total,
                   COALESCE(AVG(s.gasto_calorico), 0) AS promedio_calorias
            FROM sesion s
            JOIN cliente c ON c.usuario_cedula = s.cliente_cedula
            JOIN usuario u ON u.cedula = c.usuario_cedula
            WHERE s.fecha_inicio >= :desde AND s.fecha_inicio < :hasta
              AND UPPER(COALESCE(s.estado, '')) = 'COMPLETADA'
            GROUP BY c.usuario_cedula,
                     COALESCE(u.nombre, ''),
                     COALESCE(u.apellidos, '')
            ORDER BY total DESC
            LIMIT 5
        """;
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("desde", desde);
        query.setParameter("hasta", hasta);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        List<List<String>> tabla = new ArrayList<>();
        for (Object[] row : rows) {
            String cedula = text(row[0]);
            String cliente = nombreCompleto(row[1], row[2], cedula);
            tabla.add(List.of(
                cliente,
                formatNumber(row[3], 0),
                formatNumber(row[4], 0) + " kcal"
            ));
        }
        return section(
            "top-clientes-sesiones",
            "INTERMEDIO",
            "Top clientes por sesiones completadas",
            "Ranking (hasta 5 registros) considerando calorías promedio quemadas.",
            List.of("Cliente", "Sesiones completadas", "Calorías promedio"),
            tabla
        );
    }

    private ReportSectionDTO rutinasPorEntrenador() {
        String sql = """
            SELECT r.entrenador_cedula,
                   COALESCE(u.nombre, '') AS entrenador_nombre,
                   COALESCE(u.apellidos, '') AS entrenador_apellidos,
                   COUNT(*) AS total_rutinas,
                   COALESCE(AVG(r.duracion_semanas), 0) AS promedio_duracion
            FROM rutina r
            LEFT JOIN usuario u ON u.cedula = r.entrenador_cedula
            GROUP BY r.entrenador_cedula, entrenador_nombre, entrenador_apellidos
            ORDER BY total_rutinas DESC
        """;
        Query query = entityManager.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        List<List<String>> tabla = new ArrayList<>();
        for (Object[] row : rows) {
            String cedula = text(row[0]);
            String entrenador = nombreCompleto(row[1], row[2], cedula);
            tabla.add(List.of(
                entrenador,
                formatNumber(row[3], 0),
                formatNumber(row[4], 0) + " semanas"
            ));
        }
        return section(
            "rutinas-entrenador",
            "INTERMEDIO",
            "Producción de rutinas por entrenador",
            "Cantidad de rutinas diseñadas y duración media planificada.",
            List.of("Entrenador", "Rutinas creadas", "Duración promedio"),
            tabla
        );
    }

    private ReportSectionDTO promedioPesoMensual(LocalDate desde, LocalDate hasta) {
        String sql = """
            SELECT DATE_TRUNC('month', p.fecha) AS mes,
                   COUNT(*) AS registros,
                   AVG(p.peso) AS promedio_peso
            FROM progreso p
            WHERE p.fecha BETWEEN :desde AND :hasta
            GROUP BY mes
            ORDER BY mes
        """;
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("desde", desde);
        query.setParameter("hasta", hasta);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        List<List<String>> tabla = new ArrayList<>();
        for (Object[] row : rows) {
            tabla.add(List.of(
                formatMonth(row[0]),
                formatNumber(row[1], 0),
                formatNumber(row[2], 1) + " kg"
            ));
        }
        return section(
            "peso-promedio-mensual",
            "INTERMEDIO",
            "Evolución de peso promedio mensual",
            "Agrupado por mes para analizar tendencias en los registros de progreso.",
            List.of("Mes", "Registros", "Peso promedio"),
            tabla
        );
    }

    private ReportSectionDTO mesMasActivoEnCalorias(LocalDateTime desde, LocalDateTime hasta) {
        String sql = """
            WITH sesiones_filtradas AS (
                SELECT DATE_TRUNC('month', s.fecha_inicio) AS mes,
                       COUNT(*) AS total_sesiones,
                       AVG(COALESCE(s.gasto_calorico, 0)) AS promedio_calorias
                FROM sesion s
                WHERE s.fecha_inicio >= :desde AND s.fecha_inicio < :hasta
                  AND UPPER(COALESCE(s.estado, '')) = 'COMPLETADA'
                GROUP BY mes
            ),
            maximo AS (
                SELECT mes, total_sesiones
                FROM sesiones_filtradas
                ORDER BY total_sesiones DESC
                LIMIT 1
            )
            SELECT sesiones_filtradas.mes,
                   sesiones_filtradas.total_sesiones,
                   sesiones_filtradas.promedio_calorias
            FROM sesiones_filtradas
            JOIN maximo ON maximo.mes = sesiones_filtradas.mes
        """;
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("desde", desde);
        query.setParameter("hasta", hasta);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        List<List<String>> tabla = new ArrayList<>();
        for (Object[] row : rows) {
            tabla.add(List.of(
                formatMonth(row[0]),
                formatNumber(row[1], 0),
                formatNumber(row[2], 0) + " kcal"
            ));
        }
        return section(
            "mes-activo-calorias",
            "AVANZADO",
            "Mes con más sesiones completadas",
            "Calcula el mes más activo y el promedio de calorías quemadas en ese periodo.",
            List.of("Mes", "Sesiones completadas", "Calorías promedio"),
            tabla
        );
    }

    private ReportSectionDTO clientesSinProgresoReciente(LocalDate hasta) {
        LocalDate umbral = hasta.minusDays(45);
        String sql = """
            SELECT c.usuario_cedula,
                   COALESCE(u.nombre, '') AS cliente_nombre,
                   COALESCE(u.apellidos, '') AS cliente_apellidos,
                   MAX(p.fecha) AS ultima_fecha,
                   COUNT(p.id) AS registros
            FROM cliente c
            JOIN usuario u ON u.cedula = c.usuario_cedula
            LEFT JOIN progreso p ON p.cliente_cedula = c.usuario_cedula
            GROUP BY c.usuario_cedula,
                     COALESCE(u.nombre, ''),
                     COALESCE(u.apellidos, '')
            HAVING MAX(p.fecha) IS NULL OR MAX(p.fecha) < :umbral
            ORDER BY ultima_fecha NULLS FIRST
            LIMIT 10
        """;
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("umbral", umbral);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        List<List<String>> tabla = new ArrayList<>();
        for (Object[] row : rows) {
            String cedula = text(row[0]);
            String cliente = nombreCompleto(row[1], row[2], cedula);
            tabla.add(List.of(
                cliente,
                formatDate(toLocalDate(row[3])),
                formatNumber(row[4], 0)
            ));
        }
        return section(
            "clientes-sin-progreso",
            "AVANZADO",
            "Clientes sin seguimiento en 45 días",
            "Detecta clientes que no registran progreso desde hace 45 días o nunca lo hicieron.",
            List.of("Cliente", "Último registro", "Total histórico"),
            tabla
        );
    }

    private ReportSectionDTO variacionPesoClientes(LocalDate desde, LocalDate hasta) {
        String sql = """
            SELECT c.usuario_cedula,
                   COALESCE(u.nombre, '') AS cliente_nombre,
                   COALESCE(u.apellidos, '') AS cliente_apellidos,
                   MIN(p.peso) AS peso_min,
                   MAX(p.peso) AS peso_max,
                   (MAX(p.peso) - MIN(p.peso)) AS variacion
            FROM progreso p
            JOIN cliente c ON c.usuario_cedula = p.cliente_cedula
            JOIN usuario u ON u.cedula = c.usuario_cedula
            WHERE p.fecha BETWEEN :desde AND :hasta
              AND p.peso IS NOT NULL
            GROUP BY c.usuario_cedula,
                     COALESCE(u.nombre, ''),
                     COALESCE(u.apellidos, '')
            HAVING COUNT(*) >= 2
            ORDER BY variacion DESC
            LIMIT 5
        """;
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("desde", desde);
        query.setParameter("hasta", hasta);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        List<List<String>> tabla = new ArrayList<>();
        for (Object[] row : rows) {
            String cedula = text(row[0]);
            String cliente = nombreCompleto(row[1], row[2], cedula);
            tabla.add(List.of(
                cliente,
                formatNumber(row[3], 1) + " kg",
                formatNumber(row[4], 1) + " kg",
                formatNumber(row[5], 1) + " kg"
            ));
        }
        return section(
            "variacion-peso",
            "AVANZADO",
            "Clientes con mayor variación de peso",
            "Analiza la diferencia entre el peso mínimo y máximo dentro del rango.",
            List.of("Cliente", "Peso mínimo", "Peso máximo", "Variación"),
            tabla
        );
    }

    private ReportSectionDTO section(String id,
                                     String category,
                                     String title,
                                     String description,
                                     List<String> headers,
                                     List<List<String>> rows) {
        return new ReportSectionDTO(
            id,
            category,
            title,
            description,
            List.copyOf(headers),
            List.copyOf(rows)
        );
    }

    private List<String> normalizeSections(List<String> requestedSections) {
        if (requestedSections == null || requestedSections.isEmpty()) {
            return List.of();
        }
        List<String> normalized = new ArrayList<>();
        for (String raw : requestedSections) {
            if (raw == null) {
                continue;
            }
            String value = raw.trim().toLowerCase(Locale.ROOT);
            if (!value.isBlank() && !normalized.contains(value)) {
                normalized.add(value);
            }
        }
        return normalized;
    }

    private void addSection(List<ReportSectionDTO> collector,
                            List<String> requested,
                            String id,
                            java.util.function.Supplier<ReportSectionDTO> supplier) {
        if (requested.isEmpty() || requested.contains(id)) {
            collector.add(supplier.get());
        }
    }

    private String nombreCompleto(Object nombre, Object apellidos, String cedula) {
        String n = raw(nombre);
        String a = raw(apellidos);
        String base = (n + " " + a).trim();
        if (base.isBlank()) {
            return cedula != null && !cedula.isBlank() ? cedula : "Sin nombre";
        }
        return cedula != null && !cedula.isBlank()
            ? base + " (" + cedula + ")"
            : base;
    }

    private String raw(Object value) {
        if (value == null) {
            return "";
        }
        return Objects.toString(value, "").trim();
    }

    private String text(Object value) {
        String raw = raw(value);
        return raw.isBlank() ? "-" : raw;
    }

    private String estadoLegible(Object estado) {
        String raw = text(estado);
        return raw.replace('_', ' ').toUpperCase(LOCALE_ES);
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return "-";
        }
        return DATE_FORMAT.format(date);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "-";
        }
        return DATE_TIME_FORMAT.format(dateTime);
    }

    private String formatMonth(Object value) {
        LocalDateTime dateTime = toLocalDateTime(value);
        if (dateTime == null) {
            return "-";
        }
        return MONTH_FORMAT.format(dateTime);
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate ld) {
            return ld;
        }
        if (value instanceof LocalDateTime ldt) {
            return ldt.toLocalDate();
        }
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate();
        }
        try {
            return LocalDate.parse(value.toString());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime ldt) {
            return ldt;
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        if (value instanceof LocalDate ld) {
            return ld.atStartOfDay();
        }
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate().atStartOfDay();
        }
        if (value instanceof String strValue) {
            String trimmed = strValue.trim();
            if (trimmed.isEmpty()) {
                return null;
            }
            try {
                return LocalDateTime.parse(trimmed);
            } catch (DateTimeParseException ignored) {
                String normalized = trimmed.replace(' ', 'T');
                try {
                    return LocalDateTime.parse(normalized);
                } catch (DateTimeParseException ignored2) {
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSSSSS]");
                        return LocalDateTime.parse(trimmed, formatter);
                    } catch (DateTimeParseException ignored3) {
                        try {
                            return LocalDate.parse(trimmed).atStartOfDay();
                        } catch (DateTimeParseException ignored4) {
                            return null;
                        }
                    }
                }
            }
        }
        try {
            return LocalDateTime.parse(value.toString());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private String formatNumber(Object number, int decimals) {
        if (!(number instanceof Number num)) {
            return text(number);
        }
        String pattern = "%,." + decimals + "f";
        return String.format(LOCALE_ES, pattern, num.doubleValue());
    }
}
