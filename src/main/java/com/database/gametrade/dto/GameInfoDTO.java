package com.database.gametrade.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Score;

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
    private String downloadLink;
    private String licenseNumber;
    private String score;
    private int salesVolume;
    private String status;

    public GameInfoDTO() {}

    public GameInfoDTO(String gameName, String category, BigDecimal price,
                       String companyName, LocalDateTime localDateTime,
                       String description, String licenseNumber,
                       String score, String salesVolume) {
        this.gameName = gameName;
        this.category = category;
        this.price = price;
        this.companyName = companyName;
        this.releaseTime = localDateTime;
        this.description = description;
        this.licenseNumber = licenseNumber;
        this.score = score;
        this.salesVolume = Integer.parseInt(salesVolume);
    }
}
