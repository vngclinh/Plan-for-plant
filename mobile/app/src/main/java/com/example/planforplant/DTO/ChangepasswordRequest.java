package com.example.planforplant.DTO;

public class ChangepasswordRequest {
    private String oldPassword;
    private String newPassword;

    public void setOldPassword(String currentPassword) {
        this.oldPassword=currentPassword;
    }

    public void setNewPassword(String newPassword) {
         this.newPassword = newPassword;
    }
}
