package com.googlecalendar;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


@RequiresApi(api = Build.VERSION_CODES.N)
public class Fragment2 extends Fragment implements FragmentToActivity{
    private SharedViewModel sharedViewModel;
    EditText sch, memo, inedit;
    TextView start2, start3, cid, alarm, start12, start13, intext, name1;
    LinearLayout layout1, layout2, layout3, dp1, dp2, listlayout, mainlayout, name1layout;
    Switch switch1;
    MaterialCalendarView materialCalendarView1, materialCalendarView2;
    ListView list01;
    Button inbutton;
    WebView webView;
    Handler handler;
    WebSettings settings;

    int type = 2;          //?????? ~ ?????? or ?????? ~ ??????
    int t = 1;
    int timepicker1 = 1; // start3, 13 ??? ???????????? ????????? ??? ??????

    String dateinfo;
    String[] splitText;

    Context ct;
    String d1, t1, t2;
    int year1, month1, day1, hour1, minute1;    // ?????????????????? ????????? ???, ???, ???, ???, ???

    long now = System.currentTimeMillis();
    Date mDate = new Date(now);
    SimpleDateFormat simpleTime  = new SimpleDateFormat("HH:mm");
    Calendar cal = Calendar.getInstance();
    String getTime, getTime2;

    OnDatabaseCallback callback;

    private FragmentToActivity mCallback;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callback = (OnDatabaseCallback) getActivity();
        try {
            mCallback = (FragmentToActivity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement FragmentToActivity");
        }
    }

    class MyJavaScriptInterface {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processDATA(String data) {

        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) getLayoutInflater().inflate(R.layout.fragment2, container, false);

        sch = rootView.findViewById(R.id.sch);                  //?????? ??????
        memo = rootView.findViewById(R.id.memo);                //??????
        start2 = rootView.findViewById(R.id.start2);            //?????? ?????? ??????
        start3 = rootView.findViewById(R.id.start3);            //?????? ?????? ??????
        start12 = rootView.findViewById(R.id.start12);          //?????? ?????? ??????
        start13 = rootView.findViewById(R.id.start13);          //?????? ?????? ??????
        name1 = rootView.findViewById(R.id.name1);              //??????
        cid = rootView.findViewById(R.id.cid);                  //id
        alarm = rootView.findViewById(R.id.alarm);              //??????
        intext = rootView.findViewById(R.id.intext);            //????????? ?????????
        inedit = rootView.findViewById(R.id.inedit);            //????????? ?????? ?????????
        inbutton = rootView.findViewById(R.id.inbutton);        //????????? ?????? ??????
        mainlayout = rootView.findViewById(R.id.mainlayout);    //?????? ????????????
        name1layout = rootView.findViewById(R.id.name1layout);  //?????? ????????????
        layout1 = rootView.findViewById(R.id.layout1);          //?????? ????????????
        layout2 = rootView.findViewById(R.id.layout2);          //?????? ????????????
        layout3 = rootView.findViewById(R.id.layout3);          //?????? ????????????           gone
        listlayout = rootView.findViewById(R.id.listlayout);    //????????? ????????? ????????????   gone
        dp1 = rootView.findViewById(R.id.dp1);                  //?????? ?????? ?????? ?????? ????????????
        dp2 = rootView.findViewById(R.id.dp2);                  //?????? ?????? ?????? ?????? ????????????
        switch1 = rootView.findViewById(R.id.switch1);          //????????????
        list01 = rootView.findViewById(R.id.list01);            //?????? ?????????
        materialCalendarView1 = rootView.findViewById(R.id.calendarView1);
        materialCalendarView2 = rootView.findViewById(R.id.calendarView2);

        webView = (WebView) rootView.findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());

/*
        cal.setTime(mDate);
        cal.add(Calendar.HOUR, +1);
        getTime = simpleTime.format(mDate);
        getTime2 = simpleTime.format(cal.getTime());
        start3.setText(getTime);
        start13.setText(getTime2);
*/
        ct = container.getContext();
        setHasOptionsMenu(true);

        layout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout3.setVisibility(View.VISIBLE);
            }
        });
        List<String> list02 = new ArrayList<>();
        list02.add("?????? ??????");
        list02.add("?????? ????????????");
        list02.add("5??? ???");
        list02.add("10??? ???");
        list02.add("15??? ???");
        list02.add("30??? ???");
        list02.add("1?????? ???");
        list02.add("2?????? ???");
        list02.add("1??? ???");
        list02.add("1??? ???");
        final ArrayAdapter<String> adapter01 = new ArrayAdapter<String>(ct, android.R.layout.simple_list_item_1, list02);
        list01.setAdapter(adapter01);
        list01.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String data = (String) parent.getItemAtPosition(position);
                alarm.setText(data);
                layout3.setVisibility(View.GONE);
            }
        });


        listlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(intext.getVisibility() == View.VISIBLE) {
                    visibility();
                }
                else {
                    gone();
                }
            }
        });

        inedit.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    Pattern p = Pattern.compile("^[a-zA-X0-9]@[a-zA-Z0-9].[a-zA-Z0-9]");
                    Matcher m = p.matcher(inedit.getText().toString());

                    if ( !m.matches()){
                        Toast.makeText(ct, "Email???????????? ???????????????", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        inbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intext.setText(inedit.getText().toString());
                gone();
            }
        });

        start2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {       // start2 ????????? ??? ?????? ??? ????????? ?????? ?????????
                dp1.setVisibility(View.VISIBLE);
                materialCalendarView1.state().edit()                                                                         // ?????? ??????
                        .setFirstDayOfWeek(java.util.Calendar.SUNDAY)
                        .setMinimumDate(CalendarDay.from(2010, 0, 1))                                      // ????????? ??????
                        .setMaximumDate(CalendarDay.from(2030, 11, 31))                                    // ????????? ???
                        .setCalendarDisplayMode(CalendarMode.MONTHS)                                                          // ????????? ??? ????????? ??????
                        .commit();

                materialCalendarView1.setOnDateChangedListener(new OnDateSelectedListener() {
                    @Override
                    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                        int Year = date.getYear();                      // ???
                        int Month = date.getMonth() + 1;                // ???
                        int Day = date.getDay();                        // ???
                        String s_day, m_day, d_day;
                        if(Month < 10) {
                            m_day = "-0" + Month;      // ???+???+???
                        }
                        else {
                            m_day = String.valueOf(Month);      // ???+???+???
                        }
                        if(Day < 10) {
                            d_day = "-0" + Day;      // ???+???+???
                        }
                        else {
                            d_day = String.valueOf(Day);      // ???+???+???
                        }
                        s_day = Year + m_day + d_day;
                        start2.setText(s_day);
                        dp1.setVisibility(View.GONE);
                    }
                });
            }
        });
        start12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {       // start2 ????????? ??? ?????? ??? ????????? ?????? ?????????
                if(switch1.isChecked()==true) {
                    dp2.setVisibility(View.VISIBLE);
                    materialCalendarView2.state().edit()                                                                         // ?????? ??????
                            .setFirstDayOfWeek(java.util.Calendar.SUNDAY)
                            .setMinimumDate(CalendarDay.from(2010, 0, 1))                                      // ????????? ??????
                            .setMaximumDate(CalendarDay.from(2030, 11, 31))                                    // ????????? ???
                            .setCalendarDisplayMode(CalendarMode.MONTHS)                                                          // ????????? ??? ????????? ??????
                            .commit();

                    materialCalendarView2.setOnDateChangedListener(new OnDateSelectedListener() {
                        @Override
                        public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                            int Year = date.getYear();                      // ???
                            int Month = date.getMonth() + 1;                // ???
                            int Day = date.getDay();                        // ???
                            String s_day, m_day, d_day;
                            if(Month < 10) {
                                m_day = "-0" + Month;      // ???+???+???
                            }
                            else {
                                m_day = String.valueOf(Month);      // ???+???+???
                            }
                            if(Day < 10) {
                                d_day = "-0" + Day;      // ???+???+???
                            }
                            else {
                                d_day = String.valueOf(Day);      // ???+???+???
                            }
                            s_day = Year + m_day + d_day;
                            start12.setText(s_day);
                            dp2.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });


        start3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {       // start3 ????????? ??? ?????? ??? ?????? ?????? ?????????
                // TODO Auto-generated method stub
                if(switch1.isChecked() ==false) {
                    timepicker1 =1;
                    new TimePickerDialog(ct, timeSetListener1, hour1, minute1, false).show();
                }
            }
        });
        start13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {       // start3 ????????? ??? ?????? ??? ?????? ?????? ?????????
                // TODO Auto-generated method stub
                if(switch1.isChecked() ==false) {
                    timepicker1 =2;
                    new TimePickerDialog(ct, timeSetListener1, hour1, minute1, false).show();
                }
            }
        });

        layout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainlayout.setVisibility(View.GONE);
                name1layout.setVisibility(View.VISIBLE);
                // WebView ?????????
                init_webView();

                // ???????????? ?????? JavaScript ????????? ??????
                handler = new Handler();

            }
        });
/*
        layout1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {     // ???????????? 1(?????? ??????) ????????? ?????????

                Fragment2Dialog fragment1 = new Fragment2Dialog();
                //????????????????????? rfgName????????? ?????? bundle
                Bundle bundle = new Bundle();
                bundle.putString("log", dateinfo);
                fragment1.setArguments(bundle); //Name ?????? ??? ??????. ????????? setArguments() ???????????? ???????????? ???
                //sharedViewModel.setLiveData(log);
                ((MainActivity)getActivity()).onFragmentSelected(4, null);
                return false;
            }
        });*/

        // ????????? ?????????(????????? ??? ?????? start3, fin3 ????????? ??? null, ???????????? ??? ?????? ?????????)
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(switch1.isChecked() == true) {
                    t1 = start3.getText().toString();
                    t2 = start13.getText().toString();
                    start3.setText(null);
                    start13.setText(null);
                    start12.setText(d1);
                }
                else {
                    d1 = start12.getText().toString();
                    start3.setText(t1);
                    start13.setText(t2);
                    start12.setText(null);
                }
            }
        });


        return rootView;
    }

    public void init_webView() {

        settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        // JavaScript ??????

        // JavaScript??? window.open ??????
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(true);

        // JavaScript???????????? ????????? ????????? ?????? ??? ???????????? ?????????
        webView.addJavascriptInterface(new Fragment2.AndroidBridge(), "TestApp");

        // web client ??? chrome ?????? ??????
        webView.setWebChromeClient(new WebChromeClient());

        // webview url load. php ?????? ??????
        webView.loadUrl("wewill10.dothome.co.kr/daum.html");
        //webView.loadUrl("file:///android_asset/daum.html");
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverriedUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });
        Bundle bundle = getArguments();  //?????? ??????. getArguments() ???????????? ??????.
        String name = null;
        if(bundle != null){
            name = bundle.getString("log"); //Name ??????.
            Log.i("log", name);
        }
    }

    private class AndroidBridge {
        @JavascriptInterface
        public void setAddress(final String arg1, final String arg2, final String arg3) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    name1.setText(String.format("%s %s", arg2, arg3));

                    // WebView??? ????????? ??????????????? ???????????? ??? ??????
                    init_webView();
                    mainlayout.setVisibility(View.VISIBLE);
                    name1layout.setVisibility(View.GONE);
                }
            });
        }
    }

    public void visibility() {
        intext.setVisibility(View.GONE);
        inedit.setVisibility(View.VISIBLE);
        inbutton.setVisibility(View.VISIBLE);
    }

    public void gone() {
        intext.setVisibility(View.VISIBLE);
        inedit.setVisibility(View.GONE);
        inbutton.setVisibility(View.GONE);
    }




    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getLiveData().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {


                if(!s.contains("???")) {
                    Log.i("1", String.valueOf(1));
                    name1.setText(s);
                }
                else if(s.length() < 30) {
                    Log.i("2", String.valueOf(2));
                    clearwindow();
                    dateinfo = s;
                    splitText = dateinfo.split("???");
                    start2.setText(splitText[0]);
                    start3.setText(splitText[1]);
                    start12.setText(splitText[0]);
                    start13.setText(splitText[2]);
                    d1 = splitText[0];
                    t = 1;

                }
                else {
                    Log.i("3", String.valueOf(3));
                    clearwindow();
                    dateinfo = s;
                    splitText = dateinfo.split("???");
                    cid.setText(splitText[0]);
                    sch.setText(splitText[1]);
                    name1.setText(splitText[2]);
                    memo.setText(splitText[3]);

                    String aasdf = splitText[4];
                    String[] splitText2 = aasdf.split("??");
                    start2.setText(splitText2[0]);
                    start3.setText(splitText2[1]);
                    start12.setText(splitText[5]);
                    start13.setText(splitText[6]);
                    alarm.setText(splitText[7]);
                    intext.setText(splitText[8]);
                    type = Integer.parseInt(splitText[9]);
                    Log.i("id", cid.getText().toString());
                    t = 2;
                }
            }
        });
    }

    @Override
    public void communicate(int i, int j, String sch, String map, String mem, String date1, String date2, String time1, String time2, String alarm1, String event, String aa) {

    }

    @Override
    public void sendDateTime(String s1, String s2, String s3) {

    }

    @Override
    public void sendId(String t1) {

    }

    @Override
    public void onDetach() {
        mCallback = null;
        super.onDetach();
    }

    public void onRefresh() {
        Toast.makeText(getActivity(), "Fragment : Refresh called.", Toast.LENGTH_SHORT).show();
    }

    private void sendData(int i, int j, String sch, String map, String mem, String date1, String date2, String time1, String time2, String alarm1, String eventid, String aa) {
        mCallback.communicate(i, j, sch, map, mem, date1, date2, time1, time2, alarm1, eventid, aa);
    }

    private void sentData2(String t0) {
        mCallback.sendId(t0);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu2, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu2,inflater);
        inflater.inflate(R.menu.fragment2_menu, menu2);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();        // ????????? ?????????

        if (id == R.id.menu21) { //??????

        }
        else if (id == R.id.menu22) { //??????
            clearwindow();
            ((MainActivity)getActivity()).onFragmentSelected(1, null);
        }
        else if (id == R.id.menu23) { //??????

            if (switch1.isChecked() == true) {
                type = 4;   //????????????
            }
            else {
                type = 2;   // ??????~??????
            }
            //int maxid = callback.dateid();
            if (t == 1) {
                callback.insert(sch.getText().toString(), name1.getText().toString(), memo.getText().toString(), start2.getText().toString(), start3.getText().toString(), start12.getText().toString(), start13.getText().toString(), alarm.getText().toString(), type, intext.getText().toString());
                CalDatabase database = CalDatabase.getInstance(ct);
                callback.dateid();
                cid.setText(String.valueOf(callback.maxid()));
                Log.i("id", cid.getText().toString());
            }
            else if (t == 2) {
                callback.update(sch.getText().toString(), name1.getText().toString(), memo.getText().toString(), start2.getText().toString(), start3.getText().toString(), start12.getText().toString(), start13.getText().toString(), alarm.getText().toString(), type, intext.getText().toString(), Integer.parseInt(cid.getText().toString()));
                Log.i("id", cid.getText().toString());
            }
            Log.i("id2", cid.getText().toString());
            sendData(type, t, sch.getText().toString(), name1.getText().toString(), memo.getText().toString(), start2.getText().toString(), start12.getText().toString(), start3.getText().toString(), start13.getText().toString(), alarm.getText().toString(), cid.getText().toString(), intext.getText().toString());
            clearwindow();
            ((MainActivity)getActivity()).onFragmentSelected(1, null);

        }
        else if (id == R.id.menu24) { //??????
            if(t==1) {
                Toast.makeText(getActivity(), "???????????? ?????? ???????????????.", Toast.LENGTH_SHORT).show();
            }
            else {
                callback.delete(Integer.parseInt(cid.getText().toString()));
                //sentData2(cid.getText().toString());
                clearwindow();
                ((MainActivity)getActivity()).onFragmentSelected(1, null);
            }

        }

        return super.onOptionsItemSelected(item);
    }

    //start3 ????????? ?????? ????????? ?????? ??????
    private TimePickerDialog.OnTimeSetListener timeSetListener1 = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay1, int minute1) {
            // TODO Auto-generated method stub
            String msg = null;
            String h = null;
            String m = null;
            if (hourOfDay1 < 10) {
                h = "0"+hourOfDay1;
            }
            else {
                h = String.valueOf(hourOfDay1);
            }
            if (minute1 < 10) {
                m = "0"+minute1;
            }
            else {
                m = String.valueOf(minute1);
            }
            msg = h+":"+m;

            if (timepicker1 == 1) {
                start3.setText(msg);
            }
            else {
                start13.setText(msg);
            }
        }
    };



    public void clearwindow() {
        sch.setText(null);
        name1.setText(null);
        memo.setText(null);
        /*
        start2.setText("0000-00-00");
        start3.setText("00:00");
        start12.setText("0000-00-00");
        start13.setText("00:00");*/
        alarm.setText("??????");
        switch1.setChecked(false);
        cid.setText(null);
        intext.setText(null);
        inedit.setText(null);
    }
}