package com.socion.backend.dto;

public class ProfileTemplateDto {
    private String userId;
    private String userName;
    private String photo;

    public ProfileTemplateDto(String userId, String userName, String photo) {
        this.userId = userId;
        this.userName = userName;
        this.photo = photo;
    }

    public ProfileTemplateDto() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
