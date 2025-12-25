package com.database.gametrade.dto;

import java.time.LocalDate;

public class BuyerInfoDTO {
    private String nickname;
    private String account;
    private String gender;
    private LocalDate birthday;
    private String contact;

    public BuyerInfoDTO() {}

    public BuyerInfoDTO(String nickname, String account, String gender, LocalDate birthday, String contact) {
        this.nickname = nickname;
        this.account = account;
        this.gender = gender;
        this.birthday = birthday;
        this.contact = contact;
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
        this.gender = gender;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    @Override
    public String toString() {
        return "BuyerInfoDTO{" +
                "nickname='" + nickname + '\'' +
                ", account='" + account + '\'' +
                ", gender='" + gender + '\'' +
                ", birthday=" + birthday +
                ", contact='" + contact + '\'' +
                '}';
    }
}
