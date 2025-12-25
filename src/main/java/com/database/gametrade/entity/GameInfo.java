package com.database.gametrade.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "game_info")
public class GameInfo {

    @Id
    @Column(name = "game_name", length = 100, nullable = false)
    @NotBlank(message = "游戏名不能为空")
    @Size(max = 100, message = "游戏名长度不能超过100个字符")
    private String gameName;

    @Column(name = "category", length = 50, nullable = false)
    @NotBlank(message = "游戏类别不能为空")
    @Size(max = 50, message = "游戏类别长度不能超过50个字符")
    private String category;

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    @DecimalMin(value = "0.0", message = "价格不能为负数")
    private BigDecimal price;

    @Column(name = "company_name", length = 100, nullable = false)
    @NotBlank(message = "企业名不能为空")
    @Size(max = 100, message = "企业名长度不能超过100个字符")
    private String companyName;

    @Column(name = "release_time")
    private LocalDate releaseTime;

    @Column(name = "description", length = 500, nullable = false)
    @NotBlank(message = "游戏简介不能为空")
    @Size(max = 500, message = "游戏简介长度不能超过500个字符")
    private String description;

    @Column(name = "status", length = 20, nullable = false)
    @NotBlank(message = "状态不能为空")
    @Size(max = 20, message = "状态长度不能超过20个字符")
    private String status = "下架";

    @Column(name = "download_link", nullable = false)
    @NotBlank(message = "下载链接不能为空")
    @Size(max = 255, message = "下载链接长度不能超过255个字符")
    private String downloadLink;

    @Column(name = "license_number", length = 50, nullable = false)
    @NotBlank(message = "版号不能为空")
    @Size(max = 50, message = "版号长度不能超过50个字符")
    private String licenseNumber;

    @Column(name = "score", precision = 2, scale = 1)
    private BigDecimal score;

    @Column(name = "sales_volume", nullable = false)
    private Integer salesVolume = 0;

    @Column(name = "visitor_count", nullable = false)
    private Integer visitorCount = 0;

    // 与VendorInfo的外键关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_name", referencedColumnName = "company_name", insertable = false, updatable = false)
    private VendorInfo vendorInfo;

    // 默认构造函数
    public GameInfo() {}

    // 带参数的构造函数
    public GameInfo(String gameName, String category, BigDecimal price, String companyName,
                    String description, String downloadLink, String licenseNumber) {
        this.gameName = gameName;
        this.category = category;
        this.price = price;
        this.companyName = companyName;
        this.description = description;
        this.downloadLink = downloadLink;
        this.licenseNumber = licenseNumber;
        this.status = "下架";
        this.salesVolume = 0;
        this.visitorCount = 0;
    }

    // Getters and Setters
    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public LocalDate getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(LocalDate releaseTime) {
        this.releaseTime = releaseTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public Integer getSalesVolume() {
        return salesVolume;
    }

    public void setSalesVolume(Integer salesVolume) {
        this.salesVolume = salesVolume;
    }

    public Integer getVisitorCount() {
        return visitorCount;
    }

    public void setVisitorCount(Integer visitorCount) {
        this.visitorCount = visitorCount;
    }

    public VendorInfo getVendorInfo() {
        return vendorInfo;
    }

    public void setVendorInfo(VendorInfo vendorInfo) {
        this.vendorInfo = vendorInfo;
    }

    @Override
    public String toString() {
        return "GameInfo{" +
                "gameName='" + gameName + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                ", companyName='" + companyName + '\'' +
                ", releaseTime=" + releaseTime +
                ", status='" + status + '\'' +
                ", licenseNumber='" + licenseNumber + '\'' +
                ", score=" + score +
                ", salesVolume=" + salesVolume +
                ", visitorCount=" + visitorCount +
                '}';
    }
}
