package com.database.gametrade.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Score;

@Setter
@Getter
public class GameLibraryResponseDTO {
    private String gameName;
    private String licenseNumber;
    private String score;
    private String comment;
    private String reviewTime;
    
    public GameLibraryResponseDTO() {}
    
    public GameLibraryResponseDTO(String gameName, String licenseNumber,
                                  String score, String comment, String reviewTime) {
        this.gameName = gameName;
        this.licenseNumber = licenseNumber;
        this.score = score;
        this.comment = comment;
        this.reviewTime = reviewTime;
    }
}