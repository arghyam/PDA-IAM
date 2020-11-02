package com.pda.backend.dto;


public class NotificationDTO {
    private Long notificationId;
    private String userId;
    private String title;
    private String notificationType;
    private String dateTime;
    private Boolean isRead;

    public NotificationDTO() {
    }

    public NotificationDTO(Long notificationId, String userId, String title, String notificationType, String dateTime,
                           Boolean isRead) {
        super();
        this.notificationId = notificationId;
        this.userId = userId;
        this.title = title;
        this.notificationType = notificationType;
        this.dateTime = dateTime;
        this.isRead = isRead;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }


}