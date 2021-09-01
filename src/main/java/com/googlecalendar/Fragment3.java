package com.googlecalendar;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class Fragment3 extends Fragment {

    private SharedViewModel sharedViewModel;
    EditText search_text;
    ListView list2;
    ListViewAdapter adapter1;
    OnDatabaseCallback callback;
    Context ct;
    String date5;
    ArrayList<Integer> calid = new ArrayList<Integer>();

    private BackPressCloseHandler backPressCloseHandler;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callback = (OnDatabaseCallback) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) getLayoutInflater().inflate(R.layout.fragment3, container, false);

        search_text = rootView.findViewById(R.id.search_text);
        list2 = rootView.findViewById(R.id.list2);
        adapter1 = new ListViewAdapter();
        list2.setAdapter(adapter1);
        setHasOptionsMenu(true);

        final CalDatabase database = CalDatabase.getInstance(ct);
        listclear();

        search_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                String searchText = s.toString();
                ((ListViewAdapter)list2.getAdapter()).getFilter().filter(searchText);
                adapter1.notifyDataSetChanged();
            }
        });

        list2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int date6 = adapter1.getFilteredItemId(position);
                //Integer.parseInt(adapter1.textView05.getText().toString());
                Log.i("date6", String.valueOf(date6));
                String sql1 = "select * from Schedule where _id='" + date6 +"'";
                int count = -1;
                String info = null;
                if(database != null) {
                    Cursor cursor = database.rawQuery(sql1);
                    count = cursor.getCount();

                    for (int i = 0; i < count; i++) {
                        cursor.moveToNext();
                        int idd = cursor.getInt(0);                    //id
                        String sch = cursor.getString(1);
                        String add = cursor.getString(2);
                        String memo = cursor.getString(3);
                        String date1 = cursor.getString(4);
                        String time1 = cursor.getString(5);
                        String date2 = cursor.getString(6);
                        String time2 = cursor.getString(7);
                        String al = cursor.getString(8);
                        int ty = cursor.getInt(9);
                        String aa = cursor.getString(10);

                        String log = idd + ", " + sch + ", " + add + ", " + memo + ", " + date1 + ", " + time1 + ", " + date2 + ", " + time2 + ", " + al + ", " + ty + ", " + aa;
                        Log.i("equals", String.valueOf(date6));
                        Log.i("equals", date1);
                        Log.i("data log", log);
                        info = idd + "⌒" + sch + "⌒" + add + "⌒" + memo + "⌒" + date1 + "˘" + time1 + "⌒" + date2 + "⌒" + time2 + "⌒" + al + "⌒" + aa + "⌒" + ty;

                        sharedViewModel.setLiveData(info);
                        ((MainActivity)getActivity()).onFragmentSelected(2, null);
                    }
                }
            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu3, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu3,inflater);
        inflater.inflate(R.menu.fragment3_menu, menu3);
/*
        SearchView searchView = (SearchView) menu3.findItem(R.id.menu31).getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setQueryHint("일정 제목을 검색합니다.");
        searchView.setOnQueryTextListener(queryTextListener);
*/
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();                                                                                         // 아이템 아이디
        if (id == R.id.menu31) {
            //검색
        }
        return super.onOptionsItemSelected(item);
    }

/*    private SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {

                                        나중에 할 수 있으면 에디트텍스트를 없애고 이 방식으로 구현하기
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            String searchText = newText.toString();
            ((ListViewAdapter)list2.getAdapter()).getFilter().filter(searchText);
            adapter1.notifyDataSetChanged();
            return false;
        }
    };*/

    public void listclear() {
        CalDatabase database = CalDatabase.getInstance(ct);
        String sql = "select * from Schedule";
        int count = -1;
        if(database != null) {
            Cursor cursor = database.rawQuery(sql);
            count = cursor.getCount();
            for (int i = 0; i < count; i++) {
                cursor.moveToNext();
                int id = cursor.getInt(0);					//id
                String sch = cursor.getString(1);
                String add = cursor.getString(2);
                String memo = cursor.getString(3);
                String date1 = cursor.getString(4);
                String time1 = cursor.getString(5);
                String date2 = cursor.getString(6);
                String time2 = cursor.getString(7);
                String al = cursor.getString(8);
                int ty = cursor.getInt(9);
                String aa = cursor.getString(10);

                String dt = null;
                if(ty == 4) {
                    dt = date1 + " ~ " + date2;
                }
                else if(ty == 2) {
                    dt = date1 + ", " + time1 + " ~ " + time2;
                }
                String log = id + ", " + sch + ", " + add + ", " + memo + ", " + date1 + ", " + time1 + ", " + date2 + ", " + time2 + ", " + al + ", " + ty + ", " + dt + ", " + aa;
                Log.i("data log", log);

                calid.add(id);
                adapter1.addItem(id, sch, dt, al, add);
            }

        }
        adapter1.notifyDataSetChanged();
    }
}