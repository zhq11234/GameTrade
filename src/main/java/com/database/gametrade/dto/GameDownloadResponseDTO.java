package com.database.gametrade.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GameDownloadResponseDTO {
    private String downloadLink;
    private String gameName;
    private String licenseNumber;
    private String description;
    
    public GameDownloadResponseDTO() {}
    
    public GameDownloadResponseDTO(String downloadLink, String gameName, String licenseNumber, String description) {
        this.downloadLink = downloadLink;
        this.gameName = gameName;
        this.licenseNumber = licenseNumber;
        this.description = description;
    }
}