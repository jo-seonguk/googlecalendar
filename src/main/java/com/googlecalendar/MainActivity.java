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

    private boolean logoutchecked = false; //false = 로그인, true = 로그아웃 상태
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
                mID = 1;           //캘린더 생성
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

        // Google Calendar API 호출중에 표시되는 ProgressDialog
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Google Calendar API 호출 중입니다.");

        // Google Calendar API 사용하기 위해 필요한 인증 초기화( 자격 증명 credentials, 서비스 객체 )
        // OAuth 2.0를 사용하여 구글 계정 선택 및 인증하기 위한 준비
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(),
                Arrays.asList(SCOPES)
        ).setBackOff(new ExponentialBackOff()); // I/O 예외 상황을 대비해서 백오프 정책 사용

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


        if(alarm == "알림 안함") { alarmTime = -1; }
        else if(alarm == "일정 시작시간") { alarmTime = 0; }
        else if(alarm == "5분 전") { alarmTime = 5; }
        else if(alarm == "10분 전") { alarmTime = 10; }
        else if(alarm == "15분 전") { alarmTime = 15; }
        else if(alarm == "30분 전") { alarmTime = 30; }
        else if(alarm == "1시간 전") { alarmTime = 60; }
        else if(alarm == "2시간 전") { alarmTime = 60*2; }
        else if(alarm == "1일 전") { alarmTime = 60*24; }
        else if(alarm == "1주 전") { alarmTime = 60*24*7; }

        if(aa == "참석자 추가") {
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
                toolbar.setTitle("달력");
                break;
            case 2:
                fragment = fragment2;
                toolbar.setTitle("일정");
                break;
            case 3:
                fragment = fragment3;
                toolbar.setTitle("검색");
                break;
            case 4:
                fragment = fragment2dialog;
                toolbar.setTitle("지도");
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
        Toast.makeText(getApplicationContext(), "일정을 추가했습니다.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void update(String sch, String map, String mem, String date1, String time1, String date2, String time2, String al, int ty, String at, int date3) {
        database.updateRecord(sch, map, mem, date1, time1, date2, time2, al, ty, at, date3);
        Toast.makeText(getApplicationContext(), "일정을 수정했습니다.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void delete(int id) {
        database.deleteRecord(id);
        Toast.makeText(getApplicationContext(), "일정을 삭제했습니다.", Toast.LENGTH_SHORT).show();
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


    /* 다음 사전 조건을 모두 만족해야 Google Calendar API를 사용할 수 있다.
     * 사전 조건
     *     - Google Play Services 설치
     *     - 유효한 구글 계정 선택
     *     - 안드로이드 디바이스에서 인터넷 사용 가능
     * 하나라도 만족하지 않으면 해당 사항을 사용자에게 알림.*/
    protected String getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) { // Google Play Services를 사용할 수 없는 경우
            acquireGooglePlayServices();
        }
        else if (mCredential.getSelectedAccountName() == null) { // 유효한 Google 계정이 선택되어 있지 않은 경우
            chooseAccount();
        }
        else if (!isDeviceOnline()) {    // 인터넷을 사용할 수 없는 경우
            Toast.makeText(getApplicationContext(), "No network connection available.", Toast.LENGTH_SHORT).show();
        }
        else {// Google Calendar API 호출
            new MakeRequestTask(this, mCredential).execute();
        }
        return null;
    }

    //안드로이드 디바이스에 최신 버전의 Google Play Services가 설치되어 있는지 확인
    protected boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    //Google Play Services 업데이트로 해결가능하다면 사용자가 최신 버전으로 업데이트하도록 유도하기 위해 대화상자를 보여줌.
    protected void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    //안드로이드 디바이스에 Google Play Services가 설치 안되어 있거나 오래된 버전인 경우 보여주는 대화상자
    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(MainActivity.this, connectionStatusCode, REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    //Google Calendar API의 자격 증명( credentials ) 에 사용할 구글 계정을 설정한다.
    //전에 사용자가 구글 계정을 선택한 적이 없다면 다이얼로그에서 사용자를 선택하도록 한다.
    // GET_ACCOUNTS 퍼미션이 필요하다.
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    protected void chooseAccount() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {  // GET_ACCOUNTS 권한을 가지고 있다면
            // SharedPreferences에서 저장된 Google 계정 이름을 가져온다.
            String accountName = getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) { // 선택된 구글 계정 이름으로 설정한다.
                mCredential.setSelectedAccountName(accountName);
                accountsave(accountName);      //헤더 텍스트에 계정 이메일 정보 출력
                getResultsFromApi();
            }
            else {  // 사용자가 구글 계정을 선택할 수 있는 다이얼로그를 보여준다.
                startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            }
        }
        else { // GET_ACCOUNTS 권한을 가지고 있지 않다면 사용자에게 GET_ACCOUNTS 권한을 요구하는 다이얼로그를 보여준다.(주소록 권한 요청함)
            EasyPermissions.requestPermissions((Activity)this, "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS, Manifest.permission.GET_ACCOUNTS);
        }
    }

    //구글 플레이 서비스 업데이트 다이얼로그, 구글 계정 선택 다이얼로그, 인증 다이얼로그에서 되돌아올때 호출된다.
    @Override
    protected void onActivityResult(
            int requestCode,  // onActivityResult가 호출되었을 때 요청 코드로 요청을 구분
            int resultCode,   // 요청에 대한 결과 코드
            Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "앱을 실행시키려면 구글 플레이 서비스가 필요합니다.\"\n" +
                            "+ \"구글 플레이 서비스를 설치 후 다시 실행하세요.", Toast.LENGTH_SHORT).show();
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

    //Android 6.0 (API 23) 이상에서 런타임 권한 요청시 결과를 리턴받음
    @Override
    public void onRequestPermissionsResult(
            int requestCode,  //requestPermissions(android.app.Activity, String, int, String[])에서 전달된 요청 코드
            @NonNull String[] permissions, // 요청한 퍼미션
            @NonNull int[] grantResults    // 퍼미션 처리 결과. PERMISSION_GRANTED 또는 PERMISSION_DENIED
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    //EasyPermissions 라이브러리를 사용하여 요청한 권한을 사용자가 승인한 경우 호출된다.
    @Override
    public void onPermissionsGranted(int requestCode, List<String> requestPermissionList) {
        // 아무일도 하지 않음
    }

    //EasyPermissions 라이브러리를 사용하여 요청한 권한을 사용자가 거부한 경우 호출된다.
    @Override
    public void onPermissionsDenied(int requestCode, List<String> requestPermissionList) {
        // 아무일도 하지 않음
    }

    //안드로이드 디바이스가 인터넷 연결되어 있는지 확인한다. 연결되어 있다면 True 리턴, 아니면 False 리턴
    protected boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    //캘린더 이름에 대응하는 캘린더 ID를 리턴
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


    //비동기적으로 Google Calendar API 호출
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

        //백그라운드에서 Google Calendar API 호출 처리
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
                return "캘린더를 먼저 생성하세요.";
            }
            String eventID = eid;
            mService.events().delete(calendarID, eventID).execute();
            String eventStrings = "해당 일정을 삭제했습니다." ;
            return eventStrings;
        }

        //CalendarTitle 이름의 캘린더에서 10개의 이벤트를 가져와 리턴
        protected String getEvent() throws IOException {
            DateTime now = new DateTime(System.currentTimeMillis());
            String calendarID = getCalendarID("CalendarTitle");
            if ( calendarID == null ){
                return "캘린더를 먼저 생성하세요.";
            }

            Events events = mService.events().list(calendarID)//"primary")      //이벤트 받아옴
                    .setMaxResults(1000)
                    //.setTimeMin(DateTime.parseRfc3339(datetime))  //시작날짜
                    //.setTimeMax(DateTime.parseRfc3339(datetime2))     //끝날짜
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

            List<Event> items = events.getItems();
            for (Event event : items) {
                String eventID = event.getICalUID();                // id
                String eventSummary = event.getSummary();           // 제목
                String eventLocation = event.getLocation();         // 위치
                String eventDescription = event.getDescription();   // 메모
                String eventReminders = String.valueOf(event.getReminders());       // 알람     알람기능 어떻게 사용하는지 확인 필요

                List<EventAttendee> eventAttendee = event.getAttendees();
                String asd = String.valueOf(eventAttendee.get(0));
                

                String startD, startT = null, endD, endT = null;

                DateTime start = event.getStart().getDateTime();
                DateTime end = event.getEnd().getDateTime();
                int type = 2;
                if (start == null) {        // 형식 0000-00-00                   하루종일
                    // 모든 이벤트가 시작 시간을 갖고 있지는 않다. 그런 경우 시작 날짜만 사용
                    start = event.getStart().getDate();
                    end = event.getEnd().getDate();
                    startD = String.valueOf(start);
                    endD = String.valueOf(end);
                    type = 4;

                }
                else {                      // 형식 0000-00-00'T'00:00:00+09:00   시간 ~ 시간
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
                if(dateinfo(eventID) == 0) { // db에 없는 경우
                    CalDatabase database = CalDatabase.getInstance(getApplicationContext());
                    database.execSQL( "insert into Schedule values ("+ eventID + ", '" + eventSummary + "', '" + eventLocation + "', '" + eventDescription + "', '" + startD + "', '" + startT + "', '" + endD + "', '" + endT + "', '" + eventReminders + "', " + type + ", '" + asd + "');");

                    //insert(eventSummary, eventLocation, eventDescription, startD, startT, endD, endT, null, type);
                    //database.execSQL( "update Schedule SET _id="+ eventID +" WHERE _id=" + dateid() + ";" );
                }
                else {  // db에 있는 경우
                    update(eventSummary, eventLocation, eventDescription, startD, startT, endD, endT, eventReminders, type, asd,Integer.parseInt(eventID));
                }
                eventStrings.add(String.format("%s \n (%s)", event.getSummary(), start));
            }
            return eventStrings.size() + "개의 데이터를 가져왔습니다.";
        }

         //선택되어 있는 Google 계정에 새 캘린더를 추가한다
         protected String createCalendar() throws IOException {
            String ids = getCalendarID("CalendarTitle");
            if ( ids != null ){
                return "이미 캘린더가 생성되어 있습니다. ";
            }
            // 새로운 캘린더 생성
            com.google.api.services.calendar.model.Calendar calendar = new Calendar();
            // 캘린더의 제목 설정
            calendar.setSummary("CalendarTitle");
            // 캘린더의 시간대 설정
            calendar.setTimeZone("Asia/Seoul");
            // 구글 캘린더에 새로 만든 캘린더를 추가
            Calendar createdCalendar = mService.calendars().insert(calendar).execute();
            // 추가한 캘린더의 ID를 가져옴.
            String calendarId = createdCalendar.getId();
            // 구글 캘린더의 캘린더 목록에서 새로 만든 캘린더를 검색
            CalendarListEntry calendarListEntry = mService.calendarList().get(calendarId).execute();
            // 캘린더의 배경색을 파란색으로 표시  RGB
            calendarListEntry.setBackgroundColor("#0000ff");
            // 변경한 내용을 구글 캘린더에 반영
            CalendarListEntry updatedCalendarListEntry =
                    mService.calendarList()
                            .update(calendarListEntry.getId(), calendarListEntry)
                            .setColorRgbFormat(true)
                            .execute();
            // 새로 추가한 캘린더의 ID를 리턴
            return "캘린더가 생성되었습니다.";
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
                Toast.makeText(getApplicationContext(), "요청 취소됨.", Toast.LENGTH_SHORT).show();
            }
        }

        protected String updateEvent() throws IOException {
            String calendarID = getCalendarID("CalendarTitle");
            if ( calendarID == null ){
                return "캘린더를 먼저 생성하세요.";
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
            EventReminder[] reminders = new EventReminder[] { //4주 전까지 알림 가능
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
                return "캘린더를 먼저 생성하세요.";
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
            EventReminder[] reminders = new EventReminder[] { //4주 전까지 알림 가능
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
                return "캘린더를 먼저 생성하세요.";
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

                    //.setReminders()  알람기능 어떻게 사용하는지 확인 필요
            java.util.Calendar calander;
            calander = java.util.Calendar.getInstance();
            //SimpleDateFormat simpledateformat;
            //simpledateformat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssZ", Locale.KOREA);
            // Z에 대응하여 +0900이 입력되어 문제 생겨 수작업으로 입력
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
            EventReminder[] reminders = new EventReminder[] { //4주 전까지 알림 가능
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
                return "캘린더를 먼저 생성하세요.";
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
            // Z에 대응하여 +0900이 입력되어 문제 생겨 수작업으로 입력
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
            EventReminder[] reminders = new EventReminder[] { //4주 전까지 알림 가능
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
