package com.googlecalendar.decorators;

import android.graphics.Color;
import android.text.style.ForegroundColorSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Calendar;

/**
 * Created by samsung on 2017-04-12.
 */

public class SundayDecorator implements DayViewDecorator {                                              // 일요일 데코

    private final Calendar calendar = Calendar.getInstance();       // 캘린더 불러오기

    public SundayDecorator() {
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        day.copyTo(calendar);                                       // 캘린더 카피
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);           // 캘린더 주 단위로 받음
        return weekDay == Calendar.SUNDAY;                            // 일요일로 설정
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new ForegroundColorSpan(Color.RED));
    }  // 일요일은 빨간색으로 설정
}
