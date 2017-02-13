package com.yummy.hotchat.Fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yummy.hotchat.R;


public class MySettings extends Fragment {

    private View view;
    private CardView cvHelp;
    private CardView cvAbout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        cvAbout= (CardView) view.findViewById(R.id.cv_settings_about_us);
        cvHelp= (CardView) view.findViewById(R.id.cv_settings_help);
        initParameter();
        return view;
    }

    /**
     * 设置和帮助的跳转
     */
    void initParameter(){

        cvAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(),MySettingsAboutUs.class);
                startActivity(intent);
            }
        });

        cvHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(),MySettingsHelp.class);
                startActivity(intent);
            }
        });

    }

}
