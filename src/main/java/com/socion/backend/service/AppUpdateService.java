package com.socion.backend.service;

import com.socion.backend.dto.AppUpdateDto;


public interface AppUpdateService {
    AppUpdateDto appupdate(String appVersion, String appName, String appType);
}
