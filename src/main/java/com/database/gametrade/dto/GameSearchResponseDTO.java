package com.database.gametrade.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class GameSearchResponseDTO {
    private String gameName;
    private String category;
    private BigDecimal price;
    private BigDecimal score;
    private Integer salesVolume;
    private String companyName;
    
    public GameSearchResponseDTO() {}
    
    public GameSearchResponseDTO(String gameName, String category, BigDecimal price, 
                                BigDecimal score, Integer salesVolume, String companyName) {
        this.gameName = gameName;
        this.category = category;
        this.price = price;
        this.score = score;
        this.salesVolume = salesVolume;
        this.companyName = companyName;
    }
}