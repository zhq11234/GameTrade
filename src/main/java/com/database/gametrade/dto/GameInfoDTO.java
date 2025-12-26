package com.database.gametrade.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
public class GameInfoDTO {
    private String gameName;
    private String category;
    private BigDecimal price;
    private String companyName;
    private LocalDateTime releaseTime;
    private String description;
    private String status;
    private String downloadLink;
    private String licenseNumber;

    public GameInfoDTO() {}

    public GameInfoDTO(String gameName, String category, BigDecimal price, String companyName, 
                      LocalDateTime releaseTime, String description, String status, 
                      String downloadLink, String licenseNumber) {
        this.gameName = gameName;
        this.category = category;
        this.price = price;
        this.companyName = companyName;
        this.releaseTime = releaseTime;
        this.description = description;
        this.status = status;
        this.downloadLink = downloadLink;
        this.licenseNumber = licenseNumber;
    }
}
