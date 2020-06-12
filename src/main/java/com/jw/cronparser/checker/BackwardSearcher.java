package com.jw.cronparser.checker;

import static com.jw.cronparser.CronUtils.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.util.Collection;
import java.util.OptionalInt;
import java.util.Set;

import com.jw.cronparser.CronObject;
import com.jw.cronparser.domain.*;

public class BackwardSearcher implements CronSearcher {

    private final CronObject cronObject;

    public BackwardSearcher(CronObject cronObject) {
        this.cronObject = cronObject;
    }

    public LocalDateTime findClosest(LocalDateTime dateTime) {
        var result = backwardChainYear(dateTime);
        if (result != null) {
            return result.withNano(0);
        }
        return null;
    }

    private LocalDateTime backwardChainYear(LocalDateTime dateTime) {
        LocalDateTime current = dateTime;
        LocalDateTime result = null;
        if (backwardCheckCurrentYear(current)) {
            result = backwardChainMonth(current);
        }
        while (result == null) {
            Integer nextValidYear = backwardNextValidYear(current);
            if (nextValidYear == null) {
                return null;
            }
            current = LocalDateTime.of(nextValidYear, 12, 31, 23, 59, 59);
            result = backwardChainMonth(current);
        }
        return result;
    }

    private LocalDateTime backwardChainMonth(LocalDateTime dateTime) {
        LocalDateTime current = dateTime;
        LocalDateTime result = null;
        if (backwardCheckCurrentMonth(current)) {
            result = backwardChainDay(current);
        }
        while (result == null) {
            Integer nextValidMonth = backwardNextValidMonth(current);
            if (nextValidMonth == null) {
                return null;
            }
            Month month = Month.of(nextValidMonth);
            current = LocalDateTime.of(current.getYear(), month, month.length(Year.isLeap(current.getYear())), 23, 59, 59);
            result = backwardChainDay(current);
        }
        return result;
    }

    private LocalDateTime backwardChainDay(LocalDateTime dateTime) {
        LocalDateTime current = dateTime;
        LocalDateTime result = null;
        if (backwardCheckCurrentDayOfMonth(current) && backwardCheckCurrentDayOfWeek(current)) {
            result = backwardChainHour(current);
        }
        while (result == null) {
            Integer nextValidDay = backwardNextValidDay(current);
            if (nextValidDay == null) {
                return null;
            }
            current = LocalDateTime.of(current.getYear(), current.getMonthValue(), nextValidDay, 23, 59, 59);
            result = backwardChainHour(current);
        }
        return result;
    }

    private LocalDateTime backwardChainHour(LocalDateTime dateTime) {
        LocalDateTime current = dateTime;
        LocalDateTime result = null;
        if (backwardCheckCurrentHour(current)) {
            result = backwardChainMinute(current);
        }
        while (result == null) {
            Integer nextValidHour = backwardNextValidHour(current);
            if (nextValidHour == null) {
                return null;
            }
            current = LocalDateTime.of(current.getYear(), current.getMonthValue(), current.getDayOfMonth(), nextValidHour, 59, 59);
            result = backwardChainMinute(current);
        }
        return result;
    }

    private LocalDateTime backwardChainMinute(LocalDateTime dateTime) {
        LocalDateTime current = dateTime;
        LocalDateTime result = null;
        if (backwardCheckCurrentMinute(current)) {
            result = backwardChainSecond(current);
        }
        while (result == null) {
            Integer nextValidMinute = backwardNextValidMinute(current);
            if (nextValidMinute == null) {
                return null;
            }
            current = LocalDateTime.of(current.getYear(), current.getMonthValue(), current.getDayOfMonth(),
                    current.getHour(), nextValidMinute, 59);
            result = backwardChainSecond(current);
        }
        return result;
    }

    private LocalDateTime backwardChainSecond(LocalDateTime dateTime) {
        if (backwardCheckCurrentSecond(dateTime)) {
            return dateTime;
        }
        Integer nextValidSecond = backwardNextValidSecond(dateTime);
        if (nextValidSecond == null) {
            return null;
        } else {
            return LocalDateTime.of(dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(),
                    dateTime.getHour(), dateTime.getMinute(), nextValidSecond);
        }
    }


    private boolean backwardCheckCurrentYear(LocalDateTime dateTime) {
        Set<CronYears> cronYears = cronObject.getYears();
        return cronYears.contains(CronYears.EVERY)
                || cronYears.stream().anyMatch(cronYear -> checkCurrent(cronYear, dateTime.getYear()));
    }

    private boolean backwardCheckCurrentMonth(LocalDateTime dateTime) {
        Set<CronMonths> cronMonths = cronObject.getMonths();
        return cronMonths.contains(CronMonths.EVERY)
                || cronMonths.stream().anyMatch(cronMonth -> checkCurrent(cronMonth, dateTime.getMonthValue()));
    }

    private boolean backwardCheckCurrentDayOfMonth(LocalDateTime dateTime) {
        Set<CronDaysOfMonth> cronDays = cronObject.getDaysOfMonth();
        if (cronDays.contains(CronDaysOfMonth.EVERY) || cronDays.contains(CronDaysOfMonth.ANY)) {
            return true;
        }
        for (CronDaysOfMonth cronDay : cronDays) {
            if (cronDay.getStart() > 0 && !cronDay.isClosestWeekday()) {
                if (checkCurrent(cronDay, dateTime.getDayOfMonth())) {
                    return true;
                }
            } else if (getDayByIndex(dateTime, cronDay.getStart(), cronDay.isClosestWeekday()) == dateTime.getDayOfMonth()) {
                return true;
            }
        }
        return false;
    }

    private boolean backwardCheckCurrentDayOfWeek(LocalDateTime dateTime) {
        Set<CronDaysOfWeek> cronDays = cronObject.getDaysOfWeek();
        if (cronDays.contains(CronDaysOfWeek.EVERY) || cronDays.contains(CronDaysOfWeek.ANY)) {
            return true;
        }
        for (CronDaysOfWeek cronDay : cronDays) {
            if (!cronDay.hasOrdinal()) {
                if (cronDay.matchesDayOfWeek(dateTime.getDayOfWeek())) {
                    return true;
                }
            } else {
                return cronDay.getMatchingDaysOfWeek().stream()
                        .map(i -> nthDayOfWeek(cronDay.getOrdinal(), indexToDayOfWeek(i), dateTime.getMonth(), dateTime.getYear()))
                        .filter(OptionalInt::isPresent).map(OptionalInt::getAsInt).anyMatch(i -> i.equals(dateTime.getDayOfMonth()));
            }
        }
        return false;
    }

    private boolean backwardCheckCurrentHour(LocalDateTime dateTime) {
        Set<CronHours> cronHours = cronObject.getHours();
        return cronHours.contains(CronHours.EVERY)
                || cronHours.stream().anyMatch(cronHour -> checkCurrent(cronHour, dateTime.getHour()));
    }

    private boolean backwardCheckCurrentMinute(LocalDateTime dateTime) {
        Set<CronMinutes> cronMinutes = cronObject.getMinutes();
        return cronMinutes.contains(CronMinutes.EVERY)
                || cronMinutes.stream().anyMatch(cronMinute -> checkCurrent(cronMinute, dateTime.getMinute()));
    }

    private boolean backwardCheckCurrentSecond(LocalDateTime dateTime) {
        Set<CronSeconds> cronSeconds = cronObject.getSeconds();
        return cronSeconds.contains(CronSeconds.EVERY)
                || cronSeconds.stream().anyMatch(cronSecond -> checkCurrent(cronSecond, dateTime.getSecond()));
    }


    private Integer backwardNextValidYear(LocalDateTime dateTime) {
        int min = 1;
        int current = dateTime.getYear();
        Set<? extends CronToken> cronTokens = cronObject.getYears();
        if (current > min) {
            if (cronTokens.contains(CronYears.EVERY)) {
                return current - 1;
            }
            OptionalInt maybeClosest = cronTokens.stream()
                    .mapToInt(cronToken -> findPrevious(cronToken, current, min)).max();
            int closest = maybeClosest.isPresent() ? maybeClosest.getAsInt() : Integer.MIN_VALUE;
            return closest >= min && closest < current ? closest : null;
        }
        return null;
    }

    private Integer backwardNextValidMonth(LocalDateTime dateTime) {
        int min = 1;
        int current = dateTime.getMonthValue();
        Set<? extends CronToken> cronTokens = cronObject.getMonths();
        if (current > min) {
            if (cronTokens.contains(CronMonths.EVERY)) {
                return current - 1;
            }
            OptionalInt maybeClosest = cronTokens.stream()
                    .mapToInt(cronToken -> findPrevious(cronToken, current, min)).max();
            int closest = maybeClosest.isPresent() ? maybeClosest.getAsInt() : Integer.MIN_VALUE;
            return closest >= min && closest < current ? closest : null;
        }
        return null;
    }

    private Integer backwardNextValidDay(LocalDateTime dateTime) {
        int min = 1;
        Set<CronDaysOfMonth> cronDaysOfMonth = cronObject.getDaysOfMonth();
        Set<CronDaysOfWeek> cronDaysOfWeek = cronObject.getDaysOfWeek();
        if (dateTime.getDayOfMonth() > min) {
            if (cronDaysOfMonth.contains(CronDaysOfMonth.EVERY) || cronDaysOfWeek.contains(CronDaysOfWeek.EVERY)) {
                return dateTime.getDayOfMonth() - 1;
            }
            int byDayOfMonth = cronDaysOfMonth.contains(CronDaysOfMonth.ANY) ? Integer.MIN_VALUE
                    : getPreviousDayOfMonth(cronObject.getDaysOfMonth(), dateTime, min);
            int byDayOfWeek = cronDaysOfWeek.contains(CronDaysOfWeek.ANY) ? Integer.MIN_VALUE
                    : getPreviousDayOfWeek(cronObject.getDaysOfWeek(), dateTime, min);
            int closest = Math.max(byDayOfMonth, byDayOfWeek);
            return closest >= min && closest < dateTime.getDayOfMonth() ? closest : null;
        }
        return null;
    }

    private Integer backwardNextValidHour(LocalDateTime dateTime) {
        int min = 0;
        int current = dateTime.getHour();
        Set<? extends CronToken> cronTokens = cronObject.getHours();
        if (current > min) {
            if (cronTokens.contains(CronHours.EVERY)) {
                return current - 1;
            }
            OptionalInt maybeClosest = cronTokens.stream()
                    .mapToInt(cronToken -> findPrevious(cronToken, current, min)).max();
            int closest = maybeClosest.isPresent() ? maybeClosest.getAsInt() : Integer.MIN_VALUE;
            return closest >= min && closest < current ? closest : null;
        }
        return null;
    }

    private Integer backwardNextValidMinute(LocalDateTime dateTime) {
        int min = 0;
        int current = dateTime.getMinute();
        Set<? extends CronToken> cronTokens = cronObject.getMinutes();
        if (current > min) {
            if (cronTokens.contains(CronMinutes.EVERY)) {
                return current - 1;
            }
            OptionalInt maybeClosest = cronTokens.stream()
                    .mapToInt(cronToken -> findPrevious(cronToken, current, min)).max();
            int closest = maybeClosest.isPresent() ? maybeClosest.getAsInt() : Integer.MIN_VALUE;
            return closest >= min && closest < current ? closest : null;
        }
        return null;
    }

    private Integer backwardNextValidSecond(LocalDateTime dateTime) {
        int min = 0;
        int current = dateTime.getSecond();
        Set<? extends CronToken> cronTokens = cronObject.getSeconds();
        if (current > min) {
            if (cronTokens.contains(CronSeconds.EVERY)) {
                return current - 1;
            }
            OptionalInt maybeClosest = cronTokens.stream()
                    .mapToInt(cronToken -> findPrevious(cronToken, current, min)).max();
            int closest = maybeClosest.isPresent() ? maybeClosest.getAsInt() : Integer.MIN_VALUE;
            return closest >= min && closest < current ? closest : null;
        }
        return null;
    }

    private int getPreviousDayOfMonth(Collection<CronDaysOfMonth> cronDays, LocalDateTime dateTime, int min) {
        int current = dateTime.getDayOfMonth();
        if (current > min) {
            if (cronDays.contains(CronDaysOfMonth.EVERY)) {
                return current - 1;
            }
            int closest = Integer.MIN_VALUE;
            for (CronDaysOfMonth cronDay : cronDays) {
                if (cronDay.getStart() > 0 && !cronDay.isClosestWeekday()) {
                    closest = Math.max(closest, findPrevious(cronDay, current, min));
                } else {
                    closest = Math.max(closest, findPreviousSpecialCase(cronDay, dateTime, min));
                }
            }
            return closest >= min && closest < current ? closest : Integer.MIN_VALUE;
        }
        return Integer.MIN_VALUE;
    }

    private int getPreviousDayOfWeek(Collection<CronDaysOfWeek> cronDays, LocalDateTime dateTime, int min) {
        int lastDay = dateTime.getMonth().length(Year.isLeap(dateTime.getYear()));
        int dayOfWeekOfFirst = dayOfWeekToIndex(dateTime.withDayOfMonth(1).getDayOfWeek());
        int dayOfWeekOfLast = dayOfWeekToIndex(dateTime.withDayOfMonth(lastDay).getDayOfWeek());
        int closest = Integer.MIN_VALUE;
        for (var cronDay : cronDays) {
            if (cronDay.hasOrdinal()) {
                int temp = cronDay.getOrdinal() == 0 ? Math.max(closest, findForLastDayOfWeek(cronDay, lastDay, dayOfWeekOfLast))
                        : Math.max(closest, findForOrdinalDayOfWeek(cronDay, lastDay, dayOfWeekOfFirst));
                closest = temp < dateTime.getDayOfMonth() ? temp : closest;
            } else {
                closest = Math.max(closest, findForDayOfWeek(cronDay, dateTime.getDayOfMonth(), dateTime.getDayOfWeek()));
            }
        }
        return closest >= min ? closest : Integer.MIN_VALUE;
    }

    private int findForDayOfWeek(CronDaysOfWeek cronDay, int currentDay, DayOfWeek dayOfWeek) {
        var maybeMinDiff = cronDay.getMatchingDaysOfWeek().stream().mapToInt(d -> dayOfWeekDiff(indexToDayOfWeek(d), dayOfWeek))
                .filter(i -> i > 0).min();
        int minDiff = maybeMinDiff.isPresent() ? maybeMinDiff.getAsInt() : Integer.MAX_VALUE;
        int result = currentDay - minDiff;
        return result >= 1 && result < currentDay ? result : Integer.MIN_VALUE;
    }

    private int findForLastDayOfWeek(CronDaysOfWeek cronDay, int lastDay, int dayOfWeekOfLast) {
        var maybeMinDiff = cronDay.getMatchingDaysOfWeek().stream().mapToInt(d -> dayOfWeekDiff(dayOfWeekOfLast, d)).min();
        int minDiff = maybeMinDiff.isPresent() ? maybeMinDiff.getAsInt() : Integer.MAX_VALUE;
        int result = lastDay - minDiff;
        return result > 0 ? result : Integer.MIN_VALUE;
    }

    private int findForOrdinalDayOfWeek(CronDaysOfWeek cronDay, int lastDay, int dayOfWeekOfFirst) {
        OptionalInt maybeMinDiff = cronDay.getMatchingDaysOfWeek().stream().mapToInt(d -> dayOfWeekDiff(dayOfWeekOfFirst, d)).min();
        int minDiff = maybeMinDiff.isPresent() ? maybeMinDiff.getAsInt() : 32;
        int result = minDiff + 7 * (cronDay.getOrdinal() - 1) + 1;
        return result <= lastDay ? result : 32;
    }

    private boolean checkCurrent(CronToken cronToken, int current) {
        if (!cronToken.hasEnd() && !cronToken.hasEvery()) {
            return cronToken.getStart() == current;
        } else if (cronToken.getStart() <= current) {
            if (cronToken.hasEnd()) {
                if (cronToken.hasEvery() && cronToken.getEnd() >= current) {
                    return (current - cronToken.getStart()) % cronToken.getEvery() == 0;
                }
                return cronToken.getStart() <= current && current <= cronToken.getEnd();
            } else if (cronToken.hasEvery() && !cronToken.hasEnd()) {
                return (current - cronToken.getStart()) % cronToken.getEvery() == 0;
            }
        }
        return false;
    }

    private int findPrevious(CronToken cronToken, int current, int min) {
        if (current > min && cronToken.getStart() < current) {
            if (!cronToken.hasEvery() && !cronToken.hasEnd()) {
                return cronToken.getStart();
            } else if (cronToken.hasEnd() && !cronToken.hasEvery()) {
                return Math.min(current - 1, cronToken.getEnd());
            } else if (cronToken.hasEvery() && (!cronToken.hasEnd() || cronToken.getEnd() >= current - 1)) {
                return cronToken.getStart() + cronToken.getEvery() * ((current - 1 - cronToken.getStart()) / cronToken.getEvery());
            }
        }
        return Integer.MIN_VALUE;
    }

    private int findPreviousSpecialCase(CronDaysOfMonth cronDay, LocalDateTime current, int min) {
        if (current.getDayOfMonth() > min) {
            int day = cronDay.getStart();
            if (day <= 0) {
                day = lastDayOfMonth(current, day);
            }
            if (cronDay.isClosestWeekday()) {
                day = closestWeekDay(current.withDayOfMonth(day));
            }
            return day < current.getDayOfMonth() ? day : Integer.MIN_VALUE;
        }
        return Integer.MIN_VALUE;
    }

}
