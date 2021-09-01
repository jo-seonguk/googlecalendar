package com.googlecalendar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.googlecalendar.decorators.EventDecorator;
import com.googlecalendar.decorators.OneDayDecorator;
import com.googlecalendar.decorators.SaturdayDecorator;
import com.googlecalendar.decorators.SundayDecorator;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import static com.googlecalendar.MainActivity.fragmentStack;

@RequiresApi(api = Build.VERSION_CODES.N)
public class Fragment1 extends Fragment {
    MaterialCalendarView materialCalendarView;
    private SharedViewModel sharedViewModel;
    Context ct;
    String shot_Day;        //선택한 날짜

    String getDay;          //
    String getTime;         //
    String getTime2;        //
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


    public void currentDT() {
        long now = System.currentTimeMillis();
        Date mDate = new Date(now);
        SimpleDateFormat simpleDate  = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat simpleTime  = new SimpleDateFormat("HH:mm");
        Calendar cal = Calendar.getInstance();

        cal.setTime(mDate);
        cal.add(Calendar.HOUR, +1);
        getDay = simpleDate.format(cal.getTime());
        getTime = simpleTime.format(mDate);
        getTime2 = simpleTime.format(cal.getTime());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) getLayoutInflater().inflate(R.layout.fragment1, container, false);

        ct = container.getContext();
        materialCalendarView = rootView.findViewById(R.id.calendarView);
        setHasOptionsMenu(true);

        materialCalendarView.state().edit()                                                                         // 달력 설정
                .setFirstDayOfWeek(java.util.Calendar.SUNDAY)
                .setMinimumDate(CalendarDay.from(2010, 0, 1))                                      // 달력의 시작
                .setMaximumDate(CalendarDay.from(2030, 11, 31))                                    // 달력의 끝
                .setCalendarDisplayMode(CalendarMode.MONTHS)                                                          // 달력을 월 단위로 표시
                .commit();

        materialCalendarView.addDecorators(new SundayDecorator(), new SaturdayDecorator(), new OneDayDecorator());    // 데코 추가 (일요일, 월요일, 오늘)

        String[] result = {};   // 이 배열에 날짜 추가 하면 특정 날짜처럼 데코가 생김

        new ApiSimulator(result).executeOnExecutor(Executors.newSingleThreadExecutor());

        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {                    // 달력 클릭 시 일정 입력
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                int Year = date.getYear();                      // 년
                int Month = date.getMonth() + 1;                // 월
                int Day = date.getDay();                        // 일

                Date date2 = new Date();                        // date
                final String time1 = new SimpleDateFormat("HH").format(date2);      //시간 값 저장

                Log.i("Year test", Year + "");
                Log.i("Month test", Month + "");
                Log.i("Day test", Day + "");
                String m_day, d_day;
                if(Month < 10) {
                    m_day = "-0" + Month;      // 년+월+일
                }
                else {
                    m_day = "-" + String.valueOf(Month);      // 년+월+일
                }
                if(Day < 10) {
                   d_day = "-0" + Day;      // 년+월+일
                }
                else {
                    d_day = "-" + String.valueOf(Day);      // 년+월+일
                }
                shot_Day = Year + m_day + d_day;
                Log.i("shot_Day test", shot_Day + "");

                materialCalendarView.clearSelection();          // 선택 초기화
                currentDT();
                sharedViewModel.setLiveData(shot_Day +"⌒"+ getTime + "⌒"+ getTime2);
                FragmentDialog dialog = new FragmentDialog();
                dialog.show(getActivity().getSupportFragmentManager(), "tag");
                //DialogFragment fragment = new DialogFragment();
                Bundle bundle = new Bundle(1);
                bundle.putString("key", shot_Day);
                dialog.setArguments(bundle);
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
    public void onDetach() {
        mCallback = null;
        super.onDetach();
    }

    public void onRefresh() {
        Toast.makeText(getActivity(), "Fragment : Refresh called.", Toast.LENGTH_SHORT).show();
    }

    private void sendData(String date, String time, String time2) {
        mCallback.sendDateTime(date, time, time2);
        Log.i("go", date);
        Log.i("go", time);
        Log.i("go", time2);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu1, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu1,inflater);
        inflater.inflate(R.menu.fragment1_menu, menu1);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();                                                                                         // 아이템 아이디
        if (id == R.id.menu11) {
            ((MainActivity)getActivity()).onFragmentSelected(3, null);
        }
        else if (id == R.id.menu12) {
            currentDT();
            sendData(getDay, getTime, getTime2);
            sharedViewModel.setLiveData(getDay +"⌒"+ getTime + "⌒"+ getTime2);
            ((MainActivity)getActivity()).onFragmentSelected(2, null);
        }
        return super.onOptionsItemSelected(item);
    }

    private class ApiSimulator extends AsyncTask<Void, Void, List<CalendarDay>> {
        String[] Time_Result;       //특정 날짜 넣는 배열
        ApiSimulator(String[] Time_Result) {
            this.Time_Result = Time_Result;
        }

        @Override
        protected List<CalendarDay> doInBackground(@NonNull Void... voids) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            java.util.Calendar calendar = java.util.Calendar.getInstance();
            ArrayList<CalendarDay> dates = new ArrayList<>();

            /*특정날짜 달력에 점표시해주는곳*/ /*월은 0이 1월 년,일은 그대로*/
            //string 문자열인 Time_Result 을 받아와서 ,를 기준으로짜르고 string을 int 로 변환
            for (int i = 0; i < Time_Result.length; i++) {
                CalendarDay day = CalendarDay.from(calendar);
                String[] time = Time_Result[i].split("-");
                int year = Integer.parseInt(time[0]);
                int month = Integer.parseInt(time[1]);
                int dayy = Integer.parseInt(time[2]);

                dates.add(day);
                calendar.set(year, month - 1, dayy);
            }
            return dates;
        }

        @Override
        protected void onPostExecute(@NonNull List<CalendarDay> calendarDays) {     //특정 날짜에 빨간점 찍기
            super.onPostExecute(calendarDays);
            materialCalendarView.addDecorator(new EventDecorator(Color.RED, calendarDays, (Activity) ct));
        }
    }
}