package com.database.gametrade.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_info", uniqueConstraints = {
    @UniqueConstraint(columnNames = "account"),
    @UniqueConstraint(columnNames = "contact")
})
public class UserInfo {
    
    @Id
    @Column(name = "account", length = 50, nullable = false)
    @NotBlank(message = "账号不能为空")
    @Size(max = 50, message = "账号长度不能超过50个字符")
    private String account;
    
    @Column(name = "role", length = 20, nullable = false)
    @NotBlank(message = "角色不能为空")
    @Size(max = 20, message = "角色长度不能超过20个字符")
    private String role;
    
    @Column(name = "password", length = 100, nullable = false)
    @NotBlank(message = "密码不能为空")
    @Size(max = 100, message = "密码长度不能超过100个字符")
    private String password;
    
    @Column(name = "contact", length = 100, nullable = false)
    @NotBlank(message = "联系方式不能为空")
    @Size(max = 100, message = "联系方式长度不能超过100个字符")
    private String contact;
    
    @Column(name = "register_time", nullable = false)
    private LocalDateTime registerTime;
    
    // 默认构造函数
    public UserInfo() {
        this.registerTime = LocalDateTime.now();
    }
    
    // 带参数的构造函数
    public UserInfo(String account, String role, String password, String contact) {
        this.account = account;
        this.role = role;
        this.password = password;
        this.contact = contact;
        this.registerTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getAccount() {
        return account;
    }
    
    public void setAccount(String account) {
        this.account = account;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getContact() {
        return contact;
    }
    
    public void setContact(String contact) {
        this.contact = contact;
    }
    
    public LocalDateTime getRegisterTime() {
        return registerTime;
    }
    
    public void setRegisterTime(LocalDateTime registerTime) {
        this.registerTime = registerTime;
    }
    
    @Override
    public String toString() {
        return "UserInfo{" +
                "account='" + account + '\'' +
                ", role='" + role + '\'' +
                ", contact='" + contact + '\'' +
                ", registerTime=" + registerTime +
                '}';
    }
}
