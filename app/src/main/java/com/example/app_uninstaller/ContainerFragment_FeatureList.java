package com.example.app_uninstaller;


import android.annotation.TargetApi;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class ContainerFragment_FeatureList extends DialogFragment {

    private Fragment[] fragments = new Fragment[]{new FragmentOne_FeatureList(),new FragmentTwo_FeatureList(),new FragmentThree_FeatureList(),new FragmentFour_FeatureList(),new FragmentFive_FeatureList()};
    private int count=0;

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        assert dialog != null;
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(Objects.requireNonNull(getActivity()).getResources().getDrawable(R.drawable.dialog_fragment_bottom_round_corners));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        Objects.requireNonNull(getDialog()).requestWindowFeature(Window.FEATURE_NO_TITLE);
        View rootView =  inflater.inflate(R.layout.fragment_container_fragment__feature_list, container, false);

        final ImageView btnNext = rootView.findViewById(R.id.btn_next);
        final ImageView btnPrev = rootView.findViewById(R.id.btn_prev);

        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = Objects.requireNonNull(fragmentManager).beginTransaction();
        fragmentTransaction.add(R.id.fragmentContainer,fragments[count],"");
        fragmentTransaction.commit();

        btnPrev.setVisibility(View.INVISIBLE);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                count+=1;

                if(count <= fragments.length-1){
                    FragmentManager fragmentManager1 = getChildFragmentManager();
                    FragmentTransaction fragmentTransaction = Objects.requireNonNull(fragmentManager1).beginTransaction();
                    fragmentTransaction.replace(R.id.fragmentContainer,fragments[count],"").commit();
                    btnPrev.setVisibility(View.VISIBLE);

                    if(count==fragments.length-1){
                        btnNext.setVisibility(View.INVISIBLE);
                        btnPrev.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {

                count-=1;

                if(count >= 0){

                    FragmentManager fragmentManager1 = getChildFragmentManager();
                    FragmentTransaction fragmentTransaction = Objects.requireNonNull(fragmentManager1).beginTransaction();
                    fragmentTransaction.replace(R.id.fragmentContainer,fragments[count],"").commit();
                    btnNext.setVisibility(View.VISIBLE);
                    
                    if(count==0){
                        btnNext.setVisibility(View.VISIBLE);
                        btnPrev.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });

        return rootView;
    }
}