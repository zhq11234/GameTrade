package com.database.gametrade.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Entity
@Table(name = "buyer_info", uniqueConstraints = {
    @UniqueConstraint(columnNames = "nickname")
})
public class BuyerInfo {
    
    @Id
    @Column(name = "nickname", length = 50, nullable = false)
    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;
    
    @Column(name = "account", length = 50, nullable = false)
    @NotBlank(message = "账号不能为空")
    @Size(max = 50, message = "账号长度不能超过50个字符")
    private String account;
    
    @Column(name = "gender", length = 10, nullable = false)
    @NotBlank(message = "性别不能为空")
    @Size(max = 1, message = "性别长度不能超过1个字符")
    private String gender = "男";
    
    @Column(name = "birthdate", nullable = false)
    private LocalDate birthdate = LocalDate.of(2000, 1, 1);
    
    // 与UserInfo的外键关系
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account", referencedColumnName = "account", insertable = false, updatable = false)
    private UserInfo userInfo;
    
    // 默认构造函数
    public BuyerInfo() {}
    
    // 带参数的构造函数
    public BuyerInfo(String nickname, String account) {
        this.nickname = nickname;
        this.account = account;
        this.gender = "男";
        this.birthdate = LocalDate.of(2000, 1, 1);
    }
    
    public BuyerInfo(String nickname, String account, String gender, LocalDate birthdate) {
        this.nickname = nickname;
        this.account = account;
        this.gender = gender != null ? gender : "男";
        this.birthdate = birthdate != null ? birthdate : LocalDate.of(2000, 1, 1);
    }
    
    // Getters and Setters
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getAccount() {
        return account;
    }
    
    public void setAccount(String account) {
        this.account = account;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender != null ? gender : "男";
    }
    
    public LocalDate getBirthdate() {
        return birthdate;
    }
    
    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate != null ? birthdate : LocalDate.of(2000, 1, 1);
    }
    
    public UserInfo getUserInfo() {
        return userInfo;
    }
    
    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
    
    @Override
    public String toString() {
        return "BuyerInfo{" +
                "nickname='" + nickname + '\'' +
                ", account='" + account + '\'' +
                ", gender='" + gender + '\'' +
                ", birthdate=" + birthdate +
                '}';
    }
}
