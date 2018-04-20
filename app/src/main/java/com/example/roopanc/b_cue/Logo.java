package com.example.roopanc.b_cue;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Roopan C on 2/19/2018.
 */

public class Logo extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.logo, container, false);

        return view;
    }

    @Override
    public void onResume() {


        Log.d("resume", "onResume: ");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getActivity().getFragmentManager().popBackStack();
            }
        }, 2000);

        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //getActivity().getFragmentManager().popBackStack();
    }
}
