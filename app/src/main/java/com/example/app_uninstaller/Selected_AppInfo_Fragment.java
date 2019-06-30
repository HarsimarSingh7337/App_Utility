package com.example.app_uninstaller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

public class Selected_AppInfo_Fragment extends DialogFragment {

    private String apkPath="";
    private TextView appName;
    private final String fileBackupPath = Environment.getExternalStorageDirectory()+"/App_Utility";
    private final String fileSharePath = Environment.getExternalStorageDirectory()+"/App_Utility/sharedapk";
    private AlertDialog alertDialog;
    private ShareAPK shareAPK;
    private BackupAPK backupAPK;
    private String packageName="";
    private RefreshAppList refreshAppList;
    private LinearLayout secondContainer;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof RefreshAppList){
            refreshAppList = (RefreshAppList) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        refreshAppList = null;
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        assert dialog != null;
        Objects.requireNonNull(getDialog().getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(Objects.requireNonNull(getActivity()).getResources().getDrawable(R.drawable.dialog_fragment_bottom_round_corners));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.fragment_selected__app_info_, container, false);

        Typeface ubuntu_medium = Typeface.createFromAsset(Objects.requireNonNull(getActivity()).getAssets(),"fonts/Ubuntu-Medium.ttf");
        Typeface comfortaa_bold = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Comfortaa-Bold.ttf");

        assert getArguments() != null;
        apkPath = getArguments().getString("app_dir");
        packageName = getArguments().getString("package_name");

        appName = view.findViewById(R.id.app_name);
        ImageView appIcon = view.findViewById(R.id.app_icon);
        Button backup = view.findViewById(R.id.btn_backup);
        Button uninstall = view.findViewById(R.id.btn_uninstall);
        Button share = view.findViewById(R.id.btn_share);
        TextView label_app_version = view.findViewById(R.id.label_app_version);
        TextView label_app_size = view.findViewById(R.id.label_app_size);
        TextView label_app_firstIntallTime = view.findViewById(R.id.label_app_firstInstallTime);
        TextView label_app_lastUpdateTime = view.findViewById(R.id.label_app_lastUpdated);
        TextView value_app_version = view.findViewById(R.id.value_app_version);
        TextView value_app_size = view.findViewById(R.id.value_app_size);
        TextView value_app_firstIntallTime = view.findViewById(R.id.value_app_firstInstallTime);
        TextView value_app_lastUpdateTime = view.findViewById(R.id.value_app_lastUpdated);
        Button btn_show_more = view.findViewById(R.id.btn_show_more);
        secondContainer = view.findViewById(R.id.secondContainer);

        appName.setTypeface(ubuntu_medium);
        share.setTypeface(comfortaa_bold);
        backup.setTypeface(comfortaa_bold);
        uninstall.setTypeface(comfortaa_bold);

        label_app_version.setTypeface(comfortaa_bold);
        label_app_size.setTypeface(comfortaa_bold);
        label_app_firstIntallTime.setTypeface(comfortaa_bold);
        label_app_lastUpdateTime.setTypeface(comfortaa_bold);

        value_app_version.setTypeface(comfortaa_bold);
        value_app_size.setTypeface(comfortaa_bold);
        value_app_firstIntallTime.setTypeface(comfortaa_bold);
        value_app_lastUpdateTime.setTypeface(comfortaa_bold);

        value_app_version.setText("  "+getArguments().getString("version_name"));
        value_app_size.setText("  "+getArguments().getString("app_size"));
        value_app_firstIntallTime.setText("  "+getArguments().getString("firstInstallTime"));
        value_app_lastUpdateTime.setText("  "+getArguments().getString("lastUpdateTime"));

        //value_app_version.setTypeface(comfortaa_bold);

        appName.setText(getArguments().getString("app_name"));

        Bitmap bitmap = BitmapFactory.decodeByteArray(getArguments().getByteArray("app_icon"),0, Objects.requireNonNull(getArguments().getByteArray("app_icon")).length);

        appIcon.setImageBitmap(bitmap);

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!hasReadWritePermission()){
                        Objects.requireNonNull(getActivity()).requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},101);
                    }
                    else{
                        shareFile();
                    }
                }
                else{
                    shareFile();
                }
            }
        });

        backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(!hasReadWritePermission()){
                        Objects.requireNonNull(getActivity()).requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},102);
                    }
                    else{
                        backupFile();
                    }
                }
                else{
                    backupFile();
                }
            }
        });

        uninstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Uri uri = Uri.parse("package:"+packageName);
                Intent intent=new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
                intent.setData(uri);
                intent.putExtra(Intent.EXTRA_RETURN_RESULT,true);
                startActivityForResult(intent,201);
            }
        });

        btn_show_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.GONE);
                secondContainer.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean hasReadWritePermission(){
        return Objects.requireNonNull(getActivity()).checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case 101:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    shareFile();
                }
                break;

            case 102:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    backupFile();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == 201){
            if(resultCode == Activity.RESULT_OK){
                Toast.makeText(getActivity(), "Uninstalled successfully", Toast.LENGTH_LONG).show();
                refreshAppList.refreshList();
                Objects.requireNonNull(getDialog()).dismiss();
            }
            if(resultCode == Activity.RESULT_CANCELED){
            }
        }
    }

    private void shareFile(){
        shareAPK = new ShareAPK();
        shareAPK.execute(fileSharePath,apkPath,appName.getText().toString());
    }

    private void backupFile(){
        backupAPK = new BackupAPK();
        backupAPK.execute(fileBackupPath,apkPath,appName.getText().toString());
    }

    @SuppressLint("StaticFieldLeak")
    class ShareAPK extends AsyncTask<String, String, File> {

        @Override
        protected File doInBackground(String... filepath) {

            File originalApk = new File(filepath[1]);
            File tempFile = new File(filepath[0]);

            try{
                if(!tempFile.isDirectory()){
                    tempFile.mkdirs();
                }

                tempFile = new File(tempFile.getCanonicalPath()+ "/" +filepath[2].toLowerCase()+".apk");
                /*if(!tempFile.exists()){
                    if(!tempFile.createNewFile()){
                        return null;
                    }
                }*/
                InputStream in = new FileInputStream(originalApk);
                OutputStream out = new FileOutputStream(tempFile);

                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            }

            catch(IOException e){
                Log.e("Exception: ",e.getMessage());
            }
            return tempFile;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.progress_bar,null);
            AlertDialog.Builder ab=new AlertDialog.Builder(getActivity());
            ab.setView(view);
            ab.setCancelable(false);
            alertDialog = ab.create();
            alertDialog.show();
        }

        @Override
        protected void onPostExecute(File s) {
            super.onPostExecute(s);

            if(alertDialog!=null && alertDialog.isShowing()){
                alertDialog.dismiss();
            }

            try{
                Intent intent=new Intent(Intent.ACTION_SEND);
                intent.setType("application/vnd.android.package-archive");
                Uri photoUri = FileProvider.getUriForFile(Objects.requireNonNull(getActivity()),BuildConfig.APPLICATION_ID +".provider",s);
                intent.putExtra(Intent.EXTRA_STREAM, photoUri);

                PackageManager packageManager=getActivity().getPackageManager();
                if(packageManager.resolveActivity(intent,0) !=null ){
                    startActivity(Intent.createChooser(intent, "Share app via"));
                }
            }
            catch(Exception e){
                AlertDialog.Builder ab=new AlertDialog.Builder(getActivity());
                ab.setMessage(e.getMessage());
                ab.show();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    class BackupAPK extends AsyncTask<String,String,File>{

        @Override
        protected File doInBackground(String... filepath) {

            File tempFile=null;
            File originalApk = new File(filepath[1]);

            try{
                tempFile = new File(filepath[0]);
                if(!tempFile.isDirectory()){
                    tempFile.mkdirs();
                    return null;
                }

                tempFile = new File(tempFile.getCanonicalPath()+ "/" +filepath[2].toLowerCase()+".apk");
                if(!tempFile.exists()){
                    if(!tempFile.createNewFile()){
                        return null;
                    }
                }

                InputStream in = new FileInputStream(originalApk);
                OutputStream out = new FileOutputStream(tempFile);

                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                in.close();
                out.close();
            }
            catch(IOException e){
                Log.e("Exception: ",e.getMessage());
            }
            return tempFile;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            View view = LayoutInflater.from(getActivity()).inflate(R.layout.progress_bar,null);
            TextView message = view.findViewById(R.id.progress_bar_text);
            message.setText("Backup in Progress...");
            AlertDialog.Builder ab=new AlertDialog.Builder(getActivity());
            ab.setView(view);
            ab.setCancelable(false);
            alertDialog = ab.create();
            alertDialog.show();
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);

            if(alertDialog!=null && alertDialog.isShowing()){
                alertDialog.dismiss();
            }
            try{
                String fpath = "Internal Storage/App_Utility/"+appName.getText().toString().toLowerCase()+".apk";
                AlertDialog.Builder ab=new AlertDialog.Builder(getActivity());
                ab.setTitle("Backup Successful");
                ab.setMessage("Locate file at: "+"\n"+"\n"+fpath );
                ab.setCancelable(false);
                ab.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                ab.show();
            }
            catch(Exception ignored){ }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(shareAPK!=null && !shareAPK.isCancelled()){
            shareAPK.cancel(true);
        }

        if(backupAPK!=null && !backupAPK.isCancelled()){
            backupAPK.cancel(true);
        }

        File f = new File(fileSharePath);
        if(f.exists()){

            if(f.list().length==0){
                f.delete();
            }

            String files[] = f.list();
            for(String temp : files){
                File delFile = new File(f,temp);
                delFile.delete();
            }
            // again checking if directory is empty
            if(f.list().length==0){
                f.delete();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(alertDialog!=null && alertDialog.isShowing()){
            alertDialog.dismiss();
        }
    }

    public interface RefreshAppList{
        void refreshList();
    }
}