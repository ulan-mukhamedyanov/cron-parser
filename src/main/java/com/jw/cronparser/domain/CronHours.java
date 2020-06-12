package com.jw.cronparser.domain;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CronHours implements CronToken {

    public static final CronHours EVERY = new CronHours(0, 1, null);

    private static final String CRON_EVERY = "*";
    private static final Pattern REGEX_ALL = Pattern.compile("^(\\d+)-(\\d+)/(\\d+)$");
    private static final Pattern REGEX_RANGE = Pattern.compile("^(\\d+)-(\\d+)$");
    private static final Pattern REGEX_EVERY = Pattern.compile("^(\\d+)/(\\d+)$");
    private static final Pattern REGEX_SIMPLE = Pattern.compile("^(\\d+)$");

    private Integer start;
    private Integer every;
    private Integer end;

    public static Set<CronHours> parse(String str) {
        if (str.equals(CRON_EVERY)) {
            return Set.of(EVERY);
        }
        String[] expressions = str.split(",");
        if (expressions.length > 0) {
            return Arrays.stream(expressions).map(CronHours::parseElement).collect(Collectors.toSet());
        }
        else {
            throw new IllegalArgumentException("Wrong Cron format for hours: " + str);
        }
    }

    private static CronHours parseElement(String expression) {
        Matcher matcherAll = REGEX_ALL.matcher(expression);
        Matcher matcherEvery = REGEX_EVERY.matcher(expression);
        Matcher matcherRange = REGEX_RANGE.matcher(expression);
        Matcher matcherSimple = REGEX_SIMPLE.matcher(expression);
        if (matcherAll.matches()) {
            return new CronHours(Integer.parseInt(matcherAll.group(1)), Integer.parseInt(matcherAll.group(3)), Integer.parseInt(matcherAll.group(2)));
        } else if (matcherEvery.matches()) {
            return new CronHours(Integer.parseInt(matcherEvery.group(1)), Integer.parseInt(matcherEvery.group(2)), null);
        } else if (matcherRange.matches()) {
            return new CronHours(Integer.parseInt(matcherRange.group(1)), null, Integer.parseInt(matcherRange.group(2)));
        } else if (matcherSimple.matches()) {
            return new CronHours(Integer.parseInt(matcherSimple.group(1)), null, null);
        } else {
            throw new IllegalArgumentException("Wrong Cron format for hours: " + expression);
        }
    }

    public CronHours(Integer start, Integer every, Integer end) {
        assert start != null;
        assert start >= 0 && start < 24;
        this.start = start;
        if (end != null) {
            assert end >= 0 && end < 24 && end >= start;
            this.end = end;
        }
        if (every != null) {
            assert every > 0;
            this.every = every;
        }
    }

    public Integer getStart() {
        return start;
    }

    public Integer getEvery() {
        return every;
    }

    public boolean hasEvery() {
        return every != null;
    }

    public Integer getEnd() {
        return end;
    }

    public boolean hasEnd() {
        return end != null;
    }

    @Override
    public int hashCode() {
        int result = start ^ (start >>> 32);
        result = 31 * result + (every == null ? 0 : every.hashCode());
        result = 31 * result + (end == null ? 0 : end.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;
        CronHours cronHours = (CronHours) o;
        return start.equals(cronHours.start)
                && every.equals(cronHours.every)
                && end.equals(cronHours.end);
    }
}
