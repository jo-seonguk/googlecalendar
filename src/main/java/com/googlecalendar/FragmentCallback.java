package com.googlecalendar;

import android.os.Bundle;

import java.util.ArrayList;

public interface FragmentCallback {
    public void onFragmentSelected(int position, Bundle bundle);

    void insert(String sch, String map, String mem, String date1, String time1, String date2, String time2, String al, int ty, String at);

    ArrayList<ListViewItem> selectAll();
}
