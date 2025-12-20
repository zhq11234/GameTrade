package com.database.gametrade.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "username", unique = true, nullable = false, length = 20)
    @jakarta.validation.constraints.NotBlank(message = "用户名不能为空")
    @jakarta.validation.constraints.Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    @jakarta.validation.constraints.Pattern(regexp = "^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$", message = "用户名只能包含字母、数字、下划线和中文字符")
    private String username;
    
    @Column(name = "password", nullable = false, length = 100)
    @jakarta.validation.constraints.NotBlank(message = "密码不能为空")
    @jakarta.validation.constraints.Size(min = 8, max = 100, message = "密码长度必须在8-100位之间")
    private String password;
    
    @Column(name = "email", unique = true, length = 100)
    @jakarta.validation.constraints.Email(message = "邮箱格式不正确")
    @jakarta.validation.constraints.Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;
    
    @Column(name = "phone", length = 20)
    @jakarta.validation.constraints.Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @Column(name = "nickname", length = 50)
    @jakarta.validation.constraints.Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;
    
    // 默认构造函数
    public User() {}
    
    // 带参数的构造函数
    public User(String username, String password, String email, String phone, String nickname) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.nickname = nickname;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", nickname='" + nickname + '\'' +
                '}';
    }
}
