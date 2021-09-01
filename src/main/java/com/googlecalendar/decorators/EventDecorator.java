package com.googlecalendar.decorators;

import android.app.Activity;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.HashSet;
import java.util.List;

/**
 * Decorate several days with a dot
 */
public class EventDecorator implements DayViewDecorator {

    private int color;                      // 색
    private HashSet<CalendarDay> dates;     // 이벤트 일정

    public EventDecorator(int color, List<CalendarDay> dates, Activity context) {
        this.color = color;                 // 색
        this.dates = new HashSet<>(dates);  // 이벤트 일정
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new DotSpan(6, color)); // 빨간색
    }
}
