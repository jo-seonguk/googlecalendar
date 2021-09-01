package com.googlecalendar;

import java.util.ArrayList;

public interface OnDatabaseCallback {
    public void insert(String sch, String map, String mem, String date1, String time1, String date2, String time2, String al, int ty, String at);
    public void update(String sch, String map, String mem, String date1, String time1, String date2, String time2, String al, int ty, String at, int date3);
    public void delete(int id);



    public int dateinfo(String date);

    public ArrayList<ListViewItem> selectDate(String date);
    public ArrayList<ListViewItem> selectAll();

    void dateid();
    int maxid();
}
