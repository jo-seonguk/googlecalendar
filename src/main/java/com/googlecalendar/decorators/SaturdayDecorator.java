package com.googlecalendar.decorators;

import android.graphics.Color;
import android.text.style.ForegroundColorSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Calendar;

/**
 * Highlight Saturdays and Sundays with a background
 */
public class SaturdayDecorator implements DayViewDecorator {

    private final Calendar calendar = Calendar.getInstance();       // 캘린더 정보

    public SaturdayDecorator() {
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        day.copyTo(calendar);                           // 캘린더 카피
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);      // 캘린더 주단위로 받음
        return weekDay == Calendar.SATURDAY;                    // 토요일로 설정
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new ForegroundColorSpan(Color.BLUE));  // 토요일 파란색으로 표시

    }
}
