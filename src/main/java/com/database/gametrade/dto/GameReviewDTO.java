package com.database.gametrade.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class GameReviewDTO {
    private String nickname;
    private Integer rating;
    private String comment;
    private LocalDateTime reviewTime;

    public GameReviewDTO() {}

    public GameReviewDTO(String nickname, Integer rating, String comment, LocalDateTime reviewTime) {
        this.nickname = nickname;
        this.rating = rating;
        this.comment = comment;
        this.reviewTime = reviewTime;
    }
}
