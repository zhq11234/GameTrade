package com.database.gametrade.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "vendor_info", uniqueConstraints = {
    @UniqueConstraint(columnNames = "company_name")
})
public class VendorInfo {
    
    @Id
    @Column(name = "company_name", length = 100, nullable = false)
    @NotBlank(message = "企业名不能为空")
    @Size(max = 100, message = "企业名长度不能超过100个字符")
    private String companyName;
    
    @Column(name = "account", length = 50, nullable = false)
    @NotBlank(message = "账号不能为空")
    @Size(max = 50, message = "账号长度不能超过50个字符")
    private String account;
    
    @Column(name = "registered_address", length = 200, nullable = false)
    @NotBlank(message = "注册地址不能为空")
    @Size(max = 200, message = "注册地址长度不能超过200个字符")
    private String registeredAddress;
    
    @Column(name = "contact_person", length = 50, nullable = false)
    @NotBlank(message = "联系人不能为空")
    @Size(max = 50, message = "联系人长度不能超过50个字符")
    private String contactPerson;
    
    // 与UserInfo的外键关系
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account", referencedColumnName = "account", insertable = false, updatable = false)
    private UserInfo userInfo;
    
    // 默认构造函数
    public VendorInfo() {}
    
    // 带参数的构造函数
    public VendorInfo(String companyName, String account, String registeredAddress, String contactPerson) {
        this.companyName = companyName;
        this.account = account;
        this.registeredAddress = registeredAddress;
        this.contactPerson = contactPerson;
    }
    
    // Getters and Setters
    public String getCompanyName() {
        return companyName;
    }
    
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    
    public String getAccount() {
        return account;
    }
    
    public void setAccount(String account) {
        this.account = account;
    }
    
    public String getRegisteredAddress() {
        return registeredAddress;
    }
    
    public void setRegisteredAddress(String registeredAddress) {
        this.registeredAddress = registeredAddress;
    }
    
    public String getContactPerson() {
        return contactPerson;
    }
    
    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }
    
    public UserInfo getUserInfo() {
        return userInfo;
    }
    
    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
    
    @Override
    public String toString() {
        return "VendorInfo{" +
                "companyName='" + companyName + '\'' +
                ", account='" + account + '\'' +
                ", registeredAddress='" + registeredAddress + '\'' +
                ", contactPerson='" + contactPerson + '\'' +
                '}';
    }
}
