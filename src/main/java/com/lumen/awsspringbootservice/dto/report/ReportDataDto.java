package com.lumen.awsspringbootservice.dto.report;

import com.lumen.awsspringbootservice.entity.Purchase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportDataDto {
    private List<Purchase> purchases;
    private BigDecimal totalRevenue;
    private BigDecimal averagePurchasePrice;
    private long activeUsers;
    private long uniqueMovies;
    private Map<String, Long> planQty;
    private Map<String, BigDecimal> planRevenue;
    private Map<String, Long> genreQty;
    private Map<String, BigDecimal> genreRevenue;
    private List<TopMovieDto> topMovies;

}