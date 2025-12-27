package com.database.gametrade.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class GameSearchRequestDTO {
    
    // 按游戏名查询
    @Size(max = 100, message = "游戏名长度不能超过100个字符")
    private String gameName;
    
    // 按游戏分类查询
    @Size(max = 50, message = "游戏分类长度不能超过50个字符")
    private String category;
    
    // 按游戏热度查询（最小热度阈值）
    private BigDecimal minPopularity;
    
    // 按买家偏好查询
    @Size(max = 50, message = "买家昵称长度不能超过50个字符")
    private String buyerNickname;
    
    public GameSearchRequestDTO() {}
    
    public GameSearchRequestDTO(String gameName, String category, BigDecimal minPopularity, String buyerNickname) {
        this.gameName = gameName;
        this.category = category;
        this.minPopularity = minPopularity;
        this.buyerNickname = buyerNickname;
    }
}