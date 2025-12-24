package com.database.gametrade.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_application")
public class GameApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Integer applicationId;

    @Column(name = "game_name", length = 100, nullable = false)
    @NotBlank(message = "游戏名不能为空")
    @Size(max = 100, message = "游戏名长度不能超过100个字符")
    private String gameName;

    @Column(name = "company_name", length = 100, nullable = false)
    @NotBlank(message = "企业名不能为空")
    @Size(max = 100, message = "企业名长度不能超过100个字符")
    private String companyName;

    @Column(name = "approval_status", length = 20, nullable = false)
    @NotBlank(message = "审批状态不能为空")
    @Size(max = 20, message = "审批状态长度不能超过20个字符")
    private String approvalStatus = "待审批";

    @Column(name = "approval_result", length = 200)
    @Size(max = 200, message = "审批结果长度不能超过200个字符")
    private String approvalResult;

    @Column(name = "application_time", nullable = false)
    private LocalDateTime applicationTime;

    // 与GameInfo的外键关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_name", referencedColumnName = "game_name", insertable = false, updatable = false)
    private GameInfo gameInfo;

    // 与VendorInfo的外键关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_name", referencedColumnName = "company_name", insertable = false, updatable = false)
    private VendorInfo vendorInfo;

    // 默认构造函数
    public GameApplication() {
        this.applicationTime = LocalDateTime.now();
    }

    // 带参数的构造函数
    public GameApplication(String gameName, String companyName) {
        this.gameName = gameName;
        this.companyName = companyName;
        this.approvalStatus = "待审批";
        this.applicationTime = LocalDateTime.now();
    }

    public GameApplication(String gameName, String companyName, String approvalStatus, String approvalResult) {
        this.gameName = gameName;
        this.companyName = companyName;
        this.approvalStatus = approvalStatus != null ? approvalStatus : "待审批";
        this.approvalResult = approvalResult;
        this.applicationTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Integer applicationId) {
        this.applicationId = applicationId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus != null ? approvalStatus : "待审批";
    }

    public String getApprovalResult() {
        return approvalResult;
    }

    public void setApprovalResult(String approvalResult) {
        this.approvalResult = approvalResult;
    }

    public LocalDateTime getApplicationTime() {
        return applicationTime;
    }

    public void setApplicationTime(LocalDateTime applicationTime) {
        this.applicationTime = applicationTime;
    }

    public GameInfo getGameInfo() {
        return gameInfo;
    }

    public void setGameInfo(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

    public VendorInfo getVendorInfo() {
        return vendorInfo;
    }

    public void setVendorInfo(VendorInfo vendorInfo) {
        this.vendorInfo = vendorInfo;
    }

    @Override
    public String toString() {
        return "GameApplication{" +
                "applicationId=" + applicationId +
                ", gameName='" + gameName + '\'' +
                ", companyName='" + companyName + '\'' +
                ", approvalStatus='" + approvalStatus + '\'' +
                ", approvalResult='" + approvalResult + '\'' +
                ", applicationTime=" + applicationTime +
                '}';
    }
}
