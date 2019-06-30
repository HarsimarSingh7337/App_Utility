package com.example.app_uninstaller;

public class Selected_Apks {

    private int selectedPositionInRecyclerView;
    private String apkPath;
    private String appName;


    Selected_Apks() { }

    Selected_Apks(int pos, String path,String appName){
        this.selectedPositionInRecyclerView=pos;
        this.apkPath=path;
        this.appName=appName;
    }

    public int getSelectedPositionInRecyclerView() {
        return selectedPositionInRecyclerView;
    }

    public void setSelectedPositionInRecyclerView(int selectedPositionInRecyclerView) {
        this.selectedPositionInRecyclerView = selectedPositionInRecyclerView;
    }

    public String getApkPath() {
        return apkPath;
    }

    public void setApkPath(String apkPath) {
        this.apkPath = apkPath;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
}
