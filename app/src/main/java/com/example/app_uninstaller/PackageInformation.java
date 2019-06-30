package com.example.app_uninstaller;

import android.graphics.drawable.Drawable;

public class PackageInformation {

    private String appName="";
    private String pName="";
    private String versionName="";
    private Drawable appIcon=null;
    private String dataDir="";
    private int versionCode = 0;
    private String appSize="";
    private String firstInstallTime="";
    private String lastUpdateTime="";
    private boolean isSelected;

    PackageInformation(){

    }

    PackageInformation(String appName, String pName, String versionName,int versionCode, Drawable appIcon, String dataDir, String appSize, String firstInstallTime, String lastUpdateTime ){

        this.appName = appName;
        this.pName=pName;
        this.versionName=versionName;
        this.appIcon = appIcon;
        this.dataDir = dataDir;
        this.versionCode = versionCode;
        this.appSize=appSize;
        this.firstInstallTime = firstInstallTime;
        this.lastUpdateTime=lastUpdateTime;
    }

    public String getAppName() {
        return appName;
    }

    public String getVersionName() {
        return versionName;
    }

    public int getVersionCode(){ return this.versionCode; }

    public String getpName(){ return this.pName; }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public String getDataDir(){ return dataDir; }

    public String getAppSize(){
        return appSize;
    }

    public String getFirstInstallTime() { return firstInstallTime; }

    public String getLastUpdateTime() { return lastUpdateTime; }

    public boolean isSelected() {
        return isSelected;
    }
    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
