package com.jw.cronparser;

import com.jw.cronparser.domain.*;

public class CronParser {

    private static final int INDEX_SECONDS = 0;
    private static final int INDEX_MINUTES = 1;
    private static final int INDEX_HOURS = 2;
    private static final int INDEX_DAYS_OF_MONTH = 3;
    private static final int INDEX_MONTHS = 4;
    private static final int INDEX_DAYS_OF_WEEK = 5;
    private static final int INDEX_YEARS = 6;

    public CronObject parse(String expression) {
        String[] input = expression.replaceAll("\\s+", " ").trim().split("\\s");
        assert input.length == 7;
        return new CronObject(
                CronSeconds.parse(input[INDEX_SECONDS]),
                CronMinutes.parse(input[INDEX_MINUTES]),
                CronHours.parse(input[INDEX_HOURS]),
                CronDaysOfMonth.parse(input[INDEX_DAYS_OF_MONTH]),
                CronMonths.parse(input[INDEX_MONTHS]),
                CronDaysOfWeek.parse(input[INDEX_DAYS_OF_WEEK]),
                CronYears.parse(input[INDEX_YEARS])
        );
    }
}
