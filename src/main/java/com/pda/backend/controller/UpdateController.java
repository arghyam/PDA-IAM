package com.pda.backend.controller;

import com.pda.backend.dto.AppUpdateDto;
import com.pda.backend.service.AppUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "api/v2/user", produces = {"application/json", "application/x-www-form-urlencoded"})
public class UpdateController {
    @Autowired
    AppUpdateService appUpdateService;

    @GetMapping(value = "/versionCheckUpdate")
    public AppUpdateDto appudate(@RequestParam("appVersion") String appVersion,@RequestParam("appName") String appName, @RequestParam("appType") String appType) {
        return appUpdateService.appupdate(appVersion,appName,appType);
    }


}
