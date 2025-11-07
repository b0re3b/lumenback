package com.lumen.awsspringbootservice.service.impl;

import com.lumen.awsspringbootservice.dto.report.ReportDataDto;
import com.lumen.awsspringbootservice.dto.report.TopMovieDto;
import com.lumen.awsspringbootservice.entity.Purchase;
import com.lumen.awsspringbootservice.repository.PurchaseRepository;
import com.lumen.awsspringbootservice.util.ReportGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseReportService {

    private final PurchaseRepository purchaseRepository;
    private final SimpleEmailService simpleEmailService;

    @Value("${app.report.formats}")
    private List<String> reportFormats;

    @Value("${app.report.period.duration:P4W}")
    private Period reportPeriod;

    public void generateAndSendReport(LocalDateTime to, LocalDateTime from) {

        LocalDateTime effectiveTo = (to != null) ? to : LocalDateTime.now();

        LocalDateTime effectiveFrom = (from != null)
                ? from
                : effectiveTo.minus(reportPeriod);

        List<Purchase> purchases = purchaseRepository.findAllByPurchasedAtBetween(effectiveFrom, effectiveTo);
        if (purchases.isEmpty()) {
            log.warn("No purchases found for report period [{} - {}]. Generating empty report...", effectiveFrom, effectiveTo);

            ReportDataDto emptyReport = ReportDataDto.builder()
                    .activeUsers(0)
                    .averagePurchasePrice(BigDecimal.ZERO)
                    .genreQty(Map.of("No data", 0L))
                    .genreRevenue(Map.of("No data", BigDecimal.ZERO))
                    .planQty(Map.of("No data", 0L))
                    .planRevenue(Map.of("No data", BigDecimal.ZERO))
                    .purchases(List.of())
                    .topMovies(List.of())
                    .totalRevenue(BigDecimal.ZERO)
                    .uniqueMovies(0)
                    .build();
            Map<String, byte[]> reports = ReportGenerator.generateReports(emptyReport, reportFormats);
            simpleEmailService.sendReports(reports);

            log.info("Empty report generated and sent successfully for period [{} - {}]", effectiveFrom, effectiveTo);
            return;
        }

        ReportDataDto reportData = aggregateReportData(purchases);
        Map<String, byte[]> reports = ReportGenerator.generateReports(reportData, reportFormats);

        log.info("Generated {} reports. Sending to admins...", reports.size());
        simpleEmailService.sendReports(reports);

        log.info("Reports generated and sent successfully for period [{} - {}]", effectiveFrom, effectiveTo);
    }

    private ReportDataDto aggregateReportData(List<Purchase> purchases) {
        var scale2 = new java.math.MathContext(10);
        var zero = java.math.BigDecimal.ZERO;

        BigDecimal totalRevenue = purchases.stream()
                .map(p -> p.getSelectedMoviePlan().getPrice())
                .reduce(zero, BigDecimal::add);

        BigDecimal avgPrice = purchases.isEmpty()
                ? zero
                : totalRevenue.divide(java.math.BigDecimal.valueOf(purchases.size()), 2, java.math.RoundingMode.HALF_UP);

        long activeUsers = purchases.stream()
                .map(p -> p.getUser().getEmail())
                .distinct()
                .count();

        long uniqueMovies = purchases.stream()
                .map(p -> p.getMovie().getId())
                .distinct()
                .count();

        Map<String, Long> planQty = purchases.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        p -> p.getSelectedMoviePlan().getType().name(),
                        java.util.stream.Collectors.counting()
                ));

        Map<String, BigDecimal> planRevenue = purchases.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        p -> p.getSelectedMoviePlan().getType().name(),
                        java.util.stream.Collectors.reducing(
                                zero,
                                p -> p.getSelectedMoviePlan().getPrice(),
                                BigDecimal::add
                        )
                ));

        Map<String, Long> genreQty = purchases.stream()
                .flatMap(p -> p.getMovie().getGenres().stream().map(g -> g.name()))
                .collect(java.util.stream.Collectors.groupingBy(
                        g -> g,
                        java.util.stream.Collectors.counting()
                ));

        Map<String, BigDecimal> genreRevenue = purchases.stream()
                .flatMap(p -> p.getMovie().getGenres().stream()
                        .map(g -> java.util.Map.entry(g.name(), p.getSelectedMoviePlan().getPrice())))
                .collect(java.util.stream.Collectors.groupingBy(
                        java.util.Map.Entry::getKey,
                        java.util.stream.Collectors.reducing(zero, java.util.Map.Entry::getValue, BigDecimal::add)
                ));

        Map<java.util.UUID, Long> movieUnits = purchases.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        p -> p.getMovie().getId(),
                        java.util.stream.Collectors.counting()
                ));

        Map<java.util.UUID, BigDecimal> movieRevenue = purchases.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        p -> p.getMovie().getId(),
                        java.util.stream.Collectors.reducing(
                                zero,
                                p -> p.getSelectedMoviePlan().getPrice(),
                                BigDecimal::add
                        )
                ));

        List<TopMovieDto> topMovies = movieRevenue.entrySet()
                .stream()
                .sorted(java.util.Map.Entry.<java.util.UUID, BigDecimal>comparingByValue().reversed())
                .limit(10)
                .map(e -> {
                    var movieId = e.getKey();
                    var anyPurchase = purchases.stream()
                            .filter(p -> p.getMovie().getId().equals(movieId))
                            .findFirst()
                            .orElseThrow();
                    return new TopMovieDto(
                            movieId,
                            anyPurchase.getMovie().getTitle(),
                            anyPurchase.getMovie().getGenres(),
                            movieUnits.get(movieId),
                            e.getValue()
                    );
                })
                .toList();

        return ReportDataDto.builder()
                .purchases(purchases)
                .totalRevenue(totalRevenue)
                .averagePurchasePrice(avgPrice)
                .activeUsers(activeUsers)
                .uniqueMovies(uniqueMovies)
                .planQty(planQty)
                .planRevenue(planRevenue)
                .genreQty(genreQty)
                .genreRevenue(genreRevenue)
                .topMovies(topMovies)
                .build();
    }

}
