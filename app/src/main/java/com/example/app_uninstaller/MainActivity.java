package com.example.app_uninstaller;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity implements Selected_AppInfo_Fragment.RefreshAppList /*implements NavigationView.OnNavigationItemSelectedListener*/ {

    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;

    // ArrayList of type Custom Class to store App information
    private ArrayList<PackageInformation> list=new ArrayList<>();

    private SwipeRefreshLayout swipeRefreshLayout;

    // Searchview being used in Toolbar
    private SearchView searchView;

    private CoordinatorLayout coordinatorLayout;

    // Alert Dialog being used in AsyncTask class
    private Dialog alertDialog;

    // variable to store app version
    private String appVersionNameForToolbar="";

    // Arraylist to store multiple items for bulk sharing
    private ArrayList<Selected_Apks> selectedItemInList =new ArrayList<>();

    // Path into Internal Storax`x`ge for backing up apps for sharing
    private final String fileSharePath = Environment.getExternalStorageDirectory()+"/App_Utility/sharedapk";

    // int variable to store clicked view's position from recycler view
    private int selectedItem =-1;

    // shared preference to store recycler view layout manager Grid or list
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = getSharedPreferences("MyPrefs", 0);

        // getting app version from PackageManager Class by providing it current app's package name.
        try {
            PackageInfo packageInformation = getPackageManager().getPackageInfo(getPackageName(),0);
            appVersionNameForToolbar = packageInformation.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // setting title and subtitle into Action Bar
        Objects.requireNonNull(getSupportActionBar()).setTitle("App Utility");
        getSupportActionBar().setSubtitle("version "+appVersionNameForToolbar);

        coordinatorLayout = findViewById(R.id.coordinator_layout);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);

        // swipe to refresh functionality below
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                list.clear();
                prepareList();
                selectedItemInList.clear();
                Objects.requireNonNull(getSupportActionBar()).setTitle("App Utility");
                getSupportActionBar().setSubtitle("version "+appVersionNameForToolbar);
                invalidateOptionsMenu();
                Toast.makeText(getApplicationContext(), "Apps reloaded", Toast.LENGTH_SHORT).show();

                if(!mRecyclerView.isComputingLayout()){
                    mAdapter.notifyDataSetChanged();
                }
            }
        });

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        try{
            if(Objects.requireNonNull(sharedPreferences.getString("layoutManagerType", null)).equalsIgnoreCase("grid")){
                RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
                mRecyclerView.setLayoutManager(layoutManager);
            }
            else{
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
                mRecyclerView.setLayoutManager(layoutManager);
            }
        }
        catch(NullPointerException npe){
            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
            mRecyclerView.setLayoutManager(layoutManager);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("layoutManagerType","grid");
            editor.apply();
        }

        mAdapter = new MyAdapter(list);
        mRecyclerView.setAdapter(mAdapter);

        // fetching installed 3rd party app's information and displaying it in recycler view
        prepareList();
    }

    // Custom interface's method to refresh installed app list
    // will only execute when an app gets uninstalled
    @Override
    public void refreshList() {
        list.clear();
        prepareList();
        if(!mRecyclerView.isComputingLayout()){
            mAdapter.notifyDataSetChanged();
        }
    }

    // Custom Adapter Class for Recycler View
    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> implements Filterable {

        ArrayList<PackageInformation> list;
        ArrayList<PackageInformation> list1;

         MyAdapter(ArrayList<PackageInformation> list) {
            this.list=list;
            this.list1=list;
        }

        @NonNull
        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // create a new view
            View v;
            if(Objects.requireNonNull(sharedPreferences.getString("layoutManagerType", null)).equalsIgnoreCase("grid")) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_appinfo_grid, parent, false);
            }
            else{
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_appinfo_list, parent, false);
            }
            return new MyViewHolder(v);
        }

        // all data binding done inside this method
        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
            holder.appName.setText(list.get(position).getAppName());
            holder.appSize.setText(list.get(position).getAppSize());
            holder.appIcon.setImageDrawable(list.get(position).getAppIcon());

            // if current item is selected for multiple sharing
            // make its background gray and set it as checked.
            if(list.get(position).isSelected()){
                holder.appCheckBox.setChecked(true);
                holder.appContainer.setBackgroundColor(getResources().getColor(R.color.lightgray));
            }
            else{
                // let all the un-selected items normal as before
                holder.appCheckBox.setChecked(false);
                holder.appContainer.setBackgroundColor(getResources().getColor(android.R.color.white));
            }
        }

        // this method is responsible to make number of views in recycler view as arraylist size
        @Override
        public int getItemCount() {
            return list.size();
        }

        @Override
        public int getItemViewType(int position) {
            if(Objects.requireNonNull(sharedPreferences.getString("layoutManagerType", null)).equalsIgnoreCase("grid")){
              return R.layout.card_appinfo_grid;
             }
             else{
                 return R.layout.card_appinfo_list;
             }
        }

        // filter class implemented to search an item from recycler view.
        @Override
         public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {

                    FilterResults filterResults = new FilterResults();

                    if(constraint.length()==0){
                        filterResults.values = list1;
                        filterResults.count = list1.size();
                    }
                    else{
                        String charString = constraint.toString().toLowerCase();
                        ArrayList<PackageInformation> filterlist = new ArrayList<>();
                        for(int i=0;i<list1.size();i++){
                            if(list1.get(i).getAppName().toLowerCase().contains(charString)){
                                filterlist.add(list1.get(i));
                            }
                        }
                        filterResults.values = filterlist;
                        filterResults.count = filterlist.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    mAdapter.list = (ArrayList<PackageInformation>) results.values;
                    mAdapter.notifyDataSetChanged();
                   // Log.e("Filtered List Size: ",String.valueOf(list.size()));
                }
            };
         }

         // Custom View Holder class
         class MyViewHolder extends RecyclerView.ViewHolder {

            TextView appName,appSize;
            ImageView appIcon;
            LinearLayout appContainer;
            CheckBox appCheckBox;

            MyViewHolder(View v) {
                super(v);

                appName = v.findViewById(R.id.app_name);
                appSize =v.findViewById(R.id.app_size);

                Typeface typeface = Typeface.createFromAsset(getAssets(),"fonts/Ubuntu-Medium.ttf");
                appName.setTypeface(typeface);
                appSize.setTypeface(typeface);

               // appVersionName = v.findViewById(R.id.app_version_name);
                appIcon = v.findViewById(R.id.app_icon);
                appContainer = v.findViewById(R.id.app_container);
                appCheckBox = v.findViewById(R.id.app_checkbox);

                // when user selects an item in recycler view
                // it will open a dialog fragment containing further
                // information corresponding to the app.
                appContainer.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        // storing currently selected item's position
                        int pos = getAdapterPosition();

                        //passing image as byte array output stream to fragment
                        appIcon.buildDrawingCache();
                        Drawable drawable = list.get(pos).getAppIcon();
                        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight(),Bitmap.Config.ARGB_8888);
                        final Canvas canvas = new Canvas(bitmap);
                        drawable.setBounds(0,0,canvas.getWidth(),canvas.getHeight());
                        drawable.draw(canvas);

                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG,50,byteArrayOutputStream);

                        // putting some information into bundle class
                        Bundle bundle =new Bundle();
                        bundle.putString("app_name",list.get(pos).getAppName());
                        bundle.putByteArray("app_icon",byteArrayOutputStream.toByteArray());
                        bundle.putString("app_dir",list.get(pos).getDataDir());
                        bundle.putString("app_size",list.get(pos).getAppSize());
                        bundle.putString("package_name",list.get(pos).getpName());
                        bundle.putString("firstInstallTime",list.get(pos).getFirstInstallTime());
                        bundle.putString("lastUpdateTime",list.get(pos).getLastUpdateTime());
                        bundle.putString("version_name",list.get(pos).getVersionName());

                        FragmentManager fragmentManager=getSupportFragmentManager();
                        Selected_AppInfo_Fragment selected_appInfo_fragment=new Selected_AppInfo_Fragment();

                        //passing bundle as argument to Dialog fragment
                        selected_appInfo_fragment.setArguments(bundle);
                        selected_appInfo_fragment.show(fragmentManager,"");
                    }
                });

                // check box for multiple selection
                appCheckBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedItem = getAdapterPosition();

                        if(appCheckBox.isChecked()){

                            // check box is checked now

                            //set selected item as true in recycler view
                            list.get(selectedItem).setSelected(true);

                            ObjectAnimator animator = ObjectAnimator.ofFloat(appContainer,"translationX",-20.0f,20.0f,0);
                            animator.setRepeatCount(5);
                            animator.setDuration(100);
                            animator.start();

                            // add selected item into arraylist of type Selected_Apks
                            selectedItemInList.add(new Selected_Apks(selectedItem,list.get(selectedItem).getDataDir(),list.get(selectedItem).getAppName()));
                        }
                        else{
                            // check box is unchecked now

                            //set selected item as false in recycler view
                            list.get(selectedItem).setSelected(false);

                            // traversing list to check if current item exists in list
                            // then remove it as checkbox is unchecked now
                            for(int i=0;i<selectedItemInList.size();i++){
                                if(selectedItemInList.get(i).getSelectedPositionInRecyclerView() == selectedItem ){
                                    selectedItemInList.remove(i);
                                }
                            }
                        }
                        if(!mRecyclerView.isComputingLayout()){
                            notifyItemChanged(selectedItem);
                        }

                        // will display selected app count and total app count in Toolbar
                        // when any checkbox is selected
                        if(!selectedItemInList.isEmpty()){
                            getSupportActionBar().setTitle("Selected: "+selectedItemInList.size()+"/"+list.size());
                            getSupportActionBar().setSubtitle("");

                            // this will recreate menu in toolbar
                            invalidateOptionsMenu();
                        }
                        else{
                            // restoring toolbar contents as App Name and Version
                            getSupportActionBar().setTitle("App Utility");
                            getSupportActionBar().setSubtitle("version "+appVersionNameForToolbar);

                            // this will recreate menu in toolbar
                            invalidateOptionsMenu();
                        }
                    }
                });
            }
        }
    }

    public void prepareList() {

        List<PackageInfo> packageInfoList = getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packageInfoList.size(); i++) {
            PackageInfo packageInfo = packageInfoList.get(i);
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                continue;
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String appname = packageInfo.applicationInfo.loadLabel(getPackageManager()).toString();
            String pname = packageInfo.packageName;
            String versionName = packageInfo.versionName;
            int versionCode = packageInfo.versionCode;
           // String directory = packageInfo.applicationInfo.sourceDir;
            Drawable icon = packageInfo.applicationInfo.loadIcon(getPackageManager());
            String temp = packageInfo.applicationInfo.publicSourceDir;
            String firstInstallTime = dateFormat.format(new Date(packageInfo.firstInstallTime));
            String lastUpdateTime = dateFormat.format(new Date(packageInfo.lastUpdateTime));
            File file=new File(temp);
            double sizeInByte  = (double) file.length();

            String finalAppSize ="";
            DecimalFormat dec = new DecimalFormat("0.00");
            double k = sizeInByte/1024.0;
            double m = ((sizeInByte / 1024.0) / 1024.0);
            double g = (((sizeInByte / 1024.0) / 1024.0) / 1024.0);
            double t = ((((sizeInByte / 1024.0) / 1024.0) / 1024.0) / 1024.0);

            if ( t>1 ) {
                finalAppSize = dec.format(t).concat(" TB");
            } else if ( g>1 ) {
                finalAppSize = dec.format(g).concat(" GB");
            } else if ( m>1 ) {
                finalAppSize = dec.format(m).concat(" MB");
            } else if ( k>1 ) {
                finalAppSize = dec.format(k).concat(" KB");
            }

            PackageInformation packageInformation = new PackageInformation(appname, pname, versionName, versionCode, icon, temp,finalAppSize, firstInstallTime, lastUpdateTime);
            list.add(packageInformation);
        }

        Collections.sort(list, new Comparator<PackageInformation>() {
            @Override
            public int compare(PackageInformation packageInformation, PackageInformation t1) {
                return packageInformation.getAppName().toLowerCase().compareTo(t1.getAppName().toLowerCase());
            }
        });
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
        }
        else if(!selectedItemInList.isEmpty()) {
            list.clear();
            prepareList();
            selectedItemInList.clear();
            getSupportActionBar().setTitle("App Utility");
            getSupportActionBar().setSubtitle("version "+appVersionNameForToolbar);
            invalidateOptionsMenu();
            if(!mRecyclerView.isComputingLayout()){
                mAdapter.notifyDataSetChanged();
            }
        }
        else{
            finishAffinity();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main,menu);

        if(selectedItemInList.isEmpty()){

            MenuItem share = menu.findItem(R.id.share);
            share.setVisible(false);

            MenuItem menuItem = menu.findItem(R.id.search);
            searchView = (SearchView) menuItem.getActionView();
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    mAdapter.getFilter().filter(newText);
                    return true;
                }
            });
        }
        else{
            MenuItem share = menu.findItem(R.id.share);
            share.setVisible(true);
            MenuItem search = menu.findItem(R.id.search);
            search.setVisible(false);
            MenuItem sort = menu.findItem(R.id.sort);
            sort.setVisible(false);
            MenuItem showusage = menu.findItem(R.id.storage);
            showusage.setVisible(false);
            MenuItem features = menu.findItem(R.id.features);
            features.setVisible(false);
            MenuItem grid_list_view = menu.findItem(R.id.grid_list_view);
            grid_list_view.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.sort:
                View v = LayoutInflater.from(this).inflate(R.layout.sorting_app_list,null);
                TextView ascOrder = v.findViewById(R.id.ascending_order);
                TextView dscOrder = v.findViewById(R.id.descending_order);
                TextView sortLabel = v.findViewById(R.id.sort_label);

                // setting custom font style
                Typeface typeface = Typeface.createFromAsset(getAssets(),"fonts/Ubuntu-Medium.ttf");
                sortLabel.setTypeface(typeface);

                ascOrder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Collections.sort(list, new Comparator<PackageInformation>() {
                            @Override
                            public int compare(PackageInformation packageInformation, PackageInformation t1) {
                                return packageInformation.getAppName().toLowerCase().compareTo(t1.getAppName().toLowerCase());
                            }
                        });

                        if(!mRecyclerView.isComputingLayout()){
                            mAdapter.notifyDataSetChanged();
                        }
                        if(alertDialog!=null && alertDialog.isShowing()){
                            alertDialog.dismiss();
                        }
                        Snackbar.make(coordinatorLayout,"Apps sorted in Ascending Order",Snackbar.LENGTH_LONG).show();

                    }
                });

                dscOrder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Collections.sort(list, new Comparator<PackageInformation>() {
                            @Override
                            public int compare(PackageInformation o1, PackageInformation o2) {
                                return o2.getAppName().toLowerCase().compareTo(o1.getAppName().toLowerCase());
                            }
                        });

                        if(!mRecyclerView.isComputingLayout()){
                            mAdapter.notifyDataSetChanged();
                        }
                        if(alertDialog!=null && alertDialog.isShowing()){
                            alertDialog.dismiss();
                        }
                        Snackbar.make(coordinatorLayout,"Apps sorted in Descending Order",Snackbar.LENGTH_LONG).show();
                    }
                });

                AlertDialog.Builder ab=new AlertDialog.Builder(MainActivity.this);
                ab.setCancelable(true);
                ab.setView(v);
                alertDialog = ab.create();
                alertDialog.show();
                Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_fragment_bottom_round_corners));
                alertDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                break;

            case R.id.storage:
                FragmentManager fragmentManager=getSupportFragmentManager();
                Memory_Storage_Usage memory_storage_usage = new Memory_Storage_Usage();
                memory_storage_usage.show(fragmentManager,"");
                break;

            case R.id.features:
                FragmentManager fragmentManager1=getSupportFragmentManager();
                ContainerFragment_FeatureList containerFragment_featureList=new ContainerFragment_FeatureList();
                containerFragment_featureList.show(fragmentManager1,"");
                break;

            case R.id.share:

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!hasReadWritePermission()){
                        requestReadWritePermission();
                    }
                    else{
                        ShareMultipleApks shareMultipleApks = new ShareMultipleApks();
                        //noinspection unchecked
                        shareMultipleApks.execute(selectedItemInList);
                    }
                }
                else{
                    // permissions granted or build version is less than Marshmallow
                    ShareMultipleApks shareMultipleApks = new ShareMultipleApks();
                    //noinspection unchecked
                    shareMultipleApks.execute(selectedItemInList);
                    }
                break;

            case R.id.grid_list_view:

                if(Objects.requireNonNull(sharedPreferences.getString("layoutManagerType", null)).equalsIgnoreCase("grid")){
                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
                    mRecyclerView.setLayoutManager(layoutManager);
                    if(!mRecyclerView.isComputingLayout()){
                        mAdapter.notifyDataSetChanged();
                    }
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("layoutManagerType","linear");
                    editor.apply();
                }
                else{
                    RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
                    mRecyclerView.setLayoutManager(layoutManager);
                    if(!mRecyclerView.isComputingLayout()){
                        mAdapter.notifyDataSetChanged();
                    }
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("layoutManagerType","grid");
                    editor.apply();
                }
                break;

            case R.id.backup_location:
                AlertDialog.Builder builder =new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Backup Location");
                builder.setCancelable(false);
                builder.setMessage("1. Open "+getResources().getString(R.string.internal_storage)+", "+"\n"+
                                   "2. Open "+getResources().getString(R.string.app_utility)+" folder.");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                    }
                });
                Dialog dialog = builder.create();
                dialog.show();
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_fragment_bottom_round_corners));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean hasReadWritePermission(){
        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        else{
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestReadWritePermission(){
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},203);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 203:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    ShareMultipleApks shareMultipleApks = new ShareMultipleApks();
                    //noinspection unchecked
                    shareMultipleApks.execute(selectedItemInList);
                }
                break;
        }
    }

    // Async Task class implemented for bulk sharing of selected apps.
    @SuppressLint("StaticFieldLeak")
    class ShareMultipleApks extends AsyncTask<ArrayList<Selected_Apks>,Void,ArrayList<File>>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.progress_bar,null);
            AlertDialog.Builder ab=new AlertDialog.Builder(MainActivity.this);
            ab.setView(view);
            ab.setCancelable(false);
            alertDialog = ab.create();
            alertDialog.show();
        }

        @Override
        protected ArrayList<File> doInBackground(ArrayList<Selected_Apks>... arrayLists) {

            ArrayList<File> tempList=new ArrayList<>();

            for(int i=0; i < arrayLists[0].size();i++){

                File tempFile = new File(fileSharePath);
                File originalApk = new File(arrayLists[0].get(i).getApkPath());

                try{
                    if(!tempFile.isDirectory()){
                        tempFile.mkdirs();
                    }

                    tempFile = new File(tempFile.getCanonicalPath()+ "/" +arrayLists[0].get(i).getAppName().toLowerCase()+".apk");
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

                    tempList.add(tempFile);
                }
                catch(IOException e){
                    Log.e("Exception: ",e.getMessage());
                }
            }
            return tempList;
        }

        @Override
        protected void onPostExecute(ArrayList<File> files) {
            super.onPostExecute(files);

            ArrayList<Uri> uriArrayList=new ArrayList<>();

            if(alertDialog!=null && alertDialog.isShowing()){
                alertDialog.dismiss();
            }

            try{
                for(int i=0;i<files.size();i++){
                    Uri uriForFile = FileProvider.getUriForFile(getApplicationContext(),BuildConfig.APPLICATION_ID +".provider",files.get(i));
                    uriArrayList.add(uriForFile);
                }

                Intent intent=new Intent(Intent.ACTION_SEND_MULTIPLE);
                intent.setType("application/vnd.android.package-archive");
                intent.putExtra(Intent.EXTRA_STREAM,uriArrayList);
                PackageManager packageManager = getPackageManager();
                if(packageManager.resolveActivity(intent,0) !=null ){
                    startActivity(Intent.createChooser(intent, "Share app via"));
                }
            }
            catch(Exception e){
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

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

}