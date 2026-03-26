package com.tradex.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SendMobileCodeRequest {

    @NotBlank @Pattern(regexp = "\\d{10}")
    private String newMobile;

    public String getNewMobile() { return newMobile; }
    public void setNewMobile(String newMobile) { this.newMobile = newMobile; }
}
