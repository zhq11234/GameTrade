package com.database.gametrade.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserResponseDTO {
    private String account;
    private String role;
    private String contact;
    private String registerTime;

    public UserResponseDTO() {}

    public UserResponseDTO(String account, String role, String contact, java.time.LocalDateTime registerTime) {
        this.account = account;
        this.role = role;
        this.contact = contact;
        this.registerTime = registerTime != null ? registerTime.toString() : null;
    }
}
