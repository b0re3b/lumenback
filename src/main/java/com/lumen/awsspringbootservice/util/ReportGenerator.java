package com.lumen.awsspringbootservice.util;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.lumen.awsspringbootservice.dto.report.ReportDataDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportGenerator {

    public static Map<String, byte[]> generateReports(ReportDataDto data, List<String> formats) {
        Map<String, byte[]> result = new HashMap<>();

        try {
            if (formats.contains("csv")) result.put("weekly-report.csv", generateCsv(data));
            if (formats.contains("pdf")) result.put("weekly-report.pdf", generatePdf(data));
            if (formats.contains("xlsx")) result.put("weekly-report.xlsx", generateXlsx(data));
        } catch (Exception e) {
            log.error("Error generating reports", e);
        }

        return result;
    }

    private static byte[] generateCsv(ReportDataDto d) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter w = new PrintWriter(out);

        // 1) Деталі покупок
        w.println("Movie,Plan,User,PurchaseDate,Price");
        d.getPurchases().forEach(p -> w.printf("%s,%s,%s,%s,%s%n",
                p.getMovie().getTitle(),
                p.getSelectedMoviePlan().getType(),
                p.getUser().getEmail(),
                p.getPurchasedAt(),
                p.getSelectedMoviePlan().getPrice().toPlainString()
        ));
        w.println();

        // 2) Sales Summary
        w.println("=== Sales Summary ===");
        w.printf("Total Movies Purchased,%d%n", d.getPurchases().size());
        w.printf("Total Revenue,%s%n", d.getTotalRevenue());
        w.printf("Average per Purchase,%s%n", d.getAveragePurchasePrice());
        w.printf("Active Users,%d%n", d.getActiveUsers());
        w.printf("Unique Movies Sold,%d%n", d.getUniqueMovies());
        w.println();

        // 3) Plans Breakdown
        w.println("=== Plans Breakdown (Qty & Revenue) ===");
        w.println("Plan,Qty,Revenue");
        d.getPlanQty().forEach((plan, qty) -> {
            var rev = d.getPlanRevenue().getOrDefault(plan, java.math.BigDecimal.ZERO);
            w.printf("%s,%d,%s%n", plan, qty, rev);
        });
        w.println();

        // 4) Top Genres
        w.println("=== Top Genres (Qty & Revenue) ===");
        w.println("Genre,Qty,Revenue");
        d.getGenreQty().forEach((g, qty) -> {
            var rev = d.getGenreRevenue().getOrDefault(g, java.math.BigDecimal.ZERO);
            w.printf("%s,%d,%s%n", g, qty, rev);
        });
        w.println();

        // 5) Top Movies
        w.println("=== Top Movies (ID, Title, Genres, Units, Revenue) ===");
        w.println("MovieId,Title,Genres,Units,Revenue");
        d.getTopMovies().forEach(tm -> w.printf(
                "%s,%s,%s,%d,%s%n",
                tm.getMovieId(),
                tm.getTitle().replace(",", " "),                // щоб не ламати CSV
                tm.getGenres().stream().map(Enum::name).collect(java.util.stream.Collectors.joining("|")),
                tm.getUnits(),
                tm.getRevenue()
        ));

        w.flush();
        return out.toByteArray();
    }

    private static byte[] generatePdf(ReportDataDto d) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        var doc = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4, 36, 36, 54, 36);
        com.itextpdf.text.pdf.PdfWriter.getInstance(doc, out);
        doc.open();

        var titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
        var headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD);
        var bodyFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11);

        doc.add(new com.itextpdf.text.Paragraph("🎬 Weekly Movie Purchase Report", titleFont));
        doc.add(new com.itextpdf.text.Paragraph("Generated: " + java.time.LocalDateTime.now(), bodyFont));
        doc.add(com.itextpdf.text.Chunk.NEWLINE);

        // Sales Summary
        doc.add(new com.itextpdf.text.Paragraph("Sales Summary", headerFont));
        var t1 = new com.itextpdf.text.pdf.PdfPTable(2);
        t1.setWidthPercentage(100);
        addKvRow(t1, "Total Movies Purchased", String.valueOf(d.getPurchases().size()), bodyFont);
        addKvRow(t1, "Total Revenue", d.getTotalRevenue().toPlainString(), bodyFont);
        addKvRow(t1, "Average per Purchase", d.getAveragePurchasePrice().toPlainString(), bodyFont);
        addKvRow(t1, "Active Users", String.valueOf(d.getActiveUsers()), bodyFont);
        addKvRow(t1, "Unique Movies Sold", String.valueOf(d.getUniqueMovies()), bodyFont);
        doc.add(t1);
        doc.add(com.itextpdf.text.Chunk.NEWLINE);

        // Plans Breakdown
        doc.add(new com.itextpdf.text.Paragraph("Plans Breakdown (Qty & Revenue)", headerFont));
        var t2 = new com.itextpdf.text.pdf.PdfPTable(3);
        t2.setWidthPercentage(100);
        addHeaderRow(t2, new String[]{"Plan", "Qty", "Revenue"}, bodyFont);
        d.getPlanQty().forEach((plan, qty) -> {
            var rev = d.getPlanRevenue().getOrDefault(plan, java.math.BigDecimal.ZERO);
            addRow(t2, new String[]{plan, String.valueOf(qty), rev.toPlainString()}, bodyFont);
        });
        doc.add(t2);
        doc.add(com.itextpdf.text.Chunk.NEWLINE);

        // Top Genres
        doc.add(new com.itextpdf.text.Paragraph("Top Genres (Qty & Revenue)", headerFont));
        var t3 = new com.itextpdf.text.pdf.PdfPTable(3);
        t3.setWidthPercentage(100);
        addHeaderRow(t3, new String[]{"Genre", "Qty", "Revenue"}, bodyFont);
        d.getGenreQty().forEach((g, qty) -> {
            var rev = d.getGenreRevenue().getOrDefault(g, java.math.BigDecimal.ZERO);
            addRow(t3, new String[]{g, String.valueOf(qty), rev.toPlainString()}, bodyFont);
        });
        doc.add(t3);
        doc.add(com.itextpdf.text.Chunk.NEWLINE);

        // Top Movies
        doc.add(new com.itextpdf.text.Paragraph("Top Movies (ID, Title, Genres, Units, Revenue)", headerFont));
        var t4 = new com.itextpdf.text.pdf.PdfPTable(5);
        t4.setWidthPercentage(100);
        addHeaderRow(t4, new String[]{"MovieId", "Title", "Genres", "Units", "Revenue"}, bodyFont);
        for (var tm : d.getTopMovies()) {
            addRow(t4, new String[]{
                    tm.getMovieId().toString(),
                    tm.getTitle(),
                    tm.getGenres().stream().map(Enum::name).collect(java.util.stream.Collectors.joining("|")),
                    String.valueOf(tm.getUnits()),
                    tm.getRevenue().toPlainString()
            }, bodyFont);
        }
        doc.add(t4);

        doc.close();
        return out.toByteArray();
    }

    private static void addKvRow(com.itextpdf.text.pdf.PdfPTable t, String k, String v, com.itextpdf.text.Font f) {
        var c1 = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(k, f));
        c1.setBorder(com.itextpdf.text.Rectangle.BOX);
        var c2 = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(v, f));
        c2.setBorder(com.itextpdf.text.Rectangle.BOX);
        t.addCell(c1);
        t.addCell(c2);
    }

    private static void addHeaderRow(com.itextpdf.text.pdf.PdfPTable t, String[] headers, com.itextpdf.text.Font f) {
        for (var h : headers) {
            var cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(h, f));
            cell.setBackgroundColor(new com.itextpdf.text.BaseColor(240, 240, 240));
            t.addCell(cell);
        }
    }

    private static void addRow(com.itextpdf.text.pdf.PdfPTable t, String[] values, com.itextpdf.text.Font f) {
        for (var v : values) {
            t.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(v, f)));
        }
    }


    private static void addLine(Document doc, String text) {
        try {
            doc.add(new Paragraph(" - " + text));
        } catch (DocumentException e) {
            log.error("Error adding paragraph", e);
        }
    }

    private static byte[] generateXlsx(ReportDataDto d) throws Exception {
        try (var workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            // Styles
            var headerStyle = workbook.createCellStyle();
            var bold = workbook.createFont();
            bold.setBold(true);
            headerStyle.setFont(bold);

            // Summary
            var sSummary = workbook.createSheet("Summary");
            int r = 0;
            r = writeKeyValueRow(sSummary, r, "Total Movies Purchased", String.valueOf(d.getPurchases().size()), headerStyle);
            r = writeKeyValueRow(sSummary, r, "Total Revenue", d.getTotalRevenue().toPlainString(), headerStyle);
            r = writeKeyValueRow(sSummary, r, "Average per Purchase", d.getAveragePurchasePrice().toPlainString(), headerStyle);
            r = writeKeyValueRow(sSummary, r, "Active Users", String.valueOf(d.getActiveUsers()), headerStyle);
            r = writeKeyValueRow(sSummary, r, "Unique Movies Sold", String.valueOf(d.getUniqueMovies()), headerStyle);

            autoSizeAll(sSummary, 2);

            // Plans
            var sPlans = workbook.createSheet("Plans");
            var hr = sPlans.createRow(0);
            createCell(hr, 0, "Plan", headerStyle);
            createCell(hr, 1, "Qty", headerStyle);
            createCell(hr, 2, "Revenue", headerStyle);
            int i = 1;
            for (var e : d.getPlanQty().entrySet()) {
                var row = sPlans.createRow(i++);
                row.createCell(0).setCellValue(e.getKey());
                row.createCell(1).setCellValue(e.getValue());
                row.createCell(2).setCellValue(d.getPlanRevenue().getOrDefault(e.getKey(), java.math.BigDecimal.ZERO).doubleValue());
            }
            autoSizeAll(sPlans, 3);

            // Genres
            var sGenres = workbook.createSheet("Genres");
            hr = sGenres.createRow(0);
            createCell(hr, 0, "Genre", headerStyle);
            createCell(hr, 1, "Qty", headerStyle);
            createCell(hr, 2, "Revenue", headerStyle);
            i = 1;
            for (var e : d.getGenreQty().entrySet()) {
                var row = sGenres.createRow(i++);
                row.createCell(0).setCellValue(e.getKey());
                row.createCell(1).setCellValue(e.getValue());
                row.createCell(2).setCellValue(d.getGenreRevenue().getOrDefault(e.getKey(), java.math.BigDecimal.ZERO).doubleValue());
            }
            autoSizeAll(sGenres, 3);

            // TopMovies
            var sTop = workbook.createSheet("TopMovies");
            hr = sTop.createRow(0);
            createCell(hr, 0, "MovieId", headerStyle);
            createCell(hr, 1, "Title", headerStyle);
            createCell(hr, 2, "Genres", headerStyle);
            createCell(hr, 3, "Units", headerStyle);
            createCell(hr, 4, "Revenue", headerStyle);
            i = 1;
            for (var tm : d.getTopMovies()) {
                var row = sTop.createRow(i++);
                row.createCell(0).setCellValue(tm.getMovieId().toString());
                row.createCell(1).setCellValue(tm.getTitle());
                row.createCell(2).setCellValue(tm.getGenres().stream().map(Enum::name).collect(java.util.stream.Collectors.joining("|")));
                row.createCell(3).setCellValue(tm.getUnits());
                row.createCell(4).setCellValue(tm.getRevenue().doubleValue());
            }
            autoSizeAll(sTop, 5);

            // Purchases (деталі)
            var sPurch = workbook.createSheet("Purchases");
            hr = sPurch.createRow(0);
            createCell(hr, 0, "Movie", headerStyle);
            createCell(hr, 1, "Plan", headerStyle);
            createCell(hr, 2, "User", headerStyle);
            createCell(hr, 3, "Purchase Date", headerStyle);
            createCell(hr, 4, "Price", headerStyle);
            i = 1;
            for (var p : d.getPurchases()) {
                var row = sPurch.createRow(i++);
                row.createCell(0).setCellValue(p.getMovie().getTitle());
                row.createCell(1).setCellValue(p.getSelectedMoviePlan().getType().name());
                row.createCell(2).setCellValue(p.getUser().getEmail());
                row.createCell(3).setCellValue(p.getPurchasedAt().toString());
                row.createCell(4).setCellValue(p.getSelectedMoviePlan().getPrice().doubleValue());
            }
            autoSizeAll(sPurch, 5);

            var out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private static int writeKeyValueRow(org.apache.poi.ss.usermodel.Sheet s, int r, String k, String v, org.apache.poi.ss.usermodel.CellStyle headerStyle) {
        var row = s.createRow(r++);
        createCell(row, 0, k, headerStyle);
        row.createCell(1).setCellValue(v);
        return r;
    }

    private static void createCell(org.apache.poi.ss.usermodel.Row row, int col, String value, org.apache.poi.ss.usermodel.CellStyle style) {
        var cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private static void autoSizeAll(org.apache.poi.ss.usermodel.Sheet s, int cols) {
        for (int c = 0; c < cols; c++) s.autoSizeColumn(c);
    }

}
