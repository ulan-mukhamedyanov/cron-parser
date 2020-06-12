package com.jw.cronparser.domain;

import static com.jw.cronparser.CronUtils.MAX_MONTH;

import java.time.Month;
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
    private static final int GROUP_1 = 1;
    private static final int GROUP_2 = 2;
    private static final int GROUP_3 = 3;

    static {
        Map<String, Integer> monthMapping = new HashMap<>();
        monthMapping.put("JAN", Month.JANUARY.getValue());
        monthMapping.put("FEB", Month.FEBRUARY.getValue());
        monthMapping.put("MAR", Month.MARCH.getValue());
        monthMapping.put("APR", Month.APRIL.getValue());
        monthMapping.put("MAY", Month.MAY.getValue());
        monthMapping.put("JUN", Month.JUNE.getValue());
        monthMapping.put("JUL", Month.JULY.getValue());
        monthMapping.put("AUG", Month.AUGUST.getValue());
        monthMapping.put("SEP", Month.SEPTEMBER.getValue());
        monthMapping.put("OCT", Month.OCTOBER.getValue());
        monthMapping.put("NOV", Month.NOVEMBER.getValue());
        monthMapping.put("DEC", Month.DECEMBER.getValue());
        MONTH_MAPPING = monthMapping;
    }

    private final Integer start;
    private final Integer every;
    private final Integer end;

    CronMonths(Integer start, Integer every, Integer end) {
        assert start != null;
        assert start >= 1 && start <= MAX_MONTH;
        assert end == null || end >= 1 && end <= MAX_MONTH && end >= start;
        assert every == null || every > 0;
        this.start = start;
        this.end = end;
        this.every = every;
    }

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
            return new CronMonths(resolveMonth(matcherAll.group(GROUP_1)), resolveMonth(matcherAll.group(GROUP_3)), resolveMonth(matcherAll.group(GROUP_2)));
        } else {
            throw new IllegalArgumentException("Wrong Cron format for months: " + expression);
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
        CronMonths cronHours = (CronMonths) o;
        return start.equals(cronHours.start)
                && every.equals(cronHours.every)
                && end.equals(cronHours.end);
    }
}
