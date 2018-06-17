package com.example.testviewpager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class IntroFragment extends Fragment {

    public static String PARAM_FRAGMENT_LAYOUT = "PARAM_FRAGMENT_LAYOUT";

    private int mFragmentLayout;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DebugLogger.d(null);

        mFragmentLayout = getArguments().getInt(PARAM_FRAGMENT_LAYOUT, -1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(mFragmentLayout, container, false);

        return rootView;
    }
}