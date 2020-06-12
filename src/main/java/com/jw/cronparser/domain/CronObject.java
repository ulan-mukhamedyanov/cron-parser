package com.jw.cronparser.domain;

import java.time.LocalDateTime;
import java.util.Set;

import com.jw.cronparser.searcher.*;

import lombok.Builder;

@Builder
public class CronObject {

    private final Set<CronSeconds> seconds;
    private final Set<CronMinutes> minutes;
    private final Set<CronHours> hours;
    private final Set<CronDaysOfMonth> daysOfMonth;
    private final Set<CronMonths> months;
    private final Set<CronDaysOfWeek> daysOfWeek;
    private final Set<CronYears> years;

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
