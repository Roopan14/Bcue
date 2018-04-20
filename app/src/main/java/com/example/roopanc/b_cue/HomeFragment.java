package com.example.roopanc.b_cue;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toolbar;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by Roopan C on 2/23/2018.
 */

public class HomeFragment extends Fragment {

    ImageView addReminder, viewReminder;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    GoogleSignInClient mGoogleSignInClient;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.homefragment, container, false);

        addReminder = view.findViewById(R.id.addreminder);
        viewReminder = view.findViewById(R.id.viewreminder);

        addReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddReminder addReminder = new AddReminder();
                getFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, addReminder).addToBackStack(null).commit();
            }
        });

        viewReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewReminderList viewReminderList = new ViewReminderList();
                getFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, viewReminderList).addToBackStack(null).commit();
            }
        });

        sharedPreferences = getActivity().getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        return view;
    }

}
