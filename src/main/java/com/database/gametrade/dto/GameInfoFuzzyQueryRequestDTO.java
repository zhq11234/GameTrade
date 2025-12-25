package com.database.gametrade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GameInfoFuzzyQueryRequestDTO {
    @NotBlank(message = "游戏名关键词不能为空")
    @Size(max = 100, message = "游戏名关键词长度不能超过100个字符")
    private String keyword;
}
