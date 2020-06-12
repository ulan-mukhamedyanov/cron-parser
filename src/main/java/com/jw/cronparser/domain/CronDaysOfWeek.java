package com.jw.cronparser.domain;

import static com.jw.cronparser.CronUtils.*;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CronDaysOfWeek implements CronToken {

    public static final Integer LAST_DAY_OF_WEEK = 0;
    public static final CronDaysOfWeek ANY = new CronDaysOfWeek(true);
    public static final CronDaysOfWeek EVERY = new CronDaysOfWeek(1, null, 1, null);

    private static final String CRON_EVERY = "*";
    private static final String CRON_ANY = "?";
    private static final Pattern REGEX_ALL =
            Pattern.compile("^(\\d+|SUN|MON|TUE|WED|THU|FRI|SAT)(?:-(\\d+|SUN|MON|TUE|WED|THU|FRI|SAT))?(?:/(\\d+))?(?:#(\\d+))?$");
    private static final Pattern REGEX_LAST = Pattern.compile("^(\\d+|SUN|MON|TUE|WED|THU|FRI|SAT)L$");
    private static final int GROUP_1 = 1;
    private static final int GROUP_2 = 2;
    private static final int GROUP_3 = 3;
    private static final int GROUP_4 = 4;

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

    private CronDaysOfWeek(boolean any) {
        this.any = any;
    }

    CronDaysOfWeek(Integer start, Integer ordinal, Integer every, Integer end) {
        assert start != null && start >= 1 && start <= MAX_DAY_OF_WEEK;
        assert ordinal == null || ordinal >= 0 && ordinal <= MAX_WEEKS;
        assert end == null || end >= 1 && end <= MAX_DAY_OF_WEEK && end >= start;
        assert every == null || every > 0;
        this.start = start;
        this.ordinal = ordinal;
        this.end = end;
        this.every = every;
        this.any = false;
        calculateMatchingDaysOfWeek();
    }

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
                    resolveDayOfWeek(matcherAll.group(GROUP_1)),
                    resolveDayOfWeek(matcherAll.group(GROUP_4)),
                    resolveDayOfWeek(matcherAll.group(GROUP_3)),
                    resolveDayOfWeek(matcherAll.group(GROUP_2)));
        } else if (matcherLast.matches()) {
            return new CronDaysOfWeek(resolveDayOfWeek(matcherLast.group(GROUP_1)), LAST_DAY_OF_WEEK, null, null);
        } else {
            throw new IllegalArgumentException("Wrong Cron format for day of week: " + expression);
        }
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
            if (end != null && every == null && !start.equals(end)) {
                for (int i = 0, diff = dayOfWeekDiff(start, end); i <= diff; i++) {
                    int current = start + i > MAX_DAY_OF_WEEK ? 1 : start + i;
                    result.add(current);
                }
            }
            if (every != null) {
                for (int i = start; i <= MAX_DAY_OF_WEEK; i += every) {
                    result.add(i);
                }
            }
        }
        this.matchingDaysOfWeek = result;
    }

    public boolean matchesDayOfWeek(DayOfWeek dayOfWeek) {
        return matchingDaysOfWeek.contains(dayOfWeekToIndex(dayOfWeek));
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
        final int constant = 32;
        final int prime = 31;
        int result = start ^ (start >>> constant);
        result = prime * result + (ordinal == null ? 0 : ordinal.hashCode());
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
