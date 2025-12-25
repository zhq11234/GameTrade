package com.database.gametrade.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class GameCreateRequestDTO {
    @NotBlank(message = "账号不能为空")
    @Size(max = 50, message = "账号长度不能超过50个字符")
    private String account;

    @NotBlank(message = "游戏名不能为空")
    @Size(max = 100, message = "游戏名长度不能超过100个字符")
    private String gameName;

    @NotBlank(message = "游戏类别不能为空")
    @Size(max = 50, message = "游戏类别长度不能超过50个字符")
    private String category;

    @DecimalMin(value = "0.0", message = "价格不能为负数")
    private BigDecimal price;

    @NotBlank(message = "游戏简介不能为空")
    @Size(max = 500, message = "游戏简介长度不能超过500个字符")
    private String description;

    @NotBlank(message = "下载链接不能为空")
    @Size(max = 255, message = "下载链接长度不能超过255个字符")
    private String downloadLink;

    @NotBlank(message = "版号不能为空")
    @Size(max = 50, message = "版号长度不能超过50个字符")
    private String licenseNumber;
}
