package com.example.roopanc.b_cue;

import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.Manifest;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import static android.app.Activity.RESULT_OK;

/**
 * Created by RadhikaRanganathan on 26/02/2018.
 */

public class AddReminder extends Fragment {

    int calendarId = 0;
    int bulk = 0;
    String accountName = "roopanc4@gmail.com";
    final int REQ_READ_PERMISSION = 101;
    final int REQ_WRITE_PERMISSION = 102;
    Date startDate, endDate;
    int year, month, day;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String EVENT_URL = "https://b-cue-658ce.firebaseio.com/Events";
    FirebaseDatabase database;
    DatabaseReference databaseReference;

    String eventName, addInfo, eventDate, eventType, eventId;
    String evName, evInfo, evDate, evType, evId, evpriority;
    String priority = "0";
    int _priority = 0;
    EditText nameET, dobET, infoET;
    Spinner spinner;
    ImageView priorityImg;
    List<String> spinnerList;
    Button addButton;
    Calendar calendar;
    ProgressDialog progressDialog;
    boolean update = false;
    long eventID;
    String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "June", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkPermissions();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReferenceFromUrl(EVENT_URL);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("ondata", dataSnapshot.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.addreminder, container, false);
        calendar = Calendar.getInstance();

        sharedPreferences = getActivity().getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE);

        nameET = view.findViewById(R.id.nameet);
        dobET = view.findViewById(R.id.dobet);
        infoET = view.findViewById(R.id.infoet);
        addButton = view.findViewById(R.id.addBT);
        priorityImg = view.findViewById(R.id.priorityimg);

        progressDialog = new ProgressDialog(getActivity(), ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);


        spinner = view.findViewById(R.id.reminderspinner);
        spinnerList = new ArrayList<>();
        spinnerList.add("Birthday");
        spinnerList.add("Anniversary");
        //spinnerList.add("ToDo");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, spinnerList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                eventType = adapterView.getSelectedItem().toString();
                //Toast.makeText(getActivity(), eventType, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        dobET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        // i year i1 month (starts at 0) i2 day
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(i, i1, i2);
                        //calendar.getTimeInMillis();
                        startDate = new Date(i, i1, i2);
                        endDate = new Date(i, i1, i2, 23, 59);

                        year = i;
                        month = i1 + 1;
                        day = i2;

                        String dateDisplay = i2 + " " + months[i1] + " " + i;

                        String s = "" + i1 + i2 + i;
                        if (month<10)
                        {
                            // adding zero to months < 10
                            eventDate = day + "-0" + month + "-" + year;
                        }
                        eventDate = day + "-" + month + "-" + year;
                        dobET.setText(dateDisplay);
                        //Toast.makeText(getActivity(), startDate.toString(), Toast.LENGTH_SHORT).show();
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });

        priorityImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String description = view.getContentDescription().toString();

                if (description.equals("nonpriority")) {
                    view.setContentDescription("priority");
                    priorityImg.setImageResource(R.drawable.colostar);
                    _priority = 1;
                    priority = _priority + "";
                }
                else if (description.equals("priority"))
                {
                    view.setContentDescription("nonpriority");
                    priorityImg.setImageResource(R.drawable.star);
                    _priority = 0;
                    priority = _priority + "";
                }
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                //
                eventName = nameET.getText().toString();
                //eventDate = dobET.getText().toString();
                addInfo = infoET.getText().toString();
                //take 'priority', 'eventType' also

                if (dobET.getText().toString() != null && !dobET.getText().toString().equals("") && eventName != null && !eventName.equals("")) {

                    if (accountName != null && !accountName.equals("")) {
                        getCalendarID();
                        Log.d("a", "onClick: ");
                        String msg = "Events Added";
                        if (update)
                        {
                            msg = "Events Updated";
                        }
                        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                    } else {
                        Intent googlePicker = AccountPicker.newChooseAccountIntent(null, null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true, null, null, null, null);
                        startActivityForResult(googlePicker, 100);
                        Log.d("b", "onClick: ");
                    }
                }
                else {
                    Toast.makeText(getActivity(), "All fields are mandatory", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /*databaseReference = database.getReferenceFromUrl(EVENT_URL);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> dataSnapshots = dataSnapshot.getChildren();
                for (DataSnapshot ds : dataSnapshots)
                {
                    System.out.println(ds.getKey());
                    Iterable<DataSnapshot> dss = ds.getChildren();
                    for (DataSnapshot des : dss)
                    {
                        EventPOJO eventPOJO = des.getValue(EventPOJO.class);
                        System.out.println(eventPOJO.getEventName());
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("abc", databaseError.toString());
            }
        });
*/

        //update event scenerio
        Bundle bundle = getArguments();
        if (bundle != null)
        {
            evName = bundle.getString(HomeActivity.evName);
            eventName = evName;
            evDate = bundle.getString(HomeActivity.evDate);
            evType = bundle.getString(HomeActivity.evType);
            evpriority = bundle.getString(HomeActivity.evPriority);
            priority = evpriority;
            evInfo = bundle.getString(HomeActivity.evInfo);
            addInfo = evInfo;
            evId = bundle.getString(HomeActivity.evID);

            eventID = Long.parseLong(evId);

            splitDate(evDate);

            //14 May 2018 model
            String dateDisplay = evDate.split("-")[0] + " " + months[Integer.parseInt(evDate.split("-")[1])-1] + " " + evDate.split("-")[2];


            nameET.setText(evName);
            dobET.setText(dateDisplay);
            infoET.setText(evInfo);
            addButton.setText("Update");
            addButton.setAllCaps(false);
            if (evpriority.equals("0"))
            {
                priorityImg.setImageResource(R.drawable.star);
            }
            else {
                priorityImg.setImageResource(R.drawable.colostar);
            }
            update = true;
        }


        //true - already done - no need to show
        if (!sharedPreferences.getBoolean("static", false) || !update) {
            alertDial();
        }

        return view;
    }

    private void alertDial() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Static Data");
        builder.setMessage("Do you want to sync 50+ static data to your google calendar ?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                progressDialog.show();
                bulk = 1;
                editor = sharedPreferences.edit();
                editor.putBoolean("static", true);
                editor.commit();
                Intent googlePicker = AccountPicker.newChooseAccountIntent(null, null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true, null, null, null, null);
                startActivityForResult(googlePicker, 100);
            }
        });
        builder.setNegativeButton("No, Skip for Now", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // do nothing
                dialogInterface.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void importStaticData() {

        String[] bdates = {"01-04-2018", "02-09-2018", "02-04-2018", "12-04-2018", "16-12-2018", "30-09-2018", "12-04-2018", "10-09-2018",
                "26-01-2018", "07-05-2018", "20-08-2018", "12-04-2018", "03-02-2018", "09-10-2018", "12-02-2018", "08-06-2018", "17-01-2018", "16-05-2018", "17-08-2018",
                "24-11-2018", "24-01-2018", "14-09-2018", "24-01-2018", "16-09-2018", "02-05-2018", "19-12-2018", "11-03-2018", "29-12-2018", "11-12-2018",
                "29-05-2018", "29-06-2018", "09-10-2018", "02-05-2018", "23-08-2018", "23-06-2018", "21-05-2018", "13-02-2018", "18-11-2018", "18-09-2018", "14-12-2018",
                "30-03-2018", "07-10-2018", "08-01-2018"};

        String[] annidates = {"29-12-2018", "17-06-2018", "18-12-2018", "21-10-2018", "25-12-2018", "06-05-2018", "21-04-2018", "06-12-2018", "18-04-2018",
                "08-06-2018", "16-12-2018", "07-12-2018", "09-02-2018"};

        String[] bNames = {"Sunil", "Kavita", "Mukund", "Dhiraj", "Pooja", "Divya", "Mihir", "Shilpa", "Ajay", "Sreevidya", "Shriya", "Vinod", "Renuka", "Viren",
        "Ishan", "Ajit", "Srilaxmi", "Subham", "Shivam", "Rinkesh", "Bijal", "Rehan", "Karan", "Raashi", "Purab", "Sachin", "Mangesh", "Smita", "Riddhi", "Rigwed",
        "Vijay", "Rohini", "Aakash", "Aaush", "Murli", "Kanchan", "Duhita", "Anubhav", "Pankuri", "Riddansh", "Anand", "Usha", "Ritiika"};

        String annivNames[] = {"Sunil - Kavita", "Dhiraj - Pooja", "Mihir - Shilpa", "Ajay - Sreevidya", "Vinod - Renuka", "Ajit - Srilaxmi", "Rinkesh - Bijal",
                "Karan - Raashi", "Mangesh - Smita", "Vijay - Rohini", "Murli - Kanchan", "Anubhav - Pankuri", "Anand - Usha"};

        String[] flatnum = {"A-203", "A-501", "A-503", "A-602", "A-701", "A-704", "A-1001", "A-1004", "A-1302", "B-304", "B-401", "B-1201", "B-1301", "B-1602"};
        String[] flatnumanniv = {"A-203", "A-501", "A-503", "A-602", "A-701", "A-704", "A-1001", "A-1004", "B-304", "B-401", "B-1201", "B-1301", "B-1602"};

        //bday dates
        for (int i = 1; i<= bdates.length; i++)
        {
            if (i<4)
            {
                System.out.println(bdates[i-1]+bNames[i-1]+flatnum[0]);
                String desc = flatnum[0];
                splitFunc(bdates[i-1], desc, bNames[i-1]);
            }
            else if (i>=4 && i<7)
            {
                System.out.println(bdates[i-1]+bNames[i-1]+flatnum[1]);
                String desc = flatnum[1];
                splitFunc(bdates[i-1], desc, bNames[i-1]);
            }
            else if (i>=7 && i<9)
            {
                System.out.println(bdates[i-1]+bNames[i-1]+flatnum[2]);
                String desc = flatnum[2];
                splitFunc(bdates[i-1], desc, bNames[i-1]);
            }
            else if (i>=9 && i<12)
            {
                System.out.println(bdates[i-1]+bNames[i-1]+flatnum[3]);
                String desc = flatnum[3];
                splitFunc(bdates[i-1], desc, bNames[i-1]);
            }
            else if (i>=12 && i<16)
            {
                System.out.println(bdates[i-1]+bNames[i-1]+flatnum[4]);
                String desc = flatnum[4];
                splitFunc(bdates[i-1], desc, bNames[i-1]);
            }
            else if (i>=16 && i<20)
            {
                System.out.println(bdates[i-1]+bNames[i-1]+flatnum[5]);
                String desc = flatnum[5];
                splitFunc(bdates[i-1], desc, bNames[i-1]);
            }
            else if (i>=20 && i<23)
            {
                System.out.println(bdates[i-1]+bNames[i-1]+flatnum[6]);
                String desc = flatnum[6];
                splitFunc(bdates[i-1], desc, bNames[i-1]);
            }
            else if (i>=23 && i<26)
            {
                System.out.println(bdates[i-1]+bNames[i-1]+flatnum[7]);
                String desc = flatnum[7];
                splitFunc(bdates[i-1], desc, bNames[i-1]);
            }
            else if (i==26)
            {
                System.out.println(bdates[i-1]+bNames[i-1]+flatnum[8]);
                String desc = flatnum[8];
                splitFunc(bdates[i-1], desc, bNames[i-1]);
            }
            else if(i>26 && i<31)
            {
                System.out.println(bdates[i-1]+bNames[i-1]+flatnum[9]);
                String desc = flatnum[9];
                splitFunc(bdates[i-1], desc, bNames[i-1]);
            }
            else if (i>=31 && i<35)
            {
                System.out.println(bdates[i-1]+bNames[i-1]+flatnum[10]);
                String desc = flatnum[10];
                splitFunc(bdates[i-1], desc, bNames[i-1]);
            }
            else if (i>=35 && i<38)
            {
                System.out.println(bdates[i-1]+bNames[i-1]+flatnum[11]);
                String desc = flatnum[11];
                splitFunc(bdates[i-1], desc, bNames[i-1]);
            }
            else if (i>=38 && i<41)
            {
                System.out.println(bdates[i-1]+bNames[i-1]+flatnum[12]);
                String desc = flatnum[12];
                splitFunc(bdates[i-1], desc, bNames[i-1]);
            }
            else if (i>=41 && i<44)
            {
                System.out.println(bdates[i-1]+bNames[i-1]+flatnum[13]);
                String desc = flatnum[13];
                splitFunc(bdates[i-1], desc, bNames[i-1]);
            }
        }

        //for anniv dates

        for (int i = 1; i<= annidates.length; i++)
        {
            System.out.println(annidates[i-1]+annivNames[i-1]+flatnumanniv[i-1]);
            String desc = flatnumanniv[i-1];
            splitFunc(annidates[i-1], desc, annivNames[i-1]);
        }

        progressDialog.dismiss();



        Snackbar.make(getView(), "Events added to Calendar.", Snackbar.LENGTH_SHORT).show();
    }

    private void splitDate(String bdate) {
        eventDate = bdate;
        String[] dateSplitstr = bdate.split("-");
        int[] dateSplit = new int[dateSplitstr.length];

        int j=0;
        for(String str : dateSplitstr)
        {
            //17-06-2018 format
            dateSplit[j] = Integer.valueOf(str);
            j++;
        }
        year = dateSplit[2];
        month = dateSplit[1];
        day = dateSplit[0];

        /*//year-month-date
        setCalendarEvent(dateSplit[2], dateSplit[1], dateSplit[0], desc, eventName);*/
    }

    private void splitFunc(String bdate, String desc, String eventName) {
        eventDate = bdate;
        String[] dateSplitstr = bdate.split("-");
        int[] dateSplit = new int[dateSplitstr.length];

        int j=0;
        for(String str : dateSplitstr)
        {
            //17-06-2018 format
            dateSplit[j] = Integer.valueOf(str);
            j++;
        }
        desc = "Flat No. "+ desc;
        //year-month-date
        setCalendarEvent(dateSplit[2], dateSplit[1], dateSplit[0], desc, eventName);
    }

    private void getCalendarID() {

        String calenderEmaillAddress = accountName;
        String[] projection = new String[]{
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.ACCOUNT_NAME};
        ContentResolver cr = getActivity().getContentResolver();

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Cursor cursor = cr.query(CalendarContract.Calendars.CONTENT_URI, projection,
                CalendarContract.Calendars.ACCOUNT_NAME + "=? and (" +
                        CalendarContract.Calendars.NAME + "=? or " +
                        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME + "=?)",
                new String[]{calenderEmaillAddress, calenderEmaillAddress,
                        calenderEmaillAddress}, null);
        Log.d("Cursor", "" + cursor.toString());

        if (cursor.moveToFirst()) {

            if (cursor.getString(1).equals(calenderEmaillAddress))
                calendarId = cursor.getInt(0); //youre calender id to be insered in above your code

            Log.d("calendarID", "" + calendarId);
        }

        if (bulk == 0) {
            setCalendarEvent(year, month, day, addInfo, eventName);
        }


    }

    private void setCalendarEvent(int year, int month, int day, String addInfo, String eventName) {

        //get bday date from datepicker and assign it in startDate and endDate
        long startMillis = 0;
        long endMillis = 0;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startDate = null, endDate = null;
        try {
            startDate = simpleDateFormat.parse(year+"-"+month+"-"+day+" 00:00:00");
            startMillis = startDate.getTime();
            endDate = simpleDateFormat.parse(year+"-"+month+"-"+day+" 23:59:59");
            endMillis = endDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }


        if (update)
        {
            //using content resolver to update events to calendar
            ContentResolver cr = getActivity().getContentResolver();
            ContentValues values = new ContentValues();
            TimeZone timeZone = TimeZone.getDefault();
            values.put(CalendarContract.Events.DTSTART, startMillis);
            values.put(CalendarContract.Events.DTEND, endMillis);
            values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());
            if (eventName.contains("-")||eventType.equals("Anniversary"))
            {
                eventType = "Anniversary";
                values.put(CalendarContract.Events.TITLE, eventName + " Anniversary");
            }
            else {
                eventType = "Birthday";
                values.put(CalendarContract.Events.TITLE, eventName + "'s Birthday");
            }
            //values.put(CalendarContract.Events.ALL_DAY, true);
            values.put(CalendarContract.Events.HAS_ALARM, true);
            values.put(CalendarContract.Events.DESCRIPTION, addInfo);
            values.put(CalendarContract.Events.CALENDAR_ID, calendarId);

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            Uri eventUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID);
            Uri remUri = ContentUris.withAppendedId(CalendarContract.Reminders.CONTENT_URI, eventID);

            cr.update(eventUri, values, null, null);

            eventId = eventID + ""; // for storing in firebase


            ContentValues cvalues = new ContentValues();

            cvalues.put(CalendarContract.Reminders.MINUTES, 10);
            cvalues.put(CalendarContract.Reminders.EVENT_ID, eventID); //long form
            cvalues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALARM);
            cr.update(remUri, cvalues, null, null);

            //Uri uri = cr.insert(CalendarContract.Reminders.CONTENT_URI, values);

            //place code to store data into firebse
            EventPOJO eventPOJO = new EventPOJO(eventId, eventName, eventType, eventDate, addInfo, priority);
            accountName = sharedPreferences.getString("email", null);
            if (accountName != null) {
                accountName = accountName.split("@")[0];
                databaseReference = database.getReferenceFromUrl(EVENT_URL + "/" + accountName + "/" + eventId);
                databaseReference.setValue(eventPOJO);
                getFragmentManager().popBackStack();
                progressDialog.dismiss();
            }
            //Snackbar.make(getView(), "Events added to Calendar.", Snackbar.LENGTH_SHORT).show();
        }
        else {
            //using content resolver to add events to calendar
            ContentResolver cr = getActivity().getContentResolver();
            ContentValues values = new ContentValues();
            TimeZone timeZone = TimeZone.getDefault();
            values.put(CalendarContract.Events.DTSTART, startMillis);
            values.put(CalendarContract.Events.DTEND, endMillis);
            values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());
            if (eventName.contains("-")||eventType.equals("Anniversary"))
            {
                eventType = "Anniversary";
                values.put(CalendarContract.Events.TITLE, eventName + " Anniversary");
            }
            else {
                eventType = "Birthday";
                values.put(CalendarContract.Events.TITLE, eventName + "'s Birthday");
            }
            //values.put(CalendarContract.Events.ALL_DAY, true);
            values.put(CalendarContract.Events.HAS_ALARM, true);
            values.put(CalendarContract.Events.DESCRIPTION, addInfo);
            values.put(CalendarContract.Events.CALENDAR_ID, calendarId);

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
            Log.d("setCalendarEvent", ""+CalendarContract.Events.CONTENT_URI);

            long eventID = Long.parseLong(uri.getLastPathSegment());
            eventId = eventID + ""; // for storing in firebase


            ContentValues cvalues = new ContentValues();

            cvalues.put(CalendarContract.Reminders.MINUTES, 10);
            cvalues.put(CalendarContract.Reminders.EVENT_ID, eventID); //long form
            cvalues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALARM);//method_alarm
            cr.insert(CalendarContract.Reminders.CONTENT_URI, cvalues);
            //Uri uri = cr.insert(CalendarContract.Reminders.CONTENT_URI, values);

            //place code to store data into firebse
            EventPOJO eventPOJO = new EventPOJO(eventId, eventName, eventType, eventDate, addInfo, priority);
            accountName = sharedPreferences.getString("email", null);
            if (accountName != null) {
                accountName = accountName.split("@")[0];
                databaseReference = database.getReferenceFromUrl(EVENT_URL + "/" + accountName + "/" + eventId);
                databaseReference.setValue(eventPOJO);
                getFragmentManager().popBackStack();
                progressDialog.dismiss();
            }
            //Snackbar.make(getView(), "Events added to Calendar.", Snackbar.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            Log.d("accname", accountName);
            if (checkPermissions()) {
                getCalendarID();
                if (bulk == 1)
                {
                    importStaticData();
                }
            }
        }
        else {
            progressDialog.dismiss();
        }
    }

    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CALENDAR}, REQ_READ_PERMISSION);
        } else if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_CALENDAR}, REQ_WRITE_PERMISSION);
        } else {
            return true;
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // check whether permisssion is granted. if not, request again
        switch (requestCode) {
            case REQ_READ_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(getActivity(), "Permission Needed", Toast.LENGTH_SHORT).show();
                } else {
                    checkPermissions();
                }
                break;
            case REQ_WRITE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(getActivity(), "Permission Needed", Toast.LENGTH_SHORT).show();
                } else {
                    checkPermissions();
                }
                break;
        }
    }

}
