package com.googlecalendar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import pub.devrel.easypermissions.EasyPermissions;

import android.accounts.Account;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.calendar.CalendarScopes;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import androidx.appcompat.app.AppCompatActivity;
import pub.devrel.easypermissions.AfterPermissionGranted;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.app.Dialog;
import android.Manifest;
import android.accounts.AccountManager;
import android.widget.Toast;
//sdk.dir=C\:\\Users\\cjy34\\AppData\\Local\\Android\\Sdk
//sdk.dir=C\:\\Users\\ASU\\AppData\\Local\\Android\\Sdk

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, FragmentCallback, EasyPermissions.PermissionCallbacks, FragmentToActivity, OnDatabaseCallback {
    private static final String TAG = "MainActivity";
    private com.google.api.services.calendar.Calendar mService = null;
    private int mID = 0;
    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    static final int REQUEST_ACCOUNT_PICKER_CHANGE = 1004;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR};

    Toolbar toolbar;
    Fragment fragment1, fragment2, fragment3, fragment2dialog;
    FragmentManager manager;
    ImageView imageView;
    LinearLayout headerlayout;
    TextView text, text2;
    DrawerLayout drawerLayout;
    String sch, map, mem, date1, date2, time1, time2, alarm1, eid, aaa;
    int type = 1;
    int alarmTime = 0;

    public static Stack<Fragment> fragmentStack;

    private boolean logoutchecked = false; //false = ?????????, true = ???????????? ??????
    CalDatabase database;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //View headerView = navigationView.getHeaderView(0);
        View headerView = navigationView.inflateHeaderView(R.layout.nav_header_main);
        headerlayout = headerView.findViewById(R.id.headerlayout);
        imageView = headerView.findViewById(R.id.imageView);
        text = headerView.findViewById(R.id.text);
        text2 = headerView.findViewById(R.id.text2);

        headerlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mID = 1;           //????????? ??????
                SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(PREF_ACCOUNT_NAME, "");
                editor.apply();
                mCredential.setSelectedAccountName("");
                startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER_CHANGE);
                chooseAccount();
            }
        });

        fragment1 = new Fragment1();
        fragment2 = new Fragment2();
        fragment3 = new Fragment3();
        fragment2dialog = new Fragment2Dialog();
        fragmentStack = new Stack<>();
        fragmentStack.push(fragment1);
        manager = getSupportFragmentManager();
        manager.beginTransaction().add(R.id.container, fragment1).commit();

        // Google Calendar API ???????????? ???????????? ProgressDialog
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Google Calendar API ?????? ????????????.");

        // Google Calendar API ???????????? ?????? ????????? ?????? ?????????( ?????? ?????? credentials, ????????? ?????? )
        // OAuth 2.0??? ???????????? ?????? ?????? ?????? ??? ???????????? ?????? ??????
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(),
                Arrays.asList(SCOPES)
        ).setBackOff(new ExponentialBackOff()); // I/O ?????? ????????? ???????????? ????????? ?????? ??????

        chooseAccount();

        // open database
        if (database != null) {
            database.close();
            database = null;
        }
        database = CalDatabase.getInstance(this);

        boolean isOpen = database.open();
        if (isOpen) {
            Log.d(TAG, "Book database is open.");
        } else {
            Log.d(TAG, "Book database is not open.");
        }

        appstart12();
    }

    public void appstart12() {

        String maxid = String.valueOf(maxid());
        Log.i("max id ", maxid);
        mID = 3;
        Log.i("mid", String.valueOf(mID));
        getResultsFromApi();
    }


    @Override
    public void communicate(int i, int j, String s1, String s2, String s3, String d1, String d2, String t1, String t2, String alarm, String eventid, String aa) {
        Log.i("received i", String.valueOf(i));
        Log.i("received j", String.valueOf(j));
        Log.i("received s1", s1);
        Log.i("received s2", s2);
        Log.i("received s3", s3);
        Log.i("received d1", d1);
        Log.i("received d2", d2);
        Log.i("received t1", t1);
        Log.i("received t2", t2);
        Log.i("received al", alarm);
        Log.i("received id", eventid);
        Log.i("received at", aa);


        sch = s1;
        map = s2;
        mem = s3;
        date1 = d1;
        date2 = d2;
        time1 = t1;
        time2 = t2;
        alarm1 = alarm;
        eid = eventid;
        aaa = aa;


        if(alarm == "?????? ??????") { alarmTime = -1; }
        else if(alarm == "?????? ????????????") { alarmTime = 0; }
        else if(alarm == "5??? ???") { alarmTime = 5; }
        else if(alarm == "10??? ???") { alarmTime = 10; }
        else if(alarm == "15??? ???") { alarmTime = 15; }
        else if(alarm == "30??? ???") { alarmTime = 30; }
        else if(alarm == "1?????? ???") { alarmTime = 60; }
        else if(alarm == "2?????? ???") { alarmTime = 60*2; }
        else if(alarm == "1??? ???") { alarmTime = 60*24; }
        else if(alarm == "1??? ???") { alarmTime = 60*24*7; }

        if(aa == "????????? ??????") {
            aaa = null;
        }

        if(j == 1) {
            if (i == 2) { mID = 2; }
            else if (i == 4) { mID = 4; }
        }
        else if(j == 2) {
            if (i == 2) { mID = 5; }
            else if (i == 4) { mID = 6; }
        }
        getResultsFromApi();


    }



    @Override
    public void sendDateTime(String s1, String s2, String s3) {

    }

    @Override
    public void sendId(String sendid) {
        Log.i("received sendid", sendid);
        eid = sendid;
        mID=7;
        getResultsFromApi();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }


    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();

        return super.onOptionsItemSelected(menuItem);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.nav1) { onFragmentSelected(1, null); }
        else if (id == R.id.nav2) { onFragmentSelected(2, null); }
        else if (id == R.id.nav3) { onFragmentSelected(3, null); }


        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentSelected(int position, Bundle bundle) {
        Fragment fragment = null;
        switch (position) {
            case 1:
                fragment = fragment1;
                toolbar.setTitle("??????");
                break;
            case 2:
                fragment = fragment2;
                toolbar.setTitle("??????");
                break;
            case 3:
                fragment = fragment3;
                toolbar.setTitle("??????");
                break;
            case 4:
                fragment = fragment2dialog;
                toolbar.setTitle("??????");
                break;
            default:
                break;
        }
        fragmentStack.push(fragment);
        manager.beginTransaction().replace(R.id.container, fragment).commit();
        Log.i("stack", String.valueOf(fragmentStack.size()));
    }

    protected void onDestroy() {
        // close database
        if (database != null) {
            database.close();
            database = null;
        }
        super.onDestroy();
    }



    @Override
    public void insert(String sch, String map, String mem, String date1, String time1, String date2, String time2, String al, int ty, String at) {
        database.insertRecord(sch, map, mem, date1, time1, date2, time2, al, ty, at);
        Toast.makeText(getApplicationContext(), "????????? ??????????????????.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void update(String sch, String map, String mem, String date1, String time1, String date2, String time2, String al, int ty, String at, int date3) {
        database.updateRecord(sch, map, mem, date1, time1, date2, time2, al, ty, at, date3);
        Toast.makeText(getApplicationContext(), "????????? ??????????????????.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void delete(int id) {
        database.deleteRecord(id);
        Toast.makeText(getApplicationContext(), "????????? ??????????????????.", Toast.LENGTH_SHORT).show();
    }


    public int dateinfo(String date){
        int info = database.dateinfo(date);
        return info;
    }
    public void dateid() {
        database.dateid();
    }

    public int maxid(){
        int id = database.maxid();
        return id;
    }

    @Override
    public ArrayList<ListViewItem> selectDate(String date) {
        ArrayList<ListViewItem> result = database.selectDate(date);

        return result;
    }

    @Override
    public ArrayList<ListViewItem> selectAll() {
        ArrayList<ListViewItem> result = database.selectAll();

        return result;
    }


    /* ?????? ?????? ????????? ?????? ???????????? Google Calendar API??? ????????? ??? ??????.
     * ?????? ??????
     *     - Google Play Services ??????
     *     - ????????? ?????? ?????? ??????
     *     - ??????????????? ?????????????????? ????????? ?????? ??????
     * ???????????? ???????????? ????????? ?????? ????????? ??????????????? ??????.*/
    protected String getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) { // Google Play Services??? ????????? ??? ?????? ??????
            acquireGooglePlayServices();
        }
        else if (mCredential.getSelectedAccountName() == null) { // ????????? Google ????????? ???????????? ?????? ?????? ??????
            chooseAccount();
        }
        else if (!isDeviceOnline()) {    // ???????????? ????????? ??? ?????? ??????
            Toast.makeText(getApplicationContext(), "No network connection available.", Toast.LENGTH_SHORT).show();
        }
        else {// Google Calendar API ??????
            new MakeRequestTask(this, mCredential).execute();
        }
        return null;
    }

    //??????????????? ??????????????? ?????? ????????? Google Play Services??? ???????????? ????????? ??????
    protected boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    //Google Play Services ??????????????? ????????????????????? ???????????? ?????? ???????????? ????????????????????? ???????????? ?????? ??????????????? ?????????.
    protected void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    //??????????????? ??????????????? Google Play Services??? ?????? ????????? ????????? ????????? ????????? ?????? ???????????? ????????????
    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(MainActivity.this, connectionStatusCode, REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    //Google Calendar API??? ?????? ??????( credentials ) ??? ????????? ?????? ????????? ????????????.
    //?????? ???????????? ?????? ????????? ????????? ?????? ????????? ????????????????????? ???????????? ??????????????? ??????.
    // GET_ACCOUNTS ???????????? ????????????.
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    protected void chooseAccount() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {  // GET_ACCOUNTS ????????? ????????? ?????????
            // SharedPreferences?????? ????????? Google ?????? ????????? ????????????.
            String accountName = getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) { // ????????? ?????? ?????? ???????????? ????????????.
                mCredential.setSelectedAccountName(accountName);
                accountsave(accountName);      //?????? ???????????? ?????? ????????? ?????? ??????
                getResultsFromApi();
            }
            else {  // ???????????? ?????? ????????? ????????? ??? ?????? ?????????????????? ????????????.
                startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            }
        }
        else { // GET_ACCOUNTS ????????? ????????? ?????? ????????? ??????????????? GET_ACCOUNTS ????????? ???????????? ?????????????????? ????????????.(????????? ?????? ?????????)
            EasyPermissions.requestPermissions((Activity)this, "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS, Manifest.permission.GET_ACCOUNTS);
        }
    }

    //?????? ????????? ????????? ???????????? ???????????????, ?????? ?????? ?????? ???????????????, ?????? ????????????????????? ??????????????? ????????????.
    @Override
    protected void onActivityResult(
            int requestCode,  // onActivityResult??? ??????????????? ??? ?????? ????????? ????????? ??????
            int resultCode,   // ????????? ?????? ?????? ??????
            Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "?????? ?????????????????? ?????? ????????? ???????????? ???????????????.\"\n" +
                            "+ \"?????? ????????? ???????????? ?????? ??? ?????? ???????????????.", Toast.LENGTH_SHORT).show();
                }
                else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER_CHANGE:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                    }
                }
                break;
        }
    }

    //Android 6.0 (API 23) ???????????? ????????? ?????? ????????? ????????? ????????????
    @Override
    public void onRequestPermissionsResult(
            int requestCode,  //requestPermissions(android.app.Activity, String, int, String[])?????? ????????? ?????? ??????
            @NonNull String[] permissions, // ????????? ?????????
            @NonNull int[] grantResults    // ????????? ?????? ??????. PERMISSION_GRANTED ?????? PERMISSION_DENIED
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    //EasyPermissions ?????????????????? ???????????? ????????? ????????? ???????????? ????????? ?????? ????????????.
    @Override
    public void onPermissionsGranted(int requestCode, List<String> requestPermissionList) {
        // ???????????? ?????? ??????
    }

    //EasyPermissions ?????????????????? ???????????? ????????? ????????? ???????????? ????????? ?????? ????????????.
    @Override
    public void onPermissionsDenied(int requestCode, List<String> requestPermissionList) {
        // ???????????? ?????? ??????
    }

    //??????????????? ??????????????? ????????? ???????????? ????????? ????????????. ???????????? ????????? True ??????, ????????? False ??????
    protected boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    //????????? ????????? ???????????? ????????? ID??? ??????
    protected String getCalendarID(String calendarTitle){
        String id = null;
        // Iterate through entries in calendar list
        String pageToken = null;
        do {
            CalendarList calendarList = null;
            try {
                calendarList = mService.calendarList().list().setPageToken(pageToken).execute();
            } catch (UserRecoverableAuthIOException e) {
                startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
            }catch (IOException e) {
                e.printStackTrace();
            }
            List<CalendarListEntry> items = calendarList.getItems();

            for (CalendarListEntry calendarListEntry : items) {
                if ( calendarListEntry.getSummary().toString().equals(calendarTitle)) {
                    id = calendarListEntry.getId().toString();
                }
            }
            pageToken = calendarList.getNextPageToken();
        } while (pageToken != null);
        return id;
    }


    //?????????????????? Google Calendar API ??????
    protected class MakeRequestTask extends AsyncTask<Void, Void, String> {
        private Exception mLastError = null;
        private MainActivity mActivity;
        List<String> eventStrings = new ArrayList<String>();
        public MakeRequestTask(MainActivity activity, GoogleAccountCredential credential) {
            mActivity = activity;
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar
                    .Builder(transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build();
        }

        @Override
        protected void onPreExecute() {
            // mStatusText.setText("");
            mProgress.show();
        }

        //????????????????????? Google Calendar API ?????? ??????
        @Override
        protected String doInBackground(Void... params) {
            try {
                if ( mID == 1) { return createCalendar(); }
                else if (mID == 2) { return addEvent(); }
                else if (mID == 3) { return getEvent(); }
                else if (mID == 4) { return addEvent2(); }
                else if (mID == 5) { return updateEvent(); }
                else if (mID == 6) { return updateEvent2(); }
                else if (mID == 7) { return deleteEvent(); }
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
            return null;
        }


        protected String deleteEvent() throws IOException {
            String calendarID = getCalendarID("CalendarTitle");
            if ( calendarID == null ){
                return "???????????? ?????? ???????????????.";
            }
            String eventID = eid;
            mService.events().delete(calendarID, eventID).execute();
            String eventStrings = "?????? ????????? ??????????????????." ;
            return eventStrings;
        }

        //CalendarTitle ????????? ??????????????? 10?????? ???????????? ????????? ??????
        protected String getEvent() throws IOException {
            DateTime now = new DateTime(System.currentTimeMillis());
            String calendarID = getCalendarID("CalendarTitle");
            if ( calendarID == null ){
                return "???????????? ?????? ???????????????.";
            }

            Events events = mService.events().list(calendarID)//"primary")      //????????? ?????????
                    .setMaxResults(1000)
                    //.setTimeMin(DateTime.parseRfc3339(datetime))  //????????????
                    //.setTimeMax(DateTime.parseRfc3339(datetime2))     //?????????
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

            List<Event> items = events.getItems();
            for (Event event : items) {
                String eventID = event.getICalUID();                // id
                String eventSummary = event.getSummary();           // ??????
                String eventLocation = event.getLocation();         // ??????
                String eventDescription = event.getDescription();   // ??????
                String eventReminders = String.valueOf(event.getReminders());       // ??????     ???????????? ????????? ??????????????? ?????? ??????

                List<EventAttendee> eventAttendee = event.getAttendees();
                String asd = String.valueOf(eventAttendee.get(0));
                

                String startD, startT = null, endD, endT = null;

                DateTime start = event.getStart().getDateTime();
                DateTime end = event.getEnd().getDateTime();
                int type = 2;
                if (start == null) {        // ?????? 0000-00-00                   ????????????
                    // ?????? ???????????? ?????? ????????? ?????? ????????? ??????. ?????? ?????? ?????? ????????? ??????
                    start = event.getStart().getDate();
                    end = event.getEnd().getDate();
                    startD = String.valueOf(start);
                    endD = String.valueOf(end);
                    type = 4;

                }
                else {                      // ?????? 0000-00-00'T'00:00:00+09:00   ?????? ~ ??????
                    String splitStart[] = (String.valueOf(start)).split("'");
                    String splitEnd[] = (String.valueOf(end)).split("'");

                    String startDate = splitStart[0];
                    String startTime = splitStart[2];
                    String splitStart2[] = startTime.split(":");
                    startTime = splitStart2[0] + ":" + splitStart2[1];

                    String endDate = splitEnd[0];
                    String endTime = splitEnd[2];
                    String splitEnd2[] = endTime.split(":");
                    endTime = splitEnd2[0] + ":" + splitEnd2[1];

                    startD = startDate;
                    startT = startTime;
                    endD = endDate;
                    endT = endTime;
                }
                String asdfghj = eventID + ", " + eventSummary + ", " + eventLocation + ", " + eventDescription + ", " + startD + ", " + startT + ", " + endT + ", " + endT + ", " + eventReminders + ", " + type + ", " + asd;
                Log.i("info", asdfghj);
                if(dateinfo(eventID) == 0) { // db??? ?????? ??????
                    CalDatabase database = CalDatabase.getInstance(getApplicationContext());
                    database.execSQL( "insert into Schedule values ("+ eventID + ", '" + eventSummary + "', '" + eventLocation + "', '" + eventDescription + "', '" + startD + "', '" + startT + "', '" + endD + "', '" + endT + "', '" + eventReminders + "', " + type + ", '" + asd + "');");

                    //insert(eventSummary, eventLocation, eventDescription, startD, startT, endD, endT, null, type);
                    //database.execSQL( "update Schedule SET _id="+ eventID +" WHERE _id=" + dateid() + ";" );
                }
                else {  // db??? ?????? ??????
                    update(eventSummary, eventLocation, eventDescription, startD, startT, endD, endT, eventReminders, type, asd,Integer.parseInt(eventID));
                }
                eventStrings.add(String.format("%s \n (%s)", event.getSummary(), start));
            }
            return eventStrings.size() + "?????? ???????????? ??????????????????.";
        }

         //???????????? ?????? Google ????????? ??? ???????????? ????????????
         protected String createCalendar() throws IOException {
            String ids = getCalendarID("CalendarTitle");
            if ( ids != null ){
                return "?????? ???????????? ???????????? ????????????. ";
            }
            // ????????? ????????? ??????
            com.google.api.services.calendar.model.Calendar calendar = new Calendar();
            // ???????????? ?????? ??????
            calendar.setSummary("CalendarTitle");
            // ???????????? ????????? ??????
            calendar.setTimeZone("Asia/Seoul");
            // ?????? ???????????? ?????? ?????? ???????????? ??????
            Calendar createdCalendar = mService.calendars().insert(calendar).execute();
            // ????????? ???????????? ID??? ?????????.
            String calendarId = createdCalendar.getId();
            // ?????? ???????????? ????????? ???????????? ?????? ?????? ???????????? ??????
            CalendarListEntry calendarListEntry = mService.calendarList().get(calendarId).execute();
            // ???????????? ???????????? ??????????????? ??????  RGB
            calendarListEntry.setBackgroundColor("#0000ff");
            // ????????? ????????? ?????? ???????????? ??????
            CalendarListEntry updatedCalendarListEntry =
                    mService.calendarList()
                            .update(calendarListEntry.getId(), calendarListEntry)
                            .setColorRgbFormat(true)
                            .execute();
            // ?????? ????????? ???????????? ID??? ??????
            return "???????????? ?????????????????????.";
        }
        @Override
        protected void onPostExecute(String output) {
            mProgress.hide();
            /*mStatusText.setText(output);
            if ( mID == 3 )   mResultText.setText(TextUtils.join("\n\n", eventStrings));*/
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    //mStatusText.setText("MakeRequestTask The following error occurred:\n" + mLastError.getMessage());
                }
            } else {
                Toast.makeText(getApplicationContext(), "?????? ?????????.", Toast.LENGTH_SHORT).show();
            }
        }

        protected String updateEvent() throws IOException {
            String calendarID = getCalendarID("CalendarTitle");
            if ( calendarID == null ){
                return "???????????? ?????? ???????????????.";
            }
            String eventID = eid;

            Event event = mService.events().get(calendarID, eventID).execute();

            event.setSummary(sch)
                    .setLocation(map)
                    .setDescription(mem);

            String datetime1 = date1+'T'+time1+":00+09:00";
            String datetime2 = date1+'T'+time2+":00+09:00";
            DateTime startDateTime1 = new DateTime(datetime1);
            DateTime endDateTime1 = new  DateTime(datetime2);
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime1)
                    .setTimeZone("Asia/Seoul");
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime1)
                    .setTimeZone("Asia/Seoul");
            event.setStart(start);
            event.setEnd(end);

            EventAttendee[] attendees;
            if(aaa != null) {
                attendees = new EventAttendee[] {
                        new EventAttendee().setEmail(aaa),
                };
            }

            int reTime = alarmTime;
            EventReminder[] reminders = new EventReminder[] { //4??? ????????? ?????? ??????
                    new EventReminder().setMethod("popup").setMinutes(reTime),
            };

            if(reTime != -1) {
                Event.Reminders reminders1 = new Event.Reminders()
                        .setUseDefault(false)
                        .setOverrides(Arrays.asList(reminders));
                event.setReminders(reminders1);
            }


            try {
                Event updateevent = mService.events().update(calendarID, event.getId(), event).execute();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Exception", "Exception : " + e.toString());
            }
            System.out.printf("Event created: %s\n", event.getHtmlLink());
            Log.e("Event", "created : " + event.getHtmlLink());
            String eventStrings = "created : " + event.getHtmlLink();

            return eventStrings;
        }

        protected String updateEvent2() throws IOException {
            String calendarID = getCalendarID("CalendarTitle");
            if ( calendarID == null ){
                return "???????????? ?????? ???????????????.";
            }
            String eventID = eid;

            Event event = mService.events().get(calendarID, eventID).execute();

            event.setSummary(sch)
                    .setLocation(map)
                    .setDescription(mem);

            String datetime1 = date1;
            String datetime2 = date2;
            DateTime startDateTime1 = new DateTime(datetime1);
            DateTime endDateTime1 = new  DateTime(datetime2);
            EventDateTime start = new EventDateTime()
                    .setDate(startDateTime1)
                    .setTimeZone("Asia/Seoul");
            EventDateTime end = new EventDateTime()
                    .setDate(endDateTime1)
                    .setTimeZone("Asia/Seoul");
            event.setStart(start);
            event.setEnd(end);

            EventAttendee[] attendees;
            if(aaa != null) {
                attendees = new EventAttendee[] {
                        new EventAttendee().setEmail(aaa),
                };
            }

            int reTime = alarmTime;
            EventReminder[] reminders = new EventReminder[] { //4??? ????????? ?????? ??????
                    new EventReminder().setMethod("popup").setMinutes(reTime),
            };

            if(reTime != -1) {
                Event.Reminders reminders1 = new Event.Reminders()
                        .setUseDefault(false)
                        .setOverrides(Arrays.asList(reminders));
                event.setReminders(reminders1);
            }

            try {
                Event updateevent = mService.events().update(calendarID, event.getId(), event).execute();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Exception", "Exception : " + e.toString());
            }
            System.out.printf("Event created: %s\n", event.getHtmlLink());
            Log.e("Event", "created : " + event.getHtmlLink());
            String eventStrings = "created : " + event.getHtmlLink();

            return eventStrings;
        }

        protected String addEvent() {
            String calendarID = getCalendarID("CalendarTitle");
            if ( calendarID == null ){
                return "???????????? ?????? ???????????????.";
            }

            String eventID = eid;

            Log.i("mid", String.valueOf(mID));
            Log.i("sch", sch);
            Log.i("map", map);
            Log.i("mem", mem);

            Event event = new Event()
                    .setSummary(sch)
                    .setLocation(map)
                    .setDescription(mem)
                    .setId(eventID);

                    //.setReminders()  ???????????? ????????? ??????????????? ?????? ??????
            java.util.Calendar calander;
            calander = java.util.Calendar.getInstance();
            //SimpleDateFormat simpledateformat;
            //simpledateformat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssZ", Locale.KOREA);
            // Z??? ???????????? +0900??? ???????????? ?????? ?????? ??????????????? ??????
            //simpledateformat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:00+09:00", Locale.KOREA);
            //String datetime = simpledateformat.format(calander.getTime());
            String datetime1 = date1+'T'+time1+":00+09:00";
            String datetime2 = date1+'T'+time2+":00+09:00";
            DateTime startDateTime1 = new DateTime(datetime1);
            DateTime endDateTime1 = new  DateTime(datetime2);
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime1)
                    .setTimeZone("Asia/Seoul");
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime1)
                    .setTimeZone("Asia/Seoul");
            event.setStart(start);
            event.setEnd(end);

            EventAttendee[] attendees;
            if(aaa != null) {
                attendees = new EventAttendee[] {
                        new EventAttendee().setEmail(aaa),
                };
            }

            int reTime = alarmTime;
            EventReminder[] reminders = new EventReminder[] { //4??? ????????? ?????? ??????
                    new EventReminder().setMethod("popup").setMinutes(reTime),
            };

            if(reTime != -1) {
                Event.Reminders reminders1 = new Event.Reminders()
                        .setUseDefault(false)
                        .setOverrides(Arrays.asList(reminders));
                event.setReminders(reminders1);
            }

            //String[] recurrence = new String[]{"RRULE:FREQ=DAILY;COUNT=2"};
            //event.setRecurrence(Arrays.asList(recurrence));
            try {
                event = mService.events().insert(calendarID, event).execute();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Exception", "Exception : " + e.toString());
            }
            System.out.printf("Event created: %s\n", event.getHtmlLink());
            Log.e("Event", "created : " + event.getHtmlLink());
            String eventStrings = "created : " + event.getHtmlLink();
            return eventStrings;
        }

        protected String addEvent2() {
            String calendarID = getCalendarID("CalendarTitle");
            if ( calendarID == null ){
                return "???????????? ?????? ???????????????.";
            }

            String eventID = eid;

            Log.i("mid", String.valueOf(mID));
            Log.i("sch", sch);
            Log.i("map", map);
            Log.i("mem", mem);
            Event event = new Event()
                    .setSummary(sch)
                    .setLocation(map)
                    .setDescription(mem)
                    .setId(eventID);
            java.util.Calendar calander;
            calander = java.util.Calendar.getInstance();
            //SimpleDateFormat simpledateformat;
            //simpledateformat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssZ", Locale.KOREA);
            // Z??? ???????????? +0900??? ???????????? ?????? ?????? ??????????????? ??????
            //simpledateformat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:00+09:00", Locale.KOREA);
            //String datetime = simpledateformat.format(calander.getTime());
            Log.i("mID", String.valueOf(mID));
            Log.i("date1", date1);
            Log.i("date2", date2);
            String datetime1 = date1;
            String datetime2 = date2;
            DateTime startDateTime1 = new DateTime(datetime1);
            DateTime endDateTime1 = new  DateTime(datetime2);
            EventDateTime start = new EventDateTime()
                    .setDate(startDateTime1)
                    .setTimeZone("Asia/Seoul");
            EventDateTime end = new EventDateTime()
                    .setDate(endDateTime1)
                    .setTimeZone("Asia/Seoul");
            event.setStart(start);
            event.setEnd(end);

            EventAttendee[] attendees;
            if(aaa != null) {
                attendees = new EventAttendee[] {
                        new EventAttendee().setEmail(aaa),
                };
            }

            int reTime = alarmTime;
            EventReminder[] reminders = new EventReminder[] { //4??? ????????? ?????? ??????
                    new EventReminder().setMethod("popup").setMinutes(reTime),
            };

            if(reTime != -1) {
                Event.Reminders reminders1 = new Event.Reminders()
                        .setUseDefault(false)
                        .setOverrides(Arrays.asList(reminders));
                event.setReminders(reminders1);
            }

            //String[] recurrence = new String[]{"RRULE:FREQ=DAILY;COUNT=2"};
            //event.setRecurrence(Arrays.asList(recurrence));
            try {
                event = mService.events().insert(calendarID, event).execute();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Exception", "Exception : " + e.toString());
            }
            System.out.printf("Event created: %s\n", event.getHtmlLink());
            Log.e("Event", "created : " + event.getHtmlLink());
            String eventStrings = "created : " + event.getHtmlLink();
            return eventStrings;
        }
    }

    public void accountsave(String s) {
        text.setText(s);
    }
}
