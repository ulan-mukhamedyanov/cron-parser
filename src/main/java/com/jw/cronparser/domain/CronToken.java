package com.jw.cronparser.domain;

public interface CronToken {

    Integer getStart();

    Integer getEvery();

    boolean hasEvery();

    Integer getEnd();

    boolean hasEnd();
}
