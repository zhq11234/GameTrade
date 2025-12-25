package com.database.gametrade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GameApplicationRequestDTO {
    @NotBlank(message = "厂商账号不能为空")
    @Size(max = 50, message = "厂商账号长度不能超过50个字符")
    private String account;

    @NotBlank(message = "游戏名不能为空")
    @Size(max = 100, message = "游戏名长度不能超过100个字符")
    private String gameName;
}
