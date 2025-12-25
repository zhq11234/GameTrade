package com.database.gametrade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VendorRegisterRequestDTO {
    @NotBlank(message = "角色不能为空")
    @Size(max = 20, message = "角色长度不能超过20个字符")
    private String role = "vendor";

    @NotBlank(message = "账号不能为空")
    @Size(max = 50, message = "账号长度不能超过50个字符")
    private String account;

    @NotBlank(message = "密码不能为空")
    @Size(max = 100, message = "密码长度不能超过100个字符")
    private String password;

    @NotBlank(message = "联系方式不能为空")
    @Size(max = 100, message = "联系方式长度不能超过100个字符")
    private String contact;

    @NotBlank(message = "企业名不能为空")
    @Size(max = 100, message = "企业名长度不能超过100个字符")
    private String companyName;

    @NotBlank(message = "注册地址不能为空")
    @Size(max = 200, message = "注册地址长度不能超过200个字符")
    private String registeredAddress;

    @NotBlank(message = "联系人不能为空")
    @Size(max = 50, message = "联系人长度不能超过50个字符")
    private String contactPerson;
}
