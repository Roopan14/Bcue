package com.example.roopanc.b_cue;

import android.*;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;


/**
 * Created by Roopan C on 2/23/2018.
 */

public class ViewReminderList extends Fragment {

    FirebaseDatabase database;
    DatabaseReference databaseReference;
    String EVENT_URL;
    FirebaseListAdapter<EventPOJO> firebaseListAdapter;
    List<EventPOJO> modelList;
    RecyclerView recyclerView;
    ReminderAdapter reminderAdapter;
    String accountName = null;

    String Event_ID;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    public TextView nametv, typetv, infotv;
    public ImageView priority, delete, evetypeimage;
    public LinearLayout holderLinear;
    TextView evTextView;
    ProgressDialog progressDialog;

    ListView listView;
    String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "June", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};


    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.viewreminderlist, container, false);

        sharedPreferences = getActivity().getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE);
        accountName = sharedPreferences.getString("email", null);
        if (accountName != null)
        {
            accountName = accountName.split("@")[0];
        }
        EVENT_URL = "https://b-cue-658ce.firebaseio.com/Events/"+accountName;
        Log.d("Eventurl", EVENT_URL);

        progressDialog = new ProgressDialog(getActivity(), ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Fetching Events...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        modelList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recyclerview);

        listView = view.findViewById(R.id.listview);
        evTextView = view.findViewById(R.id.noevtv);

        reminderAdapter = new ReminderAdapter(modelList, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));
        //recyclerView.setAdapter(reminderAdapter);

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReferenceFromUrl(EVENT_URL);
        checkData();

        firebaseListAdapter = new FirebaseListAdapter<EventPOJO>(getActivity(), EventPOJO.class, R.layout.reminderlistholder, databaseReference) {


            @Override
            protected void populateView(View itemView, EventPOJO model, final int position) {

                //position starts at 0

                Log.d("pop", "populateView: ");
                nametv = itemView.findViewById(R.id.eventnametv);
                typetv = itemView.findViewById(R.id.eventtypetv);
                infotv = itemView.findViewById(R.id.eventinfotv);
                evetypeimage = itemView.findViewById(R.id.remtype);
                holderLinear = itemView.findViewById(R.id.holderlinear);

                priority = itemView.findViewById(R.id.priorityev);

                EventPOJO eventPOJO = model;
                modelList.add(eventPOJO);
                nametv.setText(eventPOJO.getEventName());
                // for now .. showing event dates instead of type
                String dateEv = eventPOJO.getEventDate();
                String dateDisplay = dateEv.split("-")[0] + " " + months[Integer.parseInt(dateEv.split("-")[1])-1] + " " + dateEv.split("-")[2];
                typetv.setText(dateDisplay);
                infotv.setText(eventPOJO.getAddInfo());

                if (eventPOJO.getEventType().equalsIgnoreCase("Birthday"))
                {
                    evetypeimage.setImageResource(R.drawable.cake);
                }
                else {
                    evetypeimage.setImageResource(R.drawable.wedding);
                }

                Event_ID = eventPOJO.getEventID();

                priority.setVisibility(View.GONE);

                if (Integer.valueOf(eventPOJO.getPriority()) == 1)
                {
                    priority.setImageResource(R.drawable.colostar);
                    priority.setVisibility(View.VISIBLE);
                }

                holderLinear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // code to pop up (Edit or Delete) Option


                        final EventPOJO eventPOJO1 = modelList.get(position);

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("Choose the action to perform");
                        builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //show edit fragment --- passing the event id
                                //Toast.makeText(getContext(), "Edit"+eventPOJO1.getEventID(), Toast.LENGTH_SHORT).show();

                                AddReminder addReminder = new AddReminder();
                                Bundle bundle = new Bundle();
                                bundle.putBoolean("edit", true);
                                bundle.putString(HomeActivity.evName, eventPOJO1.getEventName());
                                bundle.putString(HomeActivity.evDate, eventPOJO1.getEventDate());
                                bundle.putString(HomeActivity.evInfo, eventPOJO1.getAddInfo());
                                bundle.putString(HomeActivity.evPriority, eventPOJO1.getPriority());
                                bundle.putString(HomeActivity.evType, eventPOJO1.getEventType());
                                bundle.putString(HomeActivity.evID, eventPOJO1.getEventID());
                                addReminder.setArguments(bundle);
                                getFragmentManager().beginTransaction().replace(R.id.content_frame, addReminder).addToBackStack(null).commit();
                            }
                        });

                        builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //passing the event id... delete the entry
                                //firebase entry delete
                                databaseReference.child(eventPOJO1.getEventID()).removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        Toast.makeText(getContext(), "Event Deleted", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    return;
                                }

                                //calendar event delete
                                ContentResolver cr = getActivity().getContentResolver();
                                Uri eventUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, Long.parseLong(eventPOJO1.getEventID()));
                                cr.delete(eventUri, null, null);

                                //reminder delete
                                Uri remUri = ContentUris.withAppendedId(CalendarContract.Reminders.CONTENT_URI, Long.parseLong(eventPOJO1.getEventID()));
                                cr.delete(remUri, null, null);

                                checkData();

                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });

                progressDialog.dismiss();
            }

        };

        listView.setAdapter(firebaseListAdapter);
        firebaseListAdapter.notifyDataSetChanged();

        /*databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> dataSnapshots = dataSnapshot.getChildren();
                for (DataSnapshot ds : dataSnapshots)
                {
                    EventPOJO eventPOJO = ds.getValue(EventPOJO.class);
                    modelList.add(eventPOJO);
                    *//*System.out.println(ds.getKey());
                    Iterable<DataSnapshot> dss = ds.getChildren();
                    for (DataSnapshot des : dss)
                    {
                        EventPOJO eventPOJO = des.getValue(EventPOJO.class);
                        modelList.add(eventPOJO);
                        System.out.println(eventPOJO.getEventName());
                    }*//*
                }

                reminderAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("abc", databaseError.toString());
            }
        });*/


        return view;
    }

    private void checkData() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() != 0)
                {

                }
                else {
                    listView.setVisibility(View.GONE);
                    evTextView.setVisibility(View.VISIBLE);
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
