package com.database.gametrade.dto;

public class VendorInfoDTO {
    private String companyName;
    private String account;
    private String registeredAddress;
    private String contactPerson;
    private String contact;

    public VendorInfoDTO() {}

    public VendorInfoDTO(String companyName, String account, String registeredAddress, String contactPerson, String contact) {
        this.companyName = companyName;
        this.account = account;
        this.registeredAddress = registeredAddress;
        this.contactPerson = contactPerson;
        this.contact = contact;
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

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    @Override
    public String toString() {
        return "VendorInfoDTO{" +
                "companyName='" + companyName + '\'' +
                ", account='" + account + '\'' +
                ", registeredAddress='" + registeredAddress + '\'' +
                ", contactPerson='" + contactPerson + '\'' +
                ", contact='" + contact + '\'' +
                '}';
    }
}
