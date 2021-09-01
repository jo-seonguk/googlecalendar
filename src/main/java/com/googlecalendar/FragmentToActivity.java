package com.googlecalendar;

public interface FragmentToActivity {
    void communicate(int i, int j, String sch, String map, String mem, String date1, String date2, String time1, String time2, String alarm, String event, String aa);
    void sendDateTime(String s1, String s2, String s3);
    void sendId(String s1);
}
