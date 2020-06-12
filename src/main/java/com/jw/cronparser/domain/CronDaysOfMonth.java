package com.jw.cronparser.domain;

import java.util.Arrays;
import java.util.Set;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CronDaysOfMonth implements CronToken {

    public static final Integer LAST_DAY = 0;
    public static final CronDaysOfMonth ANY = new CronDaysOfMonth(true);
    public static final CronDaysOfMonth EVERY = new CronDaysOfMonth(1, 1, null, false);

    private static final String CRON_EVERY = "*";
    private static final String CRON_ANY = "?";
    private static final String CRON_LAST_WEEKDAY = "LW";
    private static final Pattern REGEX_ALL = Pattern.compile("^(\\d+)-(\\d+)/(\\d+)$");
    private static final Pattern REGEX_RANGE = Pattern.compile("^(\\d+)-(\\d+)$");
    private static final Pattern REGEX_EVERY = Pattern.compile("^(\\d+)/(\\d+)$");
    private static final Pattern REGEX_LAST = Pattern.compile("^L(-\\d+)?$");
    private static final Pattern REGEX_WEEKDAY = Pattern.compile("^(\\d+)W$");
    private static final Pattern REGEX_SIMPLE = Pattern.compile("^(\\d+)$");

    private final boolean any;
    private Integer start;
    private Integer every;
    private Integer end;
    private boolean closestWeekday;

    public static Set<CronDaysOfMonth> parse(String str) {
        if (str.equals(CRON_ANY)) {
            return Set.of(ANY);
        }
        if (str.equals(CRON_EVERY)) {
            return Set.of(EVERY);
        }
        String[] expressions = str.split(",");
        if (expressions.length > 0) {
            return Arrays.stream(expressions).map(CronDaysOfMonth::parseElementNumeralCases).collect(Collectors.toSet());
        }
        else {
            throw new IllegalArgumentException("Wrong Cron format for days of month: " + str);
        }
    }

    private static CronDaysOfMonth parseElementNumeralCases(String expression) {
        Matcher matcherAll = REGEX_ALL.matcher(expression);
        Matcher matcherEvery = REGEX_EVERY.matcher(expression);
        Matcher matcherRange = REGEX_RANGE.matcher(expression);
        Matcher matcherSimple = REGEX_SIMPLE.matcher(expression);
        if (matcherAll.matches()) {
            return new CronDaysOfMonth(Integer.parseInt(matcherAll.group(1)), Integer.parseInt(matcherAll.group(3)), Integer.parseInt(matcherAll.group(2)));
        } else if (matcherEvery.matches()) {
            return new CronDaysOfMonth(Integer.parseInt(matcherEvery.group(1)), Integer.parseInt(matcherEvery.group(2)), null);
        } else if (matcherRange.matches()) {
            return new CronDaysOfMonth(Integer.parseInt(matcherRange.group(1)), null, Integer.parseInt(matcherRange.group(2)));
        } else if (matcherSimple.matches()) {
            return new CronDaysOfMonth(Integer.parseInt(matcherSimple.group(1)), null, null);
        } else {
            return parseElementLiteralCases(expression);
        }
    }

    private static CronDaysOfMonth parseElementLiteralCases(String expression) {
        Matcher matcherLast = REGEX_LAST.matcher(expression);
        Matcher matcherWeekday = REGEX_WEEKDAY.matcher(expression);
        if (expression.equals(CRON_LAST_WEEKDAY)) {
            return new CronDaysOfMonth(0, null, null, true);
        } else if (matcherLast.matches()) {
            return new CronDaysOfMonth(matcherLast.group(1) == null ? LAST_DAY : Integer.parseInt(matcherLast.group(1)), null, null);
        } else if (matcherWeekday.matches()) {
            return new CronDaysOfMonth(Integer.parseInt(matcherWeekday.group(1)), null, null, true);
        } else {
            throw new IllegalArgumentException("Wrong Cron format for days of month: " + expression);
        }
    }

    private CronDaysOfMonth(boolean any) {
        this.any = any;
    }

    public CronDaysOfMonth(Integer start, Integer every, Integer end) {
        assert start != null;
        assert start > -31 && start <= 31;
        this.start = start;
        if (end != null) {
            assert end > -31 && end <= 31 && end >= start;
            this.end = end;
        }
        if (every != null) {
            assert every > 0;
            this.every = every;
        }
        this.closestWeekday = false;
        this.any = false;
    }

    public CronDaysOfMonth(Integer start, Integer every, Integer end, boolean closestWeekday) {
        this(start, every, end);
        this.closestWeekday = closestWeekday;
    }

    public boolean isAny() {
        return any;
    }

    public Integer getStart() {
        return start;
    }

    public boolean hasEvery() {
        return every != null;
    }

    public Integer getEvery() {
        return every;
    }

    public boolean hasEnd() {
        return end != null;
    }

    public Integer getEnd() {
        return end;
    }

    public boolean isClosestWeekday() {
        return closestWeekday;
    }

    @Override
    public int hashCode() {
        if (any) {
            return 0;
        }
        int result = start ^ (start >>> 32);
        result = 31 * result + (every == null ? 0 : every.hashCode());
        result = 31 * result + (end == null ? 0 : end.hashCode());
        result = 31 * result + (end == null ? 0 : end.hashCode());
        result = 31 * result + Objects.hashCode(any);
        result = 31 * result + Objects.hashCode(closestWeekday);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;
        CronDaysOfMonth cronDaysOfMonth = (CronDaysOfMonth) o;
        if (any && cronDaysOfMonth.any) return true;
        return any == cronDaysOfMonth.any
                && start.equals(cronDaysOfMonth.start)
                && every.equals(cronDaysOfMonth.every)
                && end.equals(cronDaysOfMonth.end)
                && closestWeekday == cronDaysOfMonth.closestWeekday;
    }
}
