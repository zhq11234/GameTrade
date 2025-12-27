package com.database.gametrade.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
public class OrderResponseDTO {
    private String orderId;
    private String buyerNickname;
    private String gameName;
    private String category;
    private BigDecimal price;
    private LocalDateTime orderTime;
    private LocalDateTime paymentTime;
    private String orderStatus;
    
    public OrderResponseDTO() {}
    
    public OrderResponseDTO(String orderId, String buyerNickname, String gameName, String category, 
                           BigDecimal price, LocalDateTime orderTime, LocalDateTime paymentTime, 
                           String orderStatus) {
        this.orderId = orderId;
        this.buyerNickname = buyerNickname;
        this.gameName = gameName;
        this.category = category;
        this.price = price;
        this.orderTime = orderTime;
        this.paymentTime = paymentTime;
        this.orderStatus = orderStatus;
    }
}