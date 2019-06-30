package com.example.app_uninstaller;


import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.text.DecimalFormat;

import androidx.fragment.app.DialogFragment;

import static android.content.Context.ACTIVITY_SERVICE;

public class Memory_Storage_Usage extends DialogFragment {


    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getDialog().getWindow().setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.dialog_fragment_round_corners));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View rootView = inflater.inflate(R.layout.fragment_memory__storage__usage, container, false);

        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Ubuntu-Medium.ttf");
        Typeface comfortaa_bold = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Comfortaa-Bold.ttf");

        TextView label_storage = rootView.findViewById(R.id.label_storage);
        TextView total_storage_label = rootView.findViewById(R.id.total_storage_label);
        TextView total_storage_value = rootView.findViewById(R.id.total_storage_value);
        TextView used_storage_label = rootView.findViewById(R.id.used_storage_label);
        TextView used_storage_value = rootView.findViewById(R.id.used_storage_value);
        TextView available_storage_label = rootView.findViewById(R.id.available_storage_label);
        TextView available_storage_value = rootView.findViewById(R.id.available_storage_value);

        TextView label_memory = rootView.findViewById(R.id.label_memory);
        TextView total_memory_label = rootView.findViewById(R.id.total_memory_label);
        TextView total_memory_value = rootView.findViewById(R.id.total_memory_value);
        TextView used_memory_label = rootView.findViewById(R.id.used_memory_label);
        TextView used_memory_value = rootView.findViewById(R.id.used_memory_value);
        TextView available_memory_label = rootView.findViewById(R.id.available_memory_label);
        TextView available_memory_value = rootView.findViewById(R.id.available_memory_value);

        ProgressBar memoryUsageProgressBar = rootView.findViewById(R.id.progress_bar_memory_usage);
        ProgressBar storageusageProgressBar = rootView.findViewById(R.id.progress_bar_storage_usage);

        TextView progressPercentageStorage = rootView.findViewById(R.id.progress_percentage_storage);
        TextView progressPercentageMemory = rootView.findViewById(R.id.progress_percentage_memory);

        total_storage_label.setTypeface(comfortaa_bold);
        total_storage_value.setTypeface(comfortaa_bold);
        used_storage_label.setTypeface(comfortaa_bold);
        used_storage_value.setTypeface(comfortaa_bold);
        available_storage_label.setTypeface(comfortaa_bold);
        available_storage_value.setTypeface(comfortaa_bold);

        total_memory_label.setTypeface(comfortaa_bold);
        total_memory_value.setTypeface(comfortaa_bold);
        used_memory_label.setTypeface(comfortaa_bold);
        used_memory_value.setTypeface(comfortaa_bold);
        available_memory_label.setTypeface(comfortaa_bold);
        available_memory_value.setTypeface(comfortaa_bold);

        label_storage.setTypeface(typeface);
        label_memory.setTypeface(typeface);

        // decimal format class to format storage size for given pattern
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        //displaying Storage related data
        double freeInternalSpaceInDouble = getAvailableInternalStorage();
        double totalInternalSpaceInDouble = getTotalInternalStorage();
        double usedInternalSpaceInDouble = totalInternalSpaceInDouble - freeInternalSpaceInDouble;

        // converting double into long
        long totalStorageAsLong = Math.round(totalInternalSpaceInDouble);
        long usedStorageAsLong = Math.round(usedInternalSpaceInDouble);

        // breaking a long number into 100 parts and doing progress calculations for storage
        long divisor = totalStorageAsLong /100;
        int progressValue = (int) (usedStorageAsLong / divisor);

        // setting progress bar value for storage
        storageusageProgressBar.setIndeterminate(false);
        storageusageProgressBar.setMax(100);
        storageusageProgressBar.setProgress(progressValue);

        // setting percentage value above progress bar
        double percentageNumber = (double) usedStorageAsLong / totalStorageAsLong;
        percentageNumber = percentageNumber*100;
        progressPercentageStorage.setText(String.valueOf(Math.round(percentageNumber))+"%");
        progressPercentageStorage.setTypeface(comfortaa_bold);

        // storing storage values into array
        double[] internalSpaceArray = new double[3];
        internalSpaceArray[0] = usedInternalSpaceInDouble;
        internalSpaceArray[1] = freeInternalSpaceInDouble;
        internalSpaceArray[2] = totalInternalSpaceInDouble;

        // traversing array
        for (int i = 0; i < internalSpaceArray.length; i++) {

            // string fields to store final storage size into MB, GB or TB unit
            String usedSize = "";
            String totalSize = "";
            String availableSize="";

            double m = ((internalSpaceArray[i] / 1024.0) / 1024.0);
            double g = (((internalSpaceArray[i] / 1024.0) / 1024.0) / 1024.0);
            double t = ((((internalSpaceArray[i] / 1024.0) / 1024.0) / 1024.0) / 1024.0);

            // used Internal Space
            if (i == 0) {
                if (t > 1) {
                    usedSize = decimalFormat.format(t).concat(" TB");
                } else if (g > 1) {
                    usedSize = decimalFormat.format(g).concat(" GB");
                } else if (m > 1) {
                    usedSize = decimalFormat.format(m).concat(" MB");
                }

                used_storage_value.setText(usedSize);
            }

            // available internal storage
            else if(i==1){
                if (t > 1) {
                    availableSize = decimalFormat.format(t).concat(" TB");
                } else if (g > 1) {
                    availableSize = decimalFormat.format(g).concat(" GB");
                } else if (m > 1) {
                    availableSize = decimalFormat.format(m).concat(" MB");
                }
                available_storage_value.setText(availableSize);
            }

            // total internal storage
            else {
                if (t > 1) {
                    totalSize = decimalFormat.format(t).concat(" TB");
                } else if (g > 1) {
                    totalSize = decimalFormat.format(g).concat(" GB");
                } else if (m > 1) {
                    totalSize = decimalFormat.format(m).concat(" MB");
                }
                total_storage_value.setText(totalSize);
            }
        }

        // calculating and displaying Ram usage
        long totalRamValue = totalRamMemorySize();
        long freeRamValue = freeRamMemorySize();
        long usedRamValue = totalRamValue - freeRamValue;

        double usedRamValueInDouble = (double) usedRamValue;
        double totalRamValueInDouble = (double) totalRamValue;
        double availableRamValueInDouble = (double) freeRamValue;

        // converting double into long
        long totalMemoryAsLong = Math.round(totalRamValueInDouble);
        long usedMemoryAsLong = Math.round(usedRamValueInDouble);

        // breaking a long number into 100 parts and doing progress calculations for storage
        divisor = totalMemoryAsLong /100;
        progressValue = (int) (usedMemoryAsLong / divisor);

        // setting progress bar value for memory
        memoryUsageProgressBar.setMax(100);
        memoryUsageProgressBar.setProgress(progressValue);

        // setting percentage value above progress bar for memory
        percentageNumber = (double) usedMemoryAsLong / totalMemoryAsLong;
        percentageNumber = percentageNumber*100;
        progressPercentageMemory.setText(String.valueOf(Math.round(percentageNumber))+"%");
        progressPercentageMemory.setTypeface(comfortaa_bold);

        // storing ram values into array
        double[] ramArray = new double[3];
        ramArray[0] = usedRamValueInDouble;
        ramArray[1] = availableRamValueInDouble;
        ramArray[2] = totalRamValueInDouble;

        //traversing array of ram values
        for (int i = 0; i < ramArray.length; i++) {
            // string fields to store final storage size into MB, GB or TB unit
            String usedSize = "";
            String totalSize = "";
            String availableSize = "";

            double m = ((ramArray[i] / 1024.0) / 1024.0);
            double g = (((ramArray[i] / 1024.0) / 1024.0) / 1024.0);
            double t = ((((ramArray[i] / 1024.0) / 1024.0) / 1024.0) / 1024.0);

            // used ram
            if (i == 0) {
                if (t > 1) {
                    usedSize = decimalFormat.format(t).concat(" TB");
                } else if (g > 1) {
                    usedSize = decimalFormat.format(g).concat(" GB");
                } else if (m > 1) {
                    usedSize = decimalFormat.format(m).concat(" MB");
                }
                used_memory_value.setText(usedSize);
            }

            // available ram
            else if(i==1){
                if (t > 1) {
                    availableSize = decimalFormat.format(t).concat(" TB");
                } else if (g > 1) {
                    availableSize = decimalFormat.format(g).concat(" GB");
                } else if (m > 1) {
                    availableSize = decimalFormat.format(m).concat(" MB");
                }
                available_memory_value.setText(availableSize);
            }

            // total ram
            else {
                if (t > 1) {
                    totalSize = decimalFormat.format(t).concat(" TB");
                } else if (g > 1) {
                    totalSize = decimalFormat.format(g).concat(" GB");
                } else if (m > 1) {
                    totalSize = decimalFormat.format(m).concat(" MB");
                }
                total_memory_value.setText(totalSize);
            }
        }
        return rootView;
    }

    private double getAvailableInternalStorage(){
        File path = Environment.getExternalStorageDirectory();
        StatFs statFs = new StatFs(path.getAbsolutePath());
        long blockSize = statFs.getBlockSizeLong();
        long availableBlocks = statFs.getAvailableBlocksLong();

        String temp = String.valueOf(availableBlocks * blockSize);
        //Log.e("Avail Storage: ",temp);
       // Log.e("Avail Storage double: ",""+Double.parseDouble(temp));
        return Double.parseDouble(temp);
    }

    private double getTotalInternalStorage(){
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();

        String temp = String.valueOf(totalBlocks * blockSize);
      /*  Log.e("original Storage: ",""+totalBlocks * blockSize);
        Log.e("Total Storage string: ",temp);
        Log.e("Total Storage double: ",""+Double.parseDouble(temp));
        Double numm = Double.parseDouble(temp);
        long num = numm.longValue();
        Log.e("Total Storage Long: ",""+num);*/
        return Double.parseDouble(temp);
    }

    private long totalRamMemorySize() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        long availableMegs = mi.totalMem;
        return availableMegs;
    }

    private long freeRamMemorySize() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        long availableMegs = mi.availMem;
        return availableMegs;
    }
}