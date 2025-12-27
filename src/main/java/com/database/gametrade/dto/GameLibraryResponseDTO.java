package com.database.gametrade.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GameLibraryResponseDTO {
    private String gameName;
    private String licenseNumber;
    
    public GameLibraryResponseDTO() {}
    
    public GameLibraryResponseDTO(String gameName, String licenseNumber) {
        this.gameName = gameName;
        this.licenseNumber = licenseNumber;
    }
}