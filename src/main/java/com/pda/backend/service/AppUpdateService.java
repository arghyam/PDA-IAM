package com.pda.backend.service;

import com.pda.backend.dto.AppUpdateDto;


public interface AppUpdateService {
    AppUpdateDto appupdate(String appVersion, String appName, String appType);
}
