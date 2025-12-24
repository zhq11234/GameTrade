package com.database.gametrade.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_info")
public class OrderInfo {

    @Id
    @Column(name = "order_id", length = 50, nullable = false)
    @NotBlank(message = "订单编号不能为空")
    @Size(max = 50, message = "订单编号长度不能超过50个字符")
    private String orderId;

    @Column(name = "nickname", length = 50, nullable = false)
    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    @Column(name = "game_name", length = 100, nullable = false)
    @NotBlank(message = "游戏名不能为空")
    @Size(max = 100, message = "游戏名长度不能超过100个字符")
    private String gameName;

    @Column(name = "category", length = 50)
    @Size(max = 50, message = "游戏类别长度不能超过50个字符")
    private String category;

    @Column(name = "price", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "价格不能为负数")
    private BigDecimal price;

    @Column(name = "order_time", nullable = false)
    private LocalDateTime orderTime;

    @Column(name = "payment_time")
    private LocalDateTime paymentTime;

    @Column(name = "order_status", length = 20, nullable = false)
    @NotBlank(message = "订单状态不能为空")
    @Size(max = 20, message = "订单状态长度不能超过20个字符")
    private String orderStatus = "待支付";

    // 与BuyerInfo的外键关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nickname", referencedColumnName = "nickname", insertable = false, updatable = false)
    private BuyerInfo buyerInfo;

    // 与GameInfo的外键关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_name", referencedColumnName = "game_name", insertable = false, updatable = false)
    private GameInfo gameInfo;

    // 默认构造函数
    public OrderInfo() {
        this.orderTime = LocalDateTime.now();
    }

    // 带参数的构造函数
    public OrderInfo(String orderId, String nickname, String gameName) {
        this.orderId = orderId;
        this.nickname = nickname;
        this.gameName = gameName;
        this.orderStatus = "待支付";
        this.orderTime = LocalDateTime.now();
    }

    public OrderInfo(String orderId, String nickname, String gameName, String category, BigDecimal price) {
        this.orderId = orderId;
        this.nickname = nickname;
        this.gameName = gameName;
        this.category = category;
        this.price = price;
        this.orderStatus = "待支付";
        this.orderTime = LocalDateTime.now();
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus != null ? orderStatus : "待支付";
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
        return "OrderInfo{" +
                "orderId='" + orderId + '\'' +
                ", nickname='" + nickname + '\'' +
                ", gameName='" + gameName + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                ", orderTime=" + orderTime +
                ", paymentTime=" + paymentTime +
                ", orderStatus='" + orderStatus + '\'' +
                '}';
    }
}
