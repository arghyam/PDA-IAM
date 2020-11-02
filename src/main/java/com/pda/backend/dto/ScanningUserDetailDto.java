package com.pda.backend.dto;

public class ScanningUserDetailDto {
    private String userId;
    private String name;
    private String photoUrl;

    public ScanningUserDetailDto() {
    }

    public ScanningUserDetailDto(String userId, String name, String photoUrl) {
        this.userId = userId;
        this.name = name;
        this.photoUrl = photoUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
