package com.jw.cronparser;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.util.OptionalInt;
import java.util.Set;

public class CronUtils {

    public static final int WEEK_LENGTH = 7;
    public static final int MIN_YEAR = 1900;
    public static final int MAX_YEAR = 2100;
    public static final int MAX_MONTH = 12;
    public static final int MAX_DAY_OF_MONTH = 31;
    public static final int MAX_DAY_OF_WEEK = 7;
    public static final int MAX_WEEKS = 5;
    public static final int MAX_HOUR = 23;
    public static final int MAX_MINUTE = 59;
    public static final int MAX_SECOND = 59;

    private static final Set<Integer> WEEKDAYS = Set.of(2, 3, 4, 5, 6);

    private CronUtils() {

    }

    public static int dayOfWeekDiff(DayOfWeek start, DayOfWeek end) {
        if (end.getValue() >= start.getValue()) {
            return end.getValue() - start.getValue();
        } else {
            return WEEK_LENGTH - start.getValue() + end.getValue();
        }
    }

    public static int dayOfWeekDiff(int start, int end) {
        return dayOfWeekDiff(indexToDayOfWeek(start), indexToDayOfWeek(end));
    }

    public static int closestWeekDay(LocalDateTime current) {
        int day = current.getDayOfMonth();
        if (WEEKDAYS.contains(current.getDayOfWeek().getValue() + 1)) {
            return day;
        } else {
            if (current.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
                return day - 1 >= 1 ? day - 1 : day + 2;
            } else {
                int lastDay = current.getMonth().length(Year.isLeap(current.getYear()));
                return day + 1 <= lastDay ? day + 1 : day - 2;
            }
        }
    }

    public static OptionalInt nthDayOfWeek(int n, DayOfWeek dayOfWeek, Month month, int year) {
        assert n >= 0 && n <= MAX_WEEKS;
        if (n > 0) {
            LocalDateTime theFirst = LocalDateTime.of(year, month, 1, 0, 0);
            int diff = dayOfWeekDiff(theFirst.getDayOfWeek(), dayOfWeek);
            int result = theFirst.plusDays(diff + WEEK_LENGTH * (n - 1)).getDayOfMonth();
            return result <= month.length(Year.isLeap(year)) ? OptionalInt.of(result) : OptionalInt.empty();
        } else {
            LocalDateTime theLast = LocalDateTime.of(year, month, month.length(Year.isLeap(year)), 0, 0);
            int diff = dayOfWeekDiff(dayOfWeek, theLast.getDayOfWeek());
            return OptionalInt.of(theLast.minusDays(diff).getDayOfMonth());
        }
    }

    public static int lastDayOfMonth(LocalDateTime dateTime, int offset) {
        return dateTime.getMonth().length(Year.isLeap(dateTime.getYear())) - Math.abs(offset);
    }

    public static DayOfWeek indexToDayOfWeek(int i) {
        int dowIndex = i - 1 == 0 ? WEEK_LENGTH : i - 1;
        return DayOfWeek.of(dowIndex);
    }

    public static int dayOfWeekToIndex(DayOfWeek dayOfWeek) {
        return dayOfWeek.getValue() + 1 > WEEK_LENGTH ? 1 : dayOfWeek.getValue() + 1;
    }

    public static int getDayByIndex(LocalDateTime dateTime, int index, boolean weekday) {
        int day = index <= 0 ? lastDayOfMonth(dateTime, index) : index;
        return weekday ? closestWeekDay(dateTime.withDayOfMonth(day)) : day;
    }
}
