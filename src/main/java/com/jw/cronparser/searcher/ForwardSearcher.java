package com.jw.cronparser.searcher;

import static com.jw.cronparser.CronUtils.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.OptionalInt;
import java.util.Set;

import com.jw.cronparser.domain.*;

public class ForwardSearcher implements CronSearcher {

    private final CronObject cronObject;

    public ForwardSearcher(CronObject cronObject) {
        this.cronObject = cronObject;
    }

    public LocalDateTime findClosest(LocalDateTime dateTime) {
        var result = forwardChainYear(dateTime);
        if (result != null) {
            return result.withNano(0);
        }
        return null;
    }

    private LocalDateTime forwardChainYear(LocalDateTime dateTime) {
        LocalDateTime current = dateTime;
        LocalDateTime result = null;
        if (forwardCheckCurrentYear(current)) {
            result = forwardChainMonth(current);
        }
        while (result == null) {
            Integer nextValidYear = forwardNextValidYear(current);
            if (nextValidYear == null) {
                return null;
            }
            current = LocalDateTime.of(nextValidYear, 1, 1, 0, 0, 0);
            result = forwardChainMonth(current);
        }
        return result;
    }

    private LocalDateTime forwardChainMonth(LocalDateTime dateTime) {
        LocalDateTime current = dateTime;
        LocalDateTime result = null;
        if (forwardCheckCurrentMonth(current)) {
            result = forwardChainDay(current);
        }
        while (result == null) {
            Integer nextValidMonth = forwardNextValidMonth(current);
            if (nextValidMonth == null) {
                return null;
            }
            current = LocalDateTime.of(current.getYear(), nextValidMonth, 1, 0, 0, 0);
            result = forwardChainDay(current);
        }
        return result;
    }

    private LocalDateTime forwardChainDay(LocalDateTime dateTime) {
        LocalDateTime current = dateTime;
        LocalDateTime result = null;
        if (forwardCheckCurrentDayOfMonth(current) && forwardCheckCurrentDayOfWeek(current)) {
            result = forwardChainHour(current);
        }
        while (result == null) {
            Integer nextValidDay = forwardNextValidDay(current);
            if (nextValidDay == null) {
                return null;
            }
            current = LocalDateTime.of(current.getYear(), current.getMonthValue(), nextValidDay, 0, 0, 0);
            result = forwardChainHour(current);
        }
        return result;
    }

    private LocalDateTime forwardChainHour(LocalDateTime dateTime) {
        LocalDateTime current = dateTime;
        LocalDateTime result = null;
        if (forwardCheckCurrentHour(current)) {
            result = forwardChainMinute(current);
        }
        while (result == null) {
            Integer nextValidHour = forwardNextValidHour(current);
            if (nextValidHour == null) {
                return null;
            }
            current = LocalDateTime.of(current.getYear(), current.getMonthValue(), current.getDayOfMonth(), nextValidHour, 0, 0);
            result = forwardChainMinute(current);
        }
        return result;
    }

    private LocalDateTime forwardChainMinute(LocalDateTime dateTime) {
        LocalDateTime current = dateTime;
        LocalDateTime result = null;
        if (forwardCheckCurrentMinute(current)) {
            result = forwardChainSecond(current);
        }
        while (result == null) {
            Integer nextValidMinute = forwardNextValidMinute(current);
            if (nextValidMinute == null) {
                return null;
            }
            current = LocalDateTime.of(current.getYear(), current.getMonthValue(), current.getDayOfMonth(),
                    current.getHour(), nextValidMinute, 0);
            result = forwardChainSecond(current);
        }
        return result;
    }

    private LocalDateTime forwardChainSecond(LocalDateTime dateTime) {
        if (forwardCheckCurrentSecond(dateTime)) {
            return dateTime;
        }
        Integer nextValidSecond = forwardNextValidSecond(dateTime);
        if (nextValidSecond == null) {
            return null;
        } else {
            return LocalDateTime.of(dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(),
                    dateTime.getHour(), dateTime.getMinute(), nextValidSecond);
        }
    }


    private boolean forwardCheckCurrentYear(LocalDateTime dateTime) {
        Set<CronYears> cronYears = cronObject.getYears();
        return cronYears.contains(CronYears.EVERY)
                || cronYears.stream().anyMatch(cronYear -> checkCurrent(cronYear, dateTime.getYear()));
    }

    private boolean forwardCheckCurrentMonth(LocalDateTime dateTime) {
        Set<CronMonths> cronMonths = cronObject.getMonths();
        return cronMonths.contains(CronMonths.EVERY)
                || cronMonths.stream().anyMatch(cronMonth -> checkCurrent(cronMonth, dateTime.getMonthValue()));
    }

    private boolean forwardCheckCurrentDayOfMonth(LocalDateTime dateTime) {
        Set<CronDaysOfMonth> cronDays = cronObject.getDaysOfMonth();
        if (cronDays.contains(CronDaysOfMonth.EVERY) || cronDays.contains(CronDaysOfMonth.ANY)) {
            return true;
        }
        for (CronDaysOfMonth cronDay : cronDays) {
            if ((cronDay.getStart().equals(dateTime.getDayOfMonth())
                    && (!cronDay.isClosestWeekday() || closestWeekDay(dateTime) == dateTime.getDayOfMonth()))
                    || (cronDay.getStart() <= 0 && dateTime.getDayOfMonth() == dateTime.getMonth().length(Year.isLeap(dateTime.getYear())) - cronDay.getStart())
                    || (cronDay.isClosestWeekday() && closestWeekDay(dateTime.withDayOfMonth(cronDay.getStart())) == dateTime.getDayOfMonth())
                    || (cronDay.hasEvery() && cronDay.getStart() < dateTime.getDayOfMonth()
                    && (!cronDay.hasEnd() || cronDay.getEnd() >= dateTime.getDayOfMonth())
                    && (dateTime.getDayOfMonth() - cronDay.getStart()) % cronDay.getEvery() == 0)) {
                return true;
            }
        }
        return false;
    }

    private boolean forwardCheckCurrentDayOfWeek(LocalDateTime dateTime) {
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

    private boolean forwardCheckCurrentHour(LocalDateTime dateTime) {
        Set<CronHours> cronHours = cronObject.getHours();
        return cronHours.contains(CronHours.EVERY)
                || cronHours.stream().anyMatch(cronHour -> checkCurrent(cronHour, dateTime.getHour()));
    }

    private boolean forwardCheckCurrentMinute(LocalDateTime dateTime) {
        Set<CronMinutes> cronMinutes = cronObject.getMinutes();
        return cronMinutes.contains(CronMinutes.EVERY)
                || cronMinutes.stream().anyMatch(cronMinute -> checkCurrent(cronMinute, dateTime.getMinute()));
    }

    private boolean forwardCheckCurrentSecond(LocalDateTime dateTime) {
        Set<CronSeconds> cronSeconds = cronObject.getSeconds();
        return cronSeconds.contains(CronSeconds.EVERY)
                || cronSeconds.stream().anyMatch(cronSecond -> checkCurrent(cronSecond, dateTime.getSecond()));
    }


    private Integer forwardNextValidYear(LocalDateTime dateTime) {
        if (cronObject.getYears().contains(CronYears.EVERY)) {
            return dateTime.getYear() + 1;
        }
        int closest = 2100;
        for (CronYears cronYear : cronObject.getYears()) {
            if (!cronYear.hasEnd() || cronYear.getEnd() > dateTime.getYear()) {
                if (cronYear.getStart() > dateTime.getYear()) {
                    closest = Math.min(closest, cronYear.getStart());
                } else if (cronYear.getStart() <= dateTime.getYear() && cronYear.hasEvery()) {
                    int nextValidYear = cronYear.getStart() + cronYear.getEvery() * ((dateTime.getYear() - cronYear.getStart()) / cronYear.getEvery() + 1);
                    closest = !cronYear.hasEnd() || cronYear.getEnd() >= nextValidYear ? Math.min(closest, nextValidYear) : closest;
                }
            }
            if (closest == dateTime.getYear() + 1) {
                return closest;
            }
        }
        return closest < 2100 ? closest : null;
    }

    private Integer forwardNextValidMonth(LocalDateTime dateTime) {
        if (cronObject.getMonths().contains(CronMonths.EVERY)) {
            return dateTime.getMonthValue() + 1 <= 12 ? dateTime.getMonthValue() + 1 : null;
        }
        int closest = 13;
        for (CronMonths cronMonth : cronObject.getMonths()) {
            if (!cronMonth.hasEnd() || cronMonth.getEnd() > dateTime.getMonthValue()) {
                if (cronMonth.getStart() > dateTime.getMonthValue()) {
                    closest = Math.min(closest, cronMonth.getStart());
                } else if (cronMonth.getStart() <= dateTime.getMonthValue() && cronMonth.hasEvery()) {
                    int nextValidMonth = cronMonth.getStart() + cronMonth.getEvery() * ((dateTime.getMonthValue() - cronMonth.getStart()) / cronMonth.getEvery() + 1);
                    closest = !cronMonth.hasEnd() || cronMonth.getEnd() >= nextValidMonth ? Math.min(closest, nextValidMonth) : closest;
                }
            }
            if (closest == dateTime.getMonthValue() + 1) {
                break;
            }
        }
        return closest <= 12 ? closest : null;
    }

    private Integer forwardNextValidDay(LocalDateTime dateTime) {
        var cronDaysOfMonth = cronObject.getDaysOfMonth();
        var cronDaysOfWeek = cronObject.getDaysOfWeek();
        var lastDay = dateTime.withDayOfMonth(dateTime.getMonth().length(Year.isLeap(dateTime.getYear())));
        if (cronDaysOfMonth.contains(CronDaysOfMonth.EVERY) || cronDaysOfWeek.contains(CronDaysOfWeek.EVERY)) {
            return dateTime.getDayOfMonth() + 1 <= lastDay.getDayOfMonth() ? dateTime.getDayOfMonth() + 1 : null;
        }
        int nextDayByDayOfMonth = !cronDaysOfMonth.contains(CronDaysOfMonth.ANY) ? findNextDayByDayOfMonth(dateTime, cronDaysOfMonth) : 32;
        int nextDayByDayOfWeek = !cronDaysOfWeek.contains(CronDaysOfWeek.ANY) ? findNextDayByDayOfWeek(dateTime, cronDaysOfWeek) : 32;
        int closest = Math.min(nextDayByDayOfMonth, nextDayByDayOfWeek);
        return closest <= lastDay.getDayOfMonth() && closest > dateTime.getDayOfMonth() ? closest : null;
    }

    private Integer forwardNextValidHour(LocalDateTime dateTime) {
        if (cronObject.getHours().contains(CronHours.EVERY)) {
            return dateTime.getHour() + 1 < 24 ? dateTime.getHour() + 1 : null;
        }
        int closest = 24;
        for (CronHours cronHour : cronObject.getHours()) {
            if (!cronHour.hasEnd() || cronHour.getEnd() > dateTime.getHour()) {
                if (cronHour.getStart() > dateTime.getHour()) {
                    closest = Math.min(closest, cronHour.getStart());
                } else if (cronHour.getStart() <= dateTime.getHour() && cronHour.hasEvery()) {
                    int nextValidHour = cronHour.getStart() + cronHour.getEvery() * ((dateTime.getHour() - cronHour.getStart()) / cronHour.getEvery() + 1);
                    closest = !cronHour.hasEnd() || cronHour.getEnd() >= nextValidHour ? Math.min(closest, nextValidHour) : closest;
                }
            }
            if (closest == dateTime.getHour() + 1) {
                break;
            }
        }
        return closest < 24 ? closest : null;
    }

    private Integer forwardNextValidMinute(LocalDateTime dateTime) {
        if (cronObject.getMinutes().contains(CronMinutes.EVERY)) {
            return dateTime.getMinute() + 1 < 60 ? dateTime.getMinute() + 1 : null;
        }
        int closest = 60;
        for (CronMinutes cronMinute : cronObject.getMinutes()) {
            if (!cronMinute.hasEnd() || cronMinute.getEnd() > dateTime.getMinute()) {
                if (cronMinute.getStart() > dateTime.getMinute()) {
                    closest = Math.min(closest, cronMinute.getStart());
                } else if (cronMinute.getStart() <= dateTime.getMinute() && cronMinute.hasEvery()) {
                    int nextValidMinute = cronMinute.getStart() + cronMinute.getEvery() * ((dateTime.getMinute() - cronMinute.getStart()) / cronMinute.getEvery() + 1);
                    closest = !cronMinute.hasEnd() || cronMinute.getEnd() >= nextValidMinute ? Math.min(closest, nextValidMinute) : closest;
                }
            }
            if (closest == dateTime.getMinute() + 1) {
                break;
            }
        }
        return closest < 60 ? closest : null;
    }

    private Integer forwardNextValidSecond(LocalDateTime dateTime) {
        if (cronObject.getSeconds().contains(CronSeconds.EVERY)) {
            return dateTime.getSecond() + 1 < 60 ? dateTime.getSecond() + 1 : null;
        }
        int closest = 60;
        for (CronSeconds cronSecond : cronObject.getSeconds()) {
            if (!cronSecond.hasEnd() || cronSecond.getEnd() > dateTime.getSecond()) {
                if (cronSecond.getStart() > dateTime.getSecond()) {
                    closest = Math.min(closest, cronSecond.getStart());
                } else if (cronSecond.getStart() <= dateTime.getSecond() && cronSecond.hasEvery()) {
                    int nextValidSecond = cronSecond.getStart() + cronSecond.getEvery() * ((dateTime.getSecond() - cronSecond.getStart()) / cronSecond.getEvery() + 1);
                    closest = !cronSecond.hasEnd() || cronSecond.getEnd() >= nextValidSecond ? Math.min(closest, nextValidSecond) : closest;
                }
            }
            if (closest == dateTime.getSecond() + 1) {
                break;
            }
        }
        return closest < 60 ? closest : null;
    }


    private int findNextDayByDayOfMonth(LocalDateTime dateTime, Set<CronDaysOfMonth> cronDaysOfMonth) {
        int lastDay = dateTime.getMonth().length(Year.isLeap(dateTime.getYear()));
        int closest = 32;
        for (var cronDay : cronDaysOfMonth) {
            if (cronDay.getStart() <= 0) {
                closest = Math.min(closest, lastDay - cronDay.getStart());
            } else if (cronDay.getStart() > dateTime.getDayOfMonth()) {
                closest = Math.min(closest, cronDay.getStart());
            } else if (cronDay.getStart() < dateTime.getDayOfMonth()) {
                if (cronDay.hasEnd() && cronDay.getEnd() > dateTime.getDayOfMonth()) {
                    return dateTime.getDayOfMonth() + 1;
                } else if (cronDay.hasEvery()) {
                    int nextValidDay = cronDay.getStart() + cronDay.getEvery() * ((dateTime.getDayOfMonth() - cronDay.getStart()) / cronDay.getEvery() + 1);
                    closest = !cronDay.hasEnd() || cronDay.getEnd() >= nextValidDay ? Math.min(closest, nextValidDay) : closest;
                }
            }
            closest = !cronDay.isClosestWeekday() && closest <= lastDay ? Math.min(closest, closestWeekDay(dateTime.withDayOfMonth(closest))) : closest;
        }
        return closest;
    }

    private int findNextDayByDayOfWeek(LocalDateTime dateTime, Set<CronDaysOfWeek> cronDaysOfWeek) {
        int lastDay = dateTime.getMonth().length(Year.isLeap(dateTime.getYear()));
        int dayOfWeekOfFirst = dayOfWeekToIndex(dateTime.withDayOfMonth(1).getDayOfWeek());
        int dayOfWeekOfLast = dayOfWeekToIndex(dateTime.withDayOfMonth(lastDay).getDayOfWeek());
        int closest = 32;
        for (var cronDay : cronDaysOfWeek) {
            if (cronDay.hasOrdinal()) {
                closest = cronDay.getOrdinal() == 0 ? Math.min(closest, findForLastDayOfWeek(cronDay, lastDay, dayOfWeekOfLast))
                        : Math.min(closest, findForOrdinalDayOfWeek(cronDay, lastDay, dayOfWeekOfFirst));
            } else {
                closest = Math.min(closest, findForDayOfWeek(cronDay, lastDay, dateTime.getDayOfMonth(), dateTime.getDayOfWeek()));
            }
        }
        return closest;
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

    private int findForDayOfWeek(CronDaysOfWeek cronDay, int lastDay, int currentDay, DayOfWeek dayOfWeek) {
        var maybeMinDiff = cronDay.getMatchingDaysOfWeek().stream().mapToInt(d -> dayOfWeekDiff(dayOfWeek, indexToDayOfWeek(d)))
                .filter(i -> i > 0).min();
        int minDiff = maybeMinDiff.isPresent() ? maybeMinDiff.getAsInt() : 32;
        int result = currentDay + minDiff;
        return result <= lastDay ? result : 32;
    }

    private int findForLastDayOfWeek(CronDaysOfWeek cronDay, int lastDay, int dayOfWeekOfLast) {
        var maybeMinDiff = cronDay.getMatchingDaysOfWeek().stream().mapToInt(d -> dayOfWeekDiff(d, dayOfWeekOfLast)).min();
        int minDiff = maybeMinDiff.isPresent() ? maybeMinDiff.getAsInt() : 32;
        int result = lastDay - minDiff;
        return result > 0 ? result : 32;
    }

    private int findForOrdinalDayOfWeek(CronDaysOfWeek cronDay, int lastDay, int dayOfWeekOfFirst) {
        OptionalInt maybeMinDiff = cronDay.getMatchingDaysOfWeek().stream().mapToInt(d -> dayOfWeekDiff(dayOfWeekOfFirst, d)).min();
        int minDiff = maybeMinDiff.isPresent() ? maybeMinDiff.getAsInt() : 32;
        int result = minDiff + 7 * (cronDay.getOrdinal() - 1) + 1;
        return result <= lastDay ? result : 32;
    }

}
