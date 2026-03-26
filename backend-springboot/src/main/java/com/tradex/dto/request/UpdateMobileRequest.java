package com.tradex.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UpdateMobileRequest {

    @NotBlank
    private String currentMobile;

    @NotBlank @Pattern(regexp = "\\d{10}")
    private String newMobile;

    @NotBlank
    private String verificationCode;

    public String getCurrentMobile() { return currentMobile; }
    public void setCurrentMobile(String currentMobile) { this.currentMobile = currentMobile; }

    public String getNewMobile() { return newMobile; }
    public void setNewMobile(String newMobile) { this.newMobile = newMobile; }

    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }
}
