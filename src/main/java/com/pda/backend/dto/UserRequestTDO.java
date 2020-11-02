package com.pda.backend.dto;

import java.util.List;

public class UserRequestTDO {

    List<String> userIds;

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }
}
