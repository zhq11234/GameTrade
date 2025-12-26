package com.database.gametrade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VendorGameQueryRequestDTO {
    @NotBlank(message = "厂商账号不能为空")
    @Size(max = 50, message = "厂商账号长度不能超过50个字符")
    private String account;

    @Size(max=10,message = "游戏名称长度不能超过10个字符")
    private String status="全部";
}
