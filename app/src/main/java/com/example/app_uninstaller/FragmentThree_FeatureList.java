package com.example.app_uninstaller;


import android.graphics.Typeface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Objects;


public class FragmentThree_FeatureList extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_fragment_three__feature_list, container, false);

        TextView num = view.findViewById(R.id.num);
        TextView content = view.findViewById(R.id.content);

        Typeface typeface = Typeface.createFromAsset(Objects.requireNonNull(getActivity()).getAssets(),"fonts/Ubuntu-Medium.ttf");

        num.setTypeface(typeface);
        content.setTypeface(typeface);

        return view;
    }
}
