package com.database.gametrade.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class GameReviewRequestDTO {
    
    @NotBlank(message = "买家昵称不能为空")
    @Size(max = 50, message = "买家昵称长度不能超过50个字符")
    private String buyerNickname;
    
    @NotBlank(message = "游戏名不能为空")
    @Size(max = 100, message = "游戏名长度不能超过100个字符")
    private String gameName;
    
    @DecimalMin(value = "0.0", message = "评分不能低于0.0")
    @DecimalMax(value = "10.0", message = "评分不能高于10.0")
    private BigDecimal score;
    
    @Size(max = 200, message = "评论长度不能超过200个字符")
    private String comment;
    
    public GameReviewRequestDTO() {}
    
    public GameReviewRequestDTO(String buyerNickname, String gameName, BigDecimal score, String comment) {
        this.buyerNickname = buyerNickname;
        this.gameName = gameName;
        this.score = score;
        this.comment = comment;
    }
}