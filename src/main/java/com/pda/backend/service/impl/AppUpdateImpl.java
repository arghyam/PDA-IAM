package com.pda.backend.service.impl;

import com.pda.backend.dto.AppUpdateDto;
import com.pda.backend.config.AppContext;

import com.pda.backend.service.AppUpdateService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Scanner;

@Service
@Component
public class AppUpdateImpl implements AppUpdateService {

    @Autowired
    AppContext appContext;

    public AppUpdateDto appupdate(String appVersion, String appName, String appType){
        AppUpdateDto appUpdateDto=new AppUpdateDto();
        appUpdateDto.setForceUpgrade(false);
        appUpdateDto.setRecommendUpgrade(false);


        String AppversionIosParticipantForced = appContext.getAppversionIosParticipantForced();
        String AppversionIosTrainerForced = appContext.getAppversionIosTrainerForced();
        String AppVersionAndroidParticipantForced = appContext.getAppVersionAndroidParticipantForced();
        String AppVesionAndroidTrainerForced = appContext.getAppVesionAndroidTrainerForced();
        String AppversionIosParticipantReco = appContext.getAppversionIosParticipantReco();
        String AppversionIosTrainerReco = appContext.getAppversionIosTrainerReco();
        String AppVersionAndroidParticipantReco = appContext.getAppVersionAndroidParticipantReco();
        String AppVesionAndroidTrainerReco = appContext.getAppVesionAndroidTrainerReco();
        if(appName.equals("trainer")&& appType.equals("android")) {

            appUpdateDto.setForceUpgrade(compareversion(appVersion, AppVesionAndroidTrainerForced));
            appUpdateDto.setRecommendUpgrade(compareversion(appVersion, AppVesionAndroidTrainerReco));


        }
        else if(appName.equals("participant")&& appType.equals("android")) {

            appUpdateDto.setForceUpgrade(compareversion(appVersion, AppVersionAndroidParticipantForced));
            appUpdateDto.setRecommendUpgrade(compareversion(appVersion, AppVersionAndroidParticipantReco));
        }
        else if(appName.equals("trainer")&& appType.equals("ios")) {

            appUpdateDto.setForceUpgrade(compareversion(appVersion, AppversionIosTrainerForced));
            appUpdateDto.setRecommendUpgrade(compareversion(appVersion, AppversionIosTrainerReco));
        }
        else if(appName.equals("participant")&& appType.equals("ios")) {

            appUpdateDto.setForceUpgrade(compareversion(appVersion, AppversionIosParticipantForced));
            appUpdateDto.setRecommendUpgrade(compareversion(appVersion, AppversionIosParticipantReco));
        }
        return appUpdateDto;
    }
    private boolean compareversion(String appVersion,String appVersionsaved){
        try (Scanner appversion = new Scanner(appVersion);

             Scanner appversionsaved = new Scanner(appVersionsaved);) {
            appversion.useDelimiter("\\.");
            appversionsaved.useDelimiter("\\.");

            while (appversion.hasNextInt() && appversionsaved.hasNextInt()) {
                int v1 = appversion.nextInt();
                int v2 = appversionsaved.nextInt();
                if (v1 < v2) {
                    return true;
                } else if (v1 > v2) {
                    return false;
                }
            }

            if (appversion.hasNextInt() && appversion.nextInt() != 0)
                return false;
            if (appversionsaved.hasNextInt() && appversionsaved.nextInt() != 0)
                return true;

            return false;
        }
    }

}
