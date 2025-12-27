package com.database.gametrade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderRequestDTO {
    
    @NotBlank(message = "买家昵称不能为空")
    @Size(max = 50, message = "买家昵称长度不能超过50个字符")
    private String buyerNickname;
    
    @NotBlank(message = "游戏名不能为空")
    @Size(max = 100, message = "游戏名长度不能超过100个字符")
    private String gameName;
    
    public OrderRequestDTO() {}
    
    public OrderRequestDTO(String buyerNickname, String gameName) {
        this.buyerNickname = buyerNickname;
        this.gameName = gameName;
    }
}