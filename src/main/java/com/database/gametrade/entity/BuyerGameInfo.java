package com.database.gametrade.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "buyer_game_info", uniqueConstraints = {
        @UniqueConstraint(columnNames = "license_number")
})
@IdClass(BuyerGameInfo.BuyerGameInfoId.class)
public class BuyerGameInfo {

    @Id
    @Column(name = "nickname", length = 50, nullable = false)
    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    @Id
    @Column(name = "game_name", length = 100, nullable = false)
    @NotBlank(message = "游戏名不能为空")
    @Size(max = 100, message = "游戏名长度不能超过100个字符")
    private String gameName;

    @Column(name = "license_number", length = 50, nullable = false)
    @NotBlank(message = "版号不能为空")
    @Size(max = 50, message = "版号长度不能超过50个字符")
    private String licenseNumber;

    @Column(name = "score", precision = 2, scale = 1, nullable = false)
    @DecimalMin(value = "0.0", message = "评分不能小于0")
    @DecimalMax(value = "10.0", message = "评分不能大于10")
    private BigDecimal score;

    @Column(name = "comment", length = 200)
    @Size(max = 200, message = "评论长度不能超过200个字符")
    private String comment;

    @Column(name = "review_time")
    private LocalDateTime reviewTime;

    // 与BuyerInfo的外键关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nickname", referencedColumnName = "nickname", insertable = false, updatable = false)
    private BuyerInfo buyerInfo;

    // 与GameInfo的外键关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_name", referencedColumnName = "game_name", insertable = false, updatable = false)
    private GameInfo gameInfo;

    // 默认构造函数
    public BuyerGameInfo() {}

    // 带参数的构造函数
    public BuyerGameInfo(String nickname, String gameName, String licenseNumber, BigDecimal score) {
        this.nickname = nickname;
        this.gameName = gameName;
        this.licenseNumber = licenseNumber;
        this.score = score;
        this.reviewTime = LocalDateTime.now();
    }

    public BuyerGameInfo(String nickname, String gameName, String licenseNumber, BigDecimal score, String comment) {
        this.nickname = nickname;
        this.gameName = gameName;
        this.licenseNumber = licenseNumber;
        this.score = score;
        this.comment = comment;
        this.reviewTime = LocalDateTime.now();
    }

    // Getters and Setters
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(LocalDateTime reviewTime) {
        this.reviewTime = reviewTime;
    }

    public BuyerInfo getBuyerInfo() {
        return buyerInfo;
    }

    public void setBuyerInfo(BuyerInfo buyerInfo) {
        this.buyerInfo = buyerInfo;
    }

    public GameInfo getGameInfo() {
        return gameInfo;
    }

    public void setGameInfo(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

    @Override
    public String toString() {
        return "BuyerGameInfo{" +
                "nickname='" + nickname + '\'' +
                ", gameName='" + gameName + '\'' +
                ", licenseNumber='" + licenseNumber + '\'' +
                ", score=" + score +
                ", comment='" + comment + '\'' +
                ", reviewTime=" + reviewTime +
                '}';
    }

    // 复合主键类
    public static class BuyerGameInfoId implements Serializable {
        private String nickname;
        private String gameName;

        public BuyerGameInfoId() {}

        public BuyerGameInfoId(String nickname, String gameName) {
            this.nickname = nickname;
            this.gameName = gameName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            BuyerGameInfoId that = (BuyerGameInfoId) o;
            return Objects.equals(nickname, that.nickname) &&
                    Objects.equals(gameName, that.gameName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(nickname, gameName);
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getGameName() {
            return gameName;
        }

        public void setGameName(String gameName) {
            this.gameName = gameName;
        }
    }
}
