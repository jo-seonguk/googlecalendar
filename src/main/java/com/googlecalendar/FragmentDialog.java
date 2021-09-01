package com.googlecalendar;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

public class FragmentDialog extends DialogFragment {
    private static final String TAG = "dialog";
    private SharedViewModel sharedViewModel;
    private Fragment fragment;

    OnDatabaseCallback callback;
    ArrayList<Integer> calid = new ArrayList<Integer>();
    TextView selectdate;
    ListView listView01;
    Button nbtn, pbtn;

    ListViewAdapter adapter;
    Context ct;
    String date5;

    String shareInfo;
    String share;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callback = (OnDatabaseCallback) getActivity();
    }

    public FragmentDialog() {

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dialog, container, false);

        selectdate = view.findViewById(R.id.selectdate);
        listView01 = view.findViewById(R.id.listview01);
        nbtn = view.findViewById(R.id.nbtn);
        pbtn = view.findViewById(R.id.pbtn);
        adapter = new ListViewAdapter();
        listView01.setAdapter(adapter);


        listreset();
        nbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = (DialogFragment) fragment;
                dismiss();
            }
        });

        pbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedViewModel.setLiveData(shareInfo);
                dismiss();
                ((MainActivity)getActivity()).onFragmentSelected(2, null);
            }
        });

        listView01.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final CalDatabase database = CalDatabase.getInstance(ct);
                adapter.notifyDataSetChanged();
                int date6 = calid.get(position);
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
                        info = id + "⌒" + sch + "⌒" + add + "⌒" + memo + "⌒" + date1 + "˘" + time1 + "⌒" + date2 + "⌒" + time2 + "⌒" + al + "⌒" + aa + "⌒" + ty;
                        /*
                        Fragment2 fragment = new Fragment2();
                        Bundle bundle = new Bundle(1);
                        bundle.putString("key", info);
                        fragment.setArguments(bundle);*/

                        sharedViewModel.setLiveData(info);
                        dismiss();
                        ((MainActivity)getActivity()).onFragmentSelected(2, null);
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getLiveData().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                shareInfo = s;
                String splitText[] = shareInfo.split("⌒");
                share = splitText[0];
                selectdate.setText(share);
                date5 = s;

            }
        });
    }

    public void listreset() {
        final CalDatabase database = CalDatabase.getInstance(ct);


        //String sql = "select * from Schedule";
        date5 = getArguments().getString("key");
        final String sql = "select * from Schedule where date1='" + date5 +"'";
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
                Log.i("equals", selectdate.getText().toString());
                Log.i("equals", date5);
                Log.i("equals", date1);
                Log.i("data log", log);
                calid.add(id);
                adapter.addItem(id, sch, dt, al, add);
            }

        }
        adapter.notifyDataSetChanged();
    }

}
