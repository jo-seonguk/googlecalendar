package com.googlecalendar.decorators;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Date;

/**
 * Decorate a day by making the text big and bold
 */
public class OneDayDecorator implements DayViewDecorator {
    private CalendarDay date;           // 캘린더

    public OneDayDecorator() {
        date = CalendarDay.today();
    }  // 캘린더에 오늘 표시 데코

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return date != null && day.equals(date);
    }   // 확인

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new StyleSpan(Typeface.BOLD));     // 글씨 bold
        view.addSpan(new RelativeSizeSpan(1.4f));   // 글씨 굵기
        view.addSpan(new ForegroundColorSpan(Color.GREEN));     // 글씨 색
    }

    public void setDate(Date date) {
        this.date = CalendarDay.from(date);
    }   // 셋
}
