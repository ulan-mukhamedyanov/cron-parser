package com.jw.cronparser.domain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CronMonths implements CronToken {

    public static final CronMonths EVERY = new CronMonths(1, 1, null);

    private static final String CRON_EVERY = "*";
    private static final Pattern REGEX_ALL =
            Pattern.compile("^(\\d+|JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)(?:-(\\d+|JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))?(?:/(\\d+))?$");

    private static final Map<String, Integer> MONTH_MAPPING;

    static {
        Map<String, Integer> monthMapping = new HashMap<>();
        monthMapping.put("JAN", 1);
        monthMapping.put("FEB", 2);
        monthMapping.put("MAR", 3);
        monthMapping.put("APR", 4);
        monthMapping.put("MAY", 5);
        monthMapping.put("JUN", 6);
        monthMapping.put("JUL", 7);
        monthMapping.put("AUG", 8);
        monthMapping.put("SEP", 9);
        monthMapping.put("OCT", 10);
        monthMapping.put("NOV", 11);
        monthMapping.put("DEC", 12);
        MONTH_MAPPING = monthMapping;
    }

    private Integer start;
    private Integer every;
    private Integer end;

    public static Set<CronMonths> parse(String str) {
        if (str.equals(CRON_EVERY)) {
            return Set.of(EVERY);
        }
        String[] expressions = str.split(",");
        if (expressions.length > 0) {
            return Arrays.stream(expressions).map(CronMonths::parseElement).collect(Collectors.toSet());
        } else {
            throw new IllegalArgumentException("Wrong Cron format for months: " + str);
        }
    }

    private static CronMonths parseElement(String expression) {
        Matcher matcherAll = REGEX_ALL.matcher(expression);
        if (matcherAll.matches()) {
            return new CronMonths(resolveMonth(matcherAll.group(1)), resolveMonth(matcherAll.group(3)), resolveMonth(matcherAll.group(2)));
        } else {
            throw new IllegalArgumentException("Wrong Cron format for months: " + expression);
        }
    }

    public CronMonths(Integer start, Integer every, Integer end) {
        assert start != null;
        assert start >= 1 && start <= 12;
        this.start = start;
        if (end != null) {
            assert end >= 1 && end <= 12 && end >= start;
            this.end = end;
        }
        if (every != null) {
            assert every > 0;
            this.every = every;
        }
    }

    private static Integer resolveMonth(String str) {
        if (str == null || str.trim().equals("")) {
            return null;
        } else if (MONTH_MAPPING.containsKey(str)) {
            return MONTH_MAPPING.get(str);
        } else {
            return Integer.parseInt(str);
        }
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
        CronMonths cronHours = (CronMonths) o;
        return start.equals(cronHours.start)
                && every.equals(cronHours.every)
                && end.equals(cronHours.end);
    }
}
