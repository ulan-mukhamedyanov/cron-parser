package com.jw.cronparser.domain;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.jw.cronparser.CronUtils;

public class CronDaysOfWeek implements CronToken {

    public static final Integer LAST_DAY_OF_WEEK = 0;
    public static final CronDaysOfWeek ANY = new CronDaysOfWeek(true);
    public static final CronDaysOfWeek EVERY = new CronDaysOfWeek(1, null, 1, null);

    public static final Set<Integer> WEEKDAYS = Set.of(2, 3, 4, 5, 6);

    private static final String CRON_EVERY = "*";
    private static final String CRON_ANY = "?";
    private static final Pattern REGEX_ALL =
            Pattern.compile("^(\\d+|SUN|MON|TUE|WED|THU|FRI|SAT)(?:-(\\d+|SUN|MON|TUE|WED|THU|FRI|SAT))?(?:/(\\d+))?(?:#(\\d+))?$");
    private static final Pattern REGEX_LAST = Pattern.compile("^(\\d+|SUN|MON|TUE|WED|THU|FRI|SAT)L$");

    private static final Map<String, Integer> WEEKDAY_MAPPING = Map.of(
            "SUN", 1,
            "MON", 2,
            "TUE", 3,
            "WED", 4,
            "THU", 5,
            "FRI", 6,
            "SAT", 7
    );

    private final boolean any;
    private Integer start;
    private Integer ordinal;
    private Integer every;
    private Integer end;
    private Set<Integer> matchingDaysOfWeek;

    public static Set<CronDaysOfWeek> parse(String str) {
        if (str.equals(CRON_ANY)) {
            return Set.of(ANY);
        }
        if (str.equals(CRON_EVERY)) {
            return Set.of(EVERY);
        }
        String[] expressions = str.split(",");
        if (expressions.length > 0) {
            return Arrays.stream(expressions).map(CronDaysOfWeek::parseElement).collect(Collectors.toSet());
        } else {
            throw new IllegalArgumentException("Wrong Cron format for day of week: " + str);
        }
    }

    private static CronDaysOfWeek parseElement(String expression) {
        Matcher matcherAll = REGEX_ALL.matcher(expression);
        Matcher matcherLast = REGEX_LAST.matcher(expression);
        if (matcherAll.matches()) {
            return new CronDaysOfWeek(
                    resolveDayOfWeek(matcherAll.group(1)),
                    resolveDayOfWeek(matcherAll.group(4)),
                    resolveDayOfWeek(matcherAll.group(3)),
                    resolveDayOfWeek(matcherAll.group(2)));
        } else if (matcherLast.matches()) {
            return new CronDaysOfWeek(resolveDayOfWeek(matcherLast.group(1)), LAST_DAY_OF_WEEK, null, null);
        } else {
            throw new IllegalArgumentException("Wrong Cron format for day of week: " + expression);
        }
    }

    private CronDaysOfWeek(boolean any) {
        this.any = any;
    }

    public CronDaysOfWeek(Integer start, Integer ordinal, Integer every, Integer end) {
        assert start != null && start >= 1 && start <= 7;
        this.start = start;
        if (ordinal != null) {
            assert ordinal >= 0 && ordinal <= 5;
            this.ordinal = ordinal;
        }
        if (end != null) {
            assert end >= 1 && end <= 7 && end >= start;
            this.end = end;
        }
        if (every != null) {
            assert every > 0;
            this.every = every;
        }
        this.any = false;
        calculateMatchingDaysOfWeek();
    }

    private static Integer resolveDayOfWeek(String str) {
        if (str == null || str.trim().equals("")) {
            return null;
        } else if (WEEKDAY_MAPPING.containsKey(str)) {
            return WEEKDAY_MAPPING.get(str);
        } else {
            return Integer.parseInt(str);
        }
    }

    private void calculateMatchingDaysOfWeek() {
        Set<Integer> result = new LinkedHashSet<>();
        if (!any) {
            result.add(start);
            if (end != null && !start.equals(end)) {
                for (int i = start; i != end + 1; i++) {
                    i = i > 7 ? 1 : i;
                    result.add(i);
                }
            }
            if (every != null) {
                for (int i = start; i <= 7; i += every) {
                    result.add(i);
                }
            }
        }
        this.matchingDaysOfWeek = result;
    }

    public boolean matchesDayOfWeek(DayOfWeek dayOfWeek) {
        return matchingDaysOfWeek.contains(CronUtils.dayOfWeekToIndex(dayOfWeek));
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

    public boolean hasOrdinal() {
        return ordinal != null;
    }

    public Integer getOrdinal() {
        return ordinal;
    }

    public Set<Integer> getMatchingDaysOfWeek() {
        return matchingDaysOfWeek;
    }

    @Override
    public int hashCode() {
        if (any) {
            return 0;
        }
        int result = start ^ (start >>> 32);
        result = 31 * result + (ordinal == null ? 0 : ordinal.hashCode());
        result = 31 * result + (every == null ? 0 : every.hashCode());
        result = 31 * result + (end == null ? 0 : end.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;
        CronDaysOfWeek cronDaysOfWeek = (CronDaysOfWeek) o;
        if (any && cronDaysOfWeek.any) {
            return true;
        }
        return any == cronDaysOfWeek.any
                && start.equals(cronDaysOfWeek.start)
                && ordinal.equals(cronDaysOfWeek.ordinal)
                && every.equals(cronDaysOfWeek.every)
                && end.equals(cronDaysOfWeek.end);
    }
}
