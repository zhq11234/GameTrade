package com.database.gametrade.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "browse_history")
@IdClass(BrowseHistory.BrowseHistoryId.class)
public class BrowseHistory {

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

    @Column(name = "browse_count", nullable = false)
    private Integer browseCount = 0;

    // 与BuyerInfo的外键关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nickname", referencedColumnName = "nickname", insertable = false, updatable = false)
    private BuyerInfo buyerInfo;

    // 与GameInfo的外键关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_name", referencedColumnName = "game_name", insertable = false, updatable = false)
    private GameInfo gameInfo;

    // 默认构造函数
    public BrowseHistory() {}

    // 带参数的构造函数
    public BrowseHistory(String nickname, String gameName) {
        this.nickname = nickname;
        this.gameName = gameName;
        this.browseCount = 0;
    }

    public BrowseHistory(String nickname, String gameName, Integer browseCount) {
        this.nickname = nickname;
        this.gameName = gameName;
        this.browseCount = browseCount != null ? browseCount : 0;
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

    public Integer getBrowseCount() {
        return browseCount;
    }

    public void setBrowseCount(Integer browseCount) {
        this.browseCount = browseCount != null ? browseCount : 0;
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
        return "BrowseHistory{" +
                "nickname='" + nickname + '\'' +
                ", gameName='" + gameName + '\'' +
                ", browseCount=" + browseCount +
                '}';
    }

    // 复合主键类
    public static class BrowseHistoryId implements Serializable {
        private String nickname;
        private String gameName;

        public BrowseHistoryId() {}

        public BrowseHistoryId(String nickname, String gameName) {
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
            BrowseHistoryId that = (BrowseHistoryId) o;
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
