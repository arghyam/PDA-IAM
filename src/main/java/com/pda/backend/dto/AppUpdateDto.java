package com.pda.backend.dto;

public class AppUpdateDto {
    boolean forceUpgrade;
    boolean recommendUpgrade;

    public boolean isForceUpgrade() {
        return forceUpgrade;
    }

    public void setForceUpgrade(boolean forceUpgrade) {
        this.forceUpgrade = forceUpgrade;
    }

    public boolean isRecommendUpgrade() {
        return recommendUpgrade;
    }

    public void setRecommendUpgrade(boolean recommendUpgrade) {
        this.recommendUpgrade = recommendUpgrade;
    }


}
