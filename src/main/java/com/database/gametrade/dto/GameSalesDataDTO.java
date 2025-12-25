package com.database.gametrade.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class GameSalesDataDTO {
    private String gameName;
    private BigDecimal price;
    private Integer salesCount;
    private BigDecimal salesAmount;
    private Integer visitorCount;
    private BigDecimal conversionRate;

    public GameSalesDataDTO() {}

    public GameSalesDataDTO(String gameName, BigDecimal price, Integer salesCount, 
                           BigDecimal salesAmount, Integer visitorCount, BigDecimal conversionRate) {
        this.gameName = gameName;
        this.price = price;
        this.salesCount = salesCount;
        this.salesAmount = salesAmount;
        this.visitorCount = visitorCount;
        this.conversionRate = conversionRate;
    }
}
