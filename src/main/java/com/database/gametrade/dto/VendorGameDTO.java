package com.database.gametrade.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class VendorGameDTO {
    private String gameName;
    private String category;
    private BigDecimal price;
    private String description;

    public VendorGameDTO() {}

    public VendorGameDTO(String gameName, String category, BigDecimal price, String description) {
        this.gameName = gameName;
        this.category = category;
        this.price = price;
        this.description = description;
    }
}
