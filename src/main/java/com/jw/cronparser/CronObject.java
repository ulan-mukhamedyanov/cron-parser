package com.jw.cronparser;

import java.time.LocalDateTime;
import java.util.Set;

import com.jw.cronparser.searcher.*;
import com.jw.cronparser.domain.*;

public class CronObject {

    private final Set<CronSeconds> seconds;
    private final Set<CronMinutes> minutes;
    private final Set<CronHours> hours;
    private final Set<CronDaysOfMonth> daysOfMonth;
    private final Set<CronMonths> months;
    private final Set<CronDaysOfWeek> daysOfWeek;
    private final Set<CronYears> years;

    public CronObject(Set<CronSeconds> seconds, Set<CronMinutes> minutes, Set<CronHours> hours,
            Set<CronDaysOfMonth> daysOfMonth, Set<CronMonths> months, Set<CronDaysOfWeek> daysOfWeek, Set<CronYears> years) {
        this.seconds = seconds;
        this.minutes = minutes;
        this.hours = hours;
        this.daysOfMonth = daysOfMonth;
        this.months = months;
        this.daysOfWeek = daysOfWeek;
        this.years = years;
        if (!(daysOfMonth.contains(CronDaysOfMonth.ANY) || daysOfWeek.contains(CronDaysOfWeek.ANY))) {
            throw new UnsupportedOperationException("Calculating with both day of month and day of week is not supported");
        } else if (daysOfMonth.contains(CronDaysOfMonth.ANY) && daysOfWeek.contains(CronDaysOfWeek.ANY)) {
            throw new IllegalArgumentException("Either day of month or day of week must be specified");
        }
    }

    public Set<CronSeconds> getSeconds() {
        return seconds;
    }

    public Set<CronMinutes> getMinutes() {
        return minutes;
    }

    public Set<CronHours> getHours() {
        return hours;
    }

    public Set<CronDaysOfMonth> getDaysOfMonth() {
        return daysOfMonth;
    }

    public Set<CronMonths> getMonths() {
        return months;
    }

    public Set<CronDaysOfWeek> getDaysOfWeek() {
        return daysOfWeek;
    }

    public Set<CronYears> getYears() {
        return years;
    }

    public LocalDateTime nextFireDateTime(LocalDateTime dateTime) {
        CronSearcher searcher = new ForwardSearcher(this);
        return searcher.findClosest(dateTime);
    }

    public LocalDateTime previousFireDateTime(LocalDateTime dateTime) {
        CronSearcher searcher = new BackwardSearcher(this);
        return searcher.findClosest(dateTime);
    }

}
