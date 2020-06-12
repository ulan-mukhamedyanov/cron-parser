package com.jw.cronparser.domain;

import static com.jw.cronparser.CronUtils.MAX_MINUTE;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CronMinutes implements CronToken {

    public static final CronMinutes EVERY = new CronMinutes(0, 1, null);

    private static final String CRON_EVERY = "*";
    private static final Pattern REGEX_ALL = Pattern.compile("^(\\d+)-(\\d+)/(\\d+)$");
    private static final Pattern REGEX_RANGE = Pattern.compile("^(\\d+)-(\\d+)$");
    private static final Pattern REGEX_EVERY = Pattern.compile("^(\\d+)/(\\d+)$");
    private static final Pattern REGEX_SIMPLE = Pattern.compile("^(\\d+)$");
    private static final int GROUP_1 = 1;
    private static final int GROUP_2 = 2;
    private static final int GROUP_3 = 3;

    private final Integer start;
    private final Integer every;
    private final Integer end;

    CronMinutes(Integer start, Integer every, Integer end) {
        assert start != null;
        assert start >= 0 && start <= MAX_MINUTE;
        assert end == null || end >= 0 && end <= MAX_MINUTE && end >= start;
        assert every == null || every > 0;
        this.start = start;
        this.end = end;
        this.every = every;
    }

    public static Set<CronMinutes> parse(String str) {
        if (str.equals(CRON_EVERY)) {
            return Set.of(EVERY);
        }
        String[] expressions = str.split(",");
        if (expressions.length > 0) {
            return Arrays.stream(expressions).map(CronMinutes::parseElement).collect(Collectors.toSet());
        } else {
            throw new IllegalArgumentException("Wrong Cron format for minutes: " + str);
        }
    }

    private static CronMinutes parseElement(String expression) {
        Matcher matcherAll = REGEX_ALL.matcher(expression);
        Matcher matcherEvery = REGEX_EVERY.matcher(expression);
        Matcher matcherRange = REGEX_RANGE.matcher(expression);
        Matcher matcherSimple = REGEX_SIMPLE.matcher(expression);
        if (matcherAll.matches()) {
            return new CronMinutes(Integer.parseInt(matcherAll.group(GROUP_1)),
                    Integer.parseInt(matcherAll.group(GROUP_3)), Integer.parseInt(matcherAll.group(GROUP_2)));
        } else if (matcherEvery.matches()) {
            return new CronMinutes(Integer.parseInt(matcherEvery.group(GROUP_1)), Integer.parseInt(matcherEvery.group(GROUP_2)), null);
        } else if (matcherRange.matches()) {
            return new CronMinutes(Integer.parseInt(matcherRange.group(GROUP_1)), null, Integer.parseInt(matcherRange.group(GROUP_2)));
        } else if (matcherSimple.matches()) {
            return new CronMinutes(Integer.parseInt(matcherSimple.group(GROUP_1)), null, null);
        } else {
            throw new IllegalArgumentException("Wrong Cron format for minutes: " + expression);
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
        final int constant = 32;
        final int prime = 31;
        int result = start ^ (start >>> constant);
        result = prime * result + (every == null ? 0 : every.hashCode());
        result = prime * result + (end == null ? 0 : end.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        CronMinutes cronMinutes = (CronMinutes) o;
        return start.equals(cronMinutes.start)
                && every.equals(cronMinutes.every)
                && end.equals(cronMinutes.end);
    }

}
